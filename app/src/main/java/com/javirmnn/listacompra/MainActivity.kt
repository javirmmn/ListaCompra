package com.javirmnn.listacompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.javirmnn.listacompra.ui.LoginScreen
import com.javirmnn.listacompra.ui.MainScreen
import com.javirmnn.listacompra.ui.SetupProfileScreen
import com.javirmnn.listacompra.ui.theme.ListaCompraTheme
import com.javirmnn.listacompra.viewmodel.AuthViewModel
import com.javirmnn.listacompra.viewmodel.ListaViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val listaViewModel = ViewModelProvider(this)[ListaViewModel::class.java]
        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setContent {
            ListaCompraTheme {
                val usuario by authViewModel.usuario.collectAsState()
                val tienePerfil by authViewModel.tienePerfil.collectAsState()

                if (usuario == null) {
                    // Fase 1: Si no hay usuario, mostrar Login
                    LoginScreen(authViewModel = authViewModel)
                } else {
                    // Si ya hay usuario, comprobamos cómo está su perfil
                    when (tienePerfil) {
                        null -> {
                            // Está buscando en la base de datos (mostramos una ruedita de carga)
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        false -> {
                            // Fase 2: No hemos encontrado su nombre, se lo pedimos
                            SetupProfileScreen(authViewModel = authViewModel)
                        }
                        true -> {
                            // Fase 3: Todo en orden, le pasamos a la lista
                            MainScreen(viewModel = listaViewModel)
                        }
                    }
                }
            }
        }
    }
}