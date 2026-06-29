package com.javirmnn.listacompra.data

data class Producto(
    val id: Int,
    val nombre: String,
    val seleccionado: Boolean = false
)