package com.javirmnn.listacompra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Creamos un único esquema de color que usaremos siempre
private val EsquemaFijo = lightColorScheme(
    primary = Primario,
    background = FondoApp,
    surface = FondoTarjeta,
    onPrimary = Color.White,
    onBackground = TextoPrincipal,
    onSurface = TextoPrincipal,
    onSurfaceVariant = TextoSecundario,
    error = ErrorColor
)

@Composable
fun ListaCompraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaFijo, // Ignoramos el sistema e imponemos el nuestro
        typography = Typography,
        content = content
    )
}