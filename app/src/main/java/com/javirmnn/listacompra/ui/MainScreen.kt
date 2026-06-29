package com.javirmnn.listacompra.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.javirmnn.listacompra.viewmodel.ListaViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: ListaViewModel) {
    val productos = viewModel.productos

    var showDialog by remember { mutableStateOf(false) }
    var productoAEditar by remember { mutableStateOf<com.javirmnn.listacompra.data.Producto?>(null) }
    var productoABorrar by remember { mutableStateOf<com.javirmnn.listacompra.data.Producto?>(null) }

    val paraComprar = productos.filter { it.seleccionado }
    val posibles = productos.filter { !it.seleccionado }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lista de la compra", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {

            // SECCIÓN 1: PARA COMPRAR (Siempre visible)
            item { DividerHeader("Para comprar") }

            if (paraComprar.isNotEmpty()) {
                items(paraComprar, key = { it.id }) { producto ->
                    ProductoItem(
                        producto = producto,
                        modifier = Modifier.animateItem(),
                        onClick = { viewModel.cambiarSeleccion(producto) },
                        onEdit = { productoAEditar = producto },
                        onDelete = { productoABorrar = producto }
                    )
                }
            } else {
                item { MensajeVacio("La lista está vacía") }
            }

            // SECCIÓN 2: PRODUCTOS (Siempre visible)
            item { DividerHeader("Productos") }

            if (posibles.isNotEmpty()) {
                items(posibles, key = { it.id }) { producto ->
                    ProductoItem(
                        producto = producto,
                        modifier = Modifier.animateItem(),
                        onClick = { viewModel.cambiarSeleccion(producto) },
                        onEdit = { productoAEditar = producto },
                        onDelete = { productoABorrar = producto }
                    )
                }
            } else {
                item { MensajeVacio("No hay productos guardados") }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        AddProductDialog(
            titulo = "Añadir producto",
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.agregarProducto(it)
                showDialog = false
            }
        )
    }

    productoAEditar?.let { producto ->
        AddProductDialog(
            titulo = "Editar producto",
            textoInicial = producto.nombre,
            onDismiss = { productoAEditar = null },
            onConfirm = { nuevoNombre ->
                viewModel.editarProducto(producto, nuevoNombre)
                productoAEditar = null
            }
        )
    }

    productoABorrar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoABorrar = null },
            title = { Text("¿Estás seguro?") },
            text = { Text("Vas a borrar '${producto.nombre}' de tu lista. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarProducto(producto)
                        productoABorrar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { productoABorrar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun DividerHeader(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp, start = 20.dp, end = 20.dp) // Ajustado para que quede mejor proporcionado
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    }
}

// Nuevo componente para controlar los textos de lista vacía
@Composable
fun MensajeVacio(texto: String) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // AQUÍ CAMBIAS LA SEPARACIÓN (antes era 32.dp)
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        style = MaterialTheme.typography.bodyLarge
    )
}