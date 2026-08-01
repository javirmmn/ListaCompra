package com.javirmnn.listacompra.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.javirmnn.listacompra.data.ListaCompartida
import com.javirmnn.listacompra.data.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ListaViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _misListas = MutableStateFlow<List<ListaCompartida>>(emptyList())
    val misListas: StateFlow<List<ListaCompartida>> = _misListas.asStateFlow()

    private val _listaActiva = MutableStateFlow<ListaCompartida?>(null)
    val listaActiva: StateFlow<ListaCompartida?> = _listaActiva.asStateFlow()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    init {
        obtenerMisListas()
    }

    private fun obtenerMisListas() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("listas")
            .whereArrayContains("miembros", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val listas = snapshot.documents.mapNotNull { it.toObject(ListaCompartida::class.java) }
                    _misListas.value = listas
                    if (_listaActiva.value == null && listas.isNotEmpty()) {
                        seleccionarLista(listas.first())
                    }
                }
            }
    }

    fun seleccionarLista(lista: ListaCompartida) {
        _listaActiva.value = lista
        obtenerProductos(lista.id)
    }

    fun crearNuevaLista(nombreLista: String) {
        val uid = auth.currentUser?.uid ?: return
        val idLista = db.collection("listas").document().id
        val codigoSecreto = generarCodigo()
        val nuevaLista = ListaCompartida(idLista, nombreLista, codigoSecreto, listOf(uid))
        db.collection("listas").document(idLista).set(nuevaLista)
    }

    fun unirseALista(codigo: String, onResultado: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("listas").whereEqualTo("codigoInvitacion", codigo).get()
            .addOnSuccessListener { resultados ->
                if (!resultados.isEmpty) {
                    val listaEncontrada = resultados.documents.first()
                    db.collection("listas").document(listaEncontrada.id)
                        .update("miembros", FieldValue.arrayUnion(uid))
                        .addOnSuccessListener { onResultado(true) }
                } else {
                    onResultado(false)
                }
            }.addOnFailureListener { onResultado(false) }
    }

    private fun generarCodigo(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { caracteres.random() }.joinToString("")
    }

    fun cambiarNombreLista(listaId: String, nuevoNombre: String) {
        db.collection("listas").document(listaId).update("nombre", nuevoNombre)
    }

    fun eliminarLista(listaId: String) {
        db.collection("listas").document(listaId).delete()
        if (_listaActiva.value?.id == listaId) {
            _listaActiva.value = null
            _productos.value = emptyList()
        }
    }

    private fun obtenerProductos(listaId: String) {
        db.collection("listas").document(listaId).collection("productos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val listaProductos = snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }
                    _productos.value = listaProductos
                }
            }
    }

    fun agregarProducto(nombre: String, aliasDelUsuario: String) {
        val listaId = _listaActiva.value?.id ?: return
        val rutaProductos = db.collection("listas").document(listaId).collection("productos")
        val idDocumento = rutaProductos.document().id
        val nuevoProducto = Producto(idDocumento, nombre, false, aliasDelUsuario, 1, "")
        rutaProductos.document(idDocumento).set(nuevoProducto)
    }

    // MODIFICADO: Al seleccionar, la cantidad pasa automáticamente a 1
    fun cambiarEstado(producto: Producto) {
        val listaId = _listaActiva.value?.id ?: return
        val nuevoEstado = !producto.seleccionado

        val actualizaciones = mutableMapOf<String, Any>("seleccionado" to nuevoEstado)
        if (nuevoEstado) {
            actualizaciones["cantidad"] = 1
        }

        db.collection("listas").document(listaId).collection("productos")
            .document(producto.id).update(actualizaciones)
    }

    fun actualizarCantidad(producto: Producto, nuevaCantidad: Int) {
        if (nuevaCantidad < 1) return
        val listaId = _listaActiva.value?.id ?: return
        db.collection("listas").document(listaId).collection("productos")
            .document(producto.id).update("cantidad", nuevaCantidad)
    }

    fun actualizarComentario(producto: Producto, nuevoComentario: String) {
        val listaId = _listaActiva.value?.id ?: return
        db.collection("listas").document(listaId).collection("productos")
            .document(producto.id).update("comentario", nuevoComentario)
    }

    fun cambiarNombreProducto(productoId: String, nuevoNombre: String) {
        val listaId = _listaActiva.value?.id ?: return
        db.collection("listas").document(listaId).collection("productos")
            .document(productoId).update("nombre", nuevoNombre)
    }

    fun eliminarProducto(productoId: String) {
        val listaId = _listaActiva.value?.id ?: return
        db.collection("listas").document(listaId).collection("productos")
            .document(productoId).delete()
    }
}