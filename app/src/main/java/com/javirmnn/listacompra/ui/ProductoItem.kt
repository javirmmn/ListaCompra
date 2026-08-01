package com.javirmnn.listacompra.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javirmnn.listacompra.data.Producto

@Composable
fun ProductoItem(
    producto: Producto,
    onCheckedChange: (Boolean) -> Unit,
    onUpdateCantidad: (Int) -> Unit,
    onUpdateComentario: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var editandoComentario by remember { mutableStateOf(false) }
    var textoComentario by remember {
        mutableStateOf(TextFieldValue(text = producto.comentario, selection = TextRange(producto.comentario.length)))
    }
    var showMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(editandoComentario) {
        if (editandoComentario) {
            textoComentario = TextFieldValue(text = producto.comentario, selection = TextRange(producto.comentario.length))
            focusRequester.requestFocus()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Textos
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = producto.nombre, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    if (producto.anadidoPor.isNotBlank()) {
                        Text(
                            text = "Añadido por: ${producto.anadidoPor}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. Botones de Cantidad (Solo marcados)
                if (producto.seleccionado) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                // Si está en 1 y restamos, se desmarca automáticamente
                                if (producto.cantidad <= 1) {
                                    onCheckedChange(false)
                                } else {
                                    onUpdateCantidad(producto.cantidad - 1)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = producto.cantidad.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        FilledTonalIconButton(
                            onClick = { onUpdateCantidad(producto.cantidad + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // 3. Casilla de marcado
                Checkbox(
                    checked = producto.seleccionado,
                    onCheckedChange = { onCheckedChange(it) }
                )

                // 4. Los tres puntos
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            // 5. Comentarios
            if (editandoComentario) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        text = "(${textoComentario.text.length}/20)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                    OutlinedTextField(
                        value = textoComentario,
                        onValueChange = {
                            if (it.text.length <= 20) textoComentario = it
                        },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                editandoComentario = false
                                onUpdateComentario(textoComentario.text)
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Guardar")
                            }
                        }
                    )
                }
            } else {
                if (producto.comentario.isEmpty()) {
                    Text(
                        text = "+ Comentario",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { editandoComentario = true }
                    )
                } else {
                    Text(
                        text = "Nota: ${producto.comentario}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { editandoComentario = true }
                    )
                }
            }
        }
    }
}