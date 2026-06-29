package com.javirmnn.listacompra.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.delay

@Composable
fun AddProductDialog(
    titulo: String = "Añadir producto",
    textoInicial: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // AQUÍ ESTÁ LA MAGIA: Le decimos que el cursor (selection) vaya al tamaño total del texto
    var texto by remember {
        mutableStateOf(
            TextFieldValue(
                text = textoInicial,
                selection = TextRange(textoInicial.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Nombre") },
                modifier = Modifier.focusRequester(focusRequester),
                singleLine = true
            )
        },
        confirmButton = {
            // Sacamos el texto plano (texto.text) para pasarlo a la base de datos
            Button(onClick = { onConfirm(texto.text) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}