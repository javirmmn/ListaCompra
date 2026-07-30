package com.javirmnn.listacompra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance() // Conectamos a nuestra base de datos

    private val _usuario = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val usuario: StateFlow<FirebaseUser?> = _usuario.asStateFlow()

    // Estado del perfil: null = cargando, true = ya tiene nombre, false = falta nombre
    private val _tienePerfil = MutableStateFlow<Boolean?>(null)
    val tienePerfil: StateFlow<Boolean?> = _tienePerfil.asStateFlow()

    init {
        // Al arrancar la app, si ya había iniciado sesión antes, comprobamos si tiene perfil
        verificarPerfil(auth.currentUser)
    }

    fun actualizarUsuario(user: FirebaseUser?) {
        _usuario.value = user
        verificarPerfil(user) // Cada vez que entra alguien, verificamos si existe su perfil
    }

    // Va a Firebase a ver si este usuario ya tiene una "ficha" creada
    private fun verificarPerfil(user: FirebaseUser?) {
        if (user == null) {
            _tienePerfil.value = null
            return
        }

        viewModelScope.launch {
            try {
                // Buscamos un documento con el ID secreto del usuario
                val documento = db.collection("usuarios").document(user.uid).get().await()
                _tienePerfil.value = documento.exists()
            } catch (e: Exception) {
                _tienePerfil.value = false
            }
        }
    }

    // Guarda el nombre del usuario en la base de datos
    fun guardarAlias(alias: String) {
        val user = auth.currentUser ?: return

        val datosUsuario = hashMapOf(
            "alias" to alias,
            "email" to user.email
        )

        db.collection("usuarios").document(user.uid)
            .set(datosUsuario)
            .addOnSuccessListener {
                _tienePerfil.value = true // Al guardarse con éxito, le damos permiso para pasar
            }
    }

    fun cerrarSesion() {
        auth.signOut()
        _usuario.value = null
        _tienePerfil.value = null
    }
}