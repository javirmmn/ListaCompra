package com.javirmnn.listacompra.data

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val seleccionado: Boolean = false,
    val anadidoPor: String = "",
    val cantidad: Int = 1, // Nuevo campo
    val comentario: String = "" // Nuevo campo
)