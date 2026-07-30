package com.javirmnn.listacompra.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.javirmnn.listacompra.R
import com.javirmnn.listacompra.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    val context = LocalContext.current

    // Este "lanzador" abrirá la ventanita nativa de Android para elegir tu cuenta de Google
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val cuenta = task.getResult(Exception::class.java)
            val credencial = GoogleAuthProvider.getCredential(cuenta.idToken, null)

            // Le damos las credenciales a Firebase para que nos deje entrar
            FirebaseAuth.getInstance().signInWithCredential(credencial)
                .addOnCompleteListener { tarea ->
                    if (tarea.isSuccessful) {
                        // ¡Login correcto! Avisamos a nuestro cerebro
                        authViewModel.actualizarUsuario(tarea.result?.user)
                    }
                }
        } catch (e: Exception) {
            // Si el usuario cierra la ventanita sin elegir cuenta, no pasa nada
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Lista Compartida",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Inicia sesión para sincronizar tus compras",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val opcionesGoogle = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    // Esta línea roja mágica la genera Google automáticamente al compilar
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                val clienteGoogle = GoogleSignIn.getClient(context, opcionesGoogle)
                launcher.launch(clienteGoogle.signInIntent)
            },
            modifier = Modifier.height(50.dp)
        ) {
            Text(text = "Entrar con Google", fontSize = 16.sp)
        }
    }
}