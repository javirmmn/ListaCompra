package com.javirmnn.listacompra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javirmnn.listacompra.viewmodel.AuthViewModel

@Composable
fun SetupProfileScreen(authViewModel: AuthViewModel) {
    var alias by remember { mutableStateOf("") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Te damos la bienvenida!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Cómo quieres que te llamen en las listas compartidas?",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                label = { Text("Tu nombre o alias") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (alias.isNotBlank()) {
                        // Le pasamos el nombre al cerebro sin espacios en blanco al principio/final
                        authViewModel.guardarAlias(alias.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = alias.isNotBlank() // El botón no se puede pulsar si está vacío
            ) {
                Text(text = "Guardar y entrar", fontSize = 16.sp)
            }
        }
    }
}