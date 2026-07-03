package com.javirmnn.listacompra.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.javirmnn.listacompra.data.DatabaseProvider
import com.javirmnn.listacompra.data.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).productoDao()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    init {
        // Escuchamos la base de datos en tiempo real
        viewModelScope.launch {
            dao.obtenerTodos().collect { lista ->
                _productos.value = lista
            }
        }
    }

    fun agregarProducto(nombre: String) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            dao.insertar(Producto(nombre = nombre.trim()))
        }
    }

    fun cambiarSeleccion(producto: Producto) {
        viewModelScope.launch {
            dao.actualizar(producto.copy(seleccionado = !producto.seleccionado))
        }
    }

    fun editarProducto(producto: Producto, nuevoNombre: String) {
        if (nuevoNombre.isBlank()) return
        viewModelScope.launch {
            dao.actualizar(producto.copy(nombre = nuevoNombre.trim()))
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            dao.borrar(producto)
        }
    }
}