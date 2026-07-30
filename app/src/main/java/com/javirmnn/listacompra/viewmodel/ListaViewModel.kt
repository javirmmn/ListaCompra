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

    // 1. Aquí guardamos las listas a las que el usuario tiene acceso
    private val _misListas = MutableStateFlow<List<ListaCompartida>>(emptyList())
    val misListas: StateFlow<List<ListaCompartida>> = _misListas.asStateFlow()

    // 2. Aquí guardamos la lista en la que estamos dentro ahora mismo
    private val _listaActiva = MutableStateFlow<ListaCompartida?>(null)
    val listaActiva: StateFlow<ListaCompartida?> = _listaActiva.asStateFlow()

    // 3. Los productos que hay DENTRO de la lista activa
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    init {
        obtenerMisListas()
    }

    // --- LÓGICA DE LAS SALAS (LISTAS) ---

    private fun obtenerMisListas() {
        val uid = auth.currentUser?.uid ?: return

        // Buscamos solo las listas donde el UID del usuario esté en la lista de "miembros"
        db.collection("listas")
            .whereArrayContains("miembros", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val listas = snapshot.documents.mapNotNull { it.toObject(ListaCompartida::class.java) }
                    _misListas.value = listas

                    // Si el usuario entra y no tiene ninguna lista seleccionada, le abrimos la primera
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

        val nuevaLista = ListaCompartida(
            id = idLista,
            nombre = nombreLista,
            codigoInvitacion = codigoSecreto,
            miembros = listOf(uid) // El que la crea es el primer miembro
        )

        db.collection("listas").document(idLista).set(nuevaLista)
    }

    fun unirseALista(codigo: String, onResultado: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("listas")
            .whereEqualTo("codigoInvitacion", codigo)
            .get()
            .addOnSuccessListener { resultados ->
                if (!resultados.isEmpty) {
                    val listaEncontrada = resultados.documents.first()
                    db.collection("listas").document(listaEncontrada.id)
                        .update("miembros", FieldValue.arrayUnion(uid))
                        .addOnSuccessListener {
                            onResultado(true) // ¡Código correcto!
                        }
                } else {
                    onResultado(false) // ¡Código incorrecto!
                }
            }
            .addOnFailureListener {
                onResultado(false) // Fallo de conexión o error
            }
    }

    private fun generarCodigo(): String {
        // Generamos un código de 8 caracteres (2,8 billones de combinaciones)
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { caracteres.random() }.joinToString("")
    }


    // --- LÓGICA DE LOS PRODUCTOS (Ahora dentro de carpetas) ---

    private fun obtenerProductos(listaId: String) {
        // Ahora buscamos en listas -> [ID de la lista] -> productos
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

        // Preparamos el camino hacia la sub-carpeta de esta lista
        val rutaProductos = db.collection("listas").document(listaId).collection("productos")
        val idDocumento = rutaProductos.document().id

        val nuevoProducto = Producto(
            id = idDocumento,
            nombre = nombre,
            seleccionado = false,
            anadidoPor = aliasDelUsuario // ¡Aquí está tu parámetro perfectamente integrado!
        )

        rutaProductos.document(idDocumento).set(nuevoProducto)
    }

    fun cambiarEstado(producto: Producto) {
        val listaId = _listaActiva.value?.id ?: return
        db.collection("listas").document(listaId).collection("productos").document(producto.id)
            .update("seleccionado", !producto.seleccionado)
    }

    fun cambiarNombreLista(listaId: String, nuevoNombre: String) {
        db.collection("listas").document(listaId).update("nombre", nuevoNombre)
    }

    fun eliminarLista(listaId: String) {
        // Eliminamos el documento de la lista de la base de datos
        db.collection("listas").document(listaId).delete()

        // Si la lista que acabamos de borrar era la que teníamos abierta en pantalla, la cerramos
        if (_listaActiva.value?.id == listaId) {
            _listaActiva.value = null
            _productos.value = emptyList()
        }
    }
}