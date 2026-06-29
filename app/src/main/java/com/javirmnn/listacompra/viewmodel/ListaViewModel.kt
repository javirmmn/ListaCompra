package com.javirmnn.listacompra.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.javirmnn.listacompra.data.Producto

class ListaViewModel : ViewModel() {

    private var siguienteId = 1

    private val _productos = mutableStateListOf<Producto>()
    val productos = _productos

    fun agregarProducto(nombre: String) {

        if (nombre.isBlank()) return

        _productos.add(
            Producto(
                id = siguienteId++,
                nombre = nombre.trim()
            )
        )

        ordenar()
    }

    fun cambiarSeleccion(producto: Producto) {

        val index =
            _productos.indexOfFirst {
                it.id == producto.id
            }

        if (index == -1) return

        _productos[index] =
            producto.copy(
                seleccionado = !producto.seleccionado
            )

        ordenar()
    }

    fun editarProducto(
        producto: Producto,
        nuevoNombre: String
    ) {

        val index =
            _productos.indexOfFirst {
                it.id == producto.id
            }

        if (index == -1) return

        _productos[index] =
            producto.copy(
                nombre = nuevoNombre.trim()
            )

        ordenar()
    }

    fun eliminarProducto(
        producto: Producto
    ) {

        _productos.remove(producto)
    }

    private fun ordenar() {

        val ordenados =
            _productos.sortedWith(
                compareByDescending<Producto> {
                    it.seleccionado
                }.thenBy {
                    it.nombre.lowercase()
                }
            )

        _productos.clear()
        _productos.addAll(ordenados)
    }
}