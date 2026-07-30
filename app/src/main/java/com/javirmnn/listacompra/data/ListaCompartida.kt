package com.javirmnn.listacompra.data

data class ListaCompartida(
    val id: String = "",
    val nombre: String = "",
    val codigoInvitacion: String = "", // El código secreto de 6 letras/números
    val miembros: List<String> = emptyList() // Lista de los UID de los usuarios que pueden entrar
)