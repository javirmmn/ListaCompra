package com.javirmnn.listacompra.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.javirmnn.listacompra.data.Producto

@Composable
fun ProductoItem(
    producto: Producto,
    modifier: Modifier = Modifier, // <-- Nuevo: permite recibir animaciones
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expandirMenu by remember { mutableStateOf(false) }

    // Usamos ElevatedCard para darle profundidad y sombra
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp) // Separación entre tarjetas
            .clickable(onClick = onClick) // Toda la tarjeta es clicable
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp) // Espacio interior de la tarjeta
        ) {
            Checkbox(
                checked = producto.seleccionado,
                onCheckedChange = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = producto.nombre,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            // Menú de opciones
            Box {
                IconButton(onClick = { expandirMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones"
                    )
                }

                DropdownMenu(
                    expanded = expandirMenu,
                    onDismissRequest = { expandirMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            expandirMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Borrar", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expandirMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}