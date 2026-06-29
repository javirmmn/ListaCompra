package com.javirmnn.listacompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.javirmnn.listacompra.ui.MainScreen
import com.javirmnn.listacompra.ui.theme.ListaCompraTheme
import com.javirmnn.listacompra.viewmodel.ListaViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val viewModel =
            ViewModelProvider(this)[ListaViewModel::class.java]

        setContent {
            ListaCompraTheme {
                MainScreen(viewModel)
            }
        }
    }
}