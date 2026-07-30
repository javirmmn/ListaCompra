package com.javirmnn.listacompra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.javirmnn.listacompra.data.ListaCompartida
import com.javirmnn.listacompra.viewmodel.ListaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ListaViewModel) {
    val listas by viewModel.misListas.collectAsState()
    val listaActiva by viewModel.listaActiva.collectAsState()
    val productos by viewModel.productos.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showJoinListDialog by remember { mutableStateOf(false) }

    // Controladores del menú de los 3 puntos
    var expandedMenuId by remember { mutableStateOf<String?>(null) }
    var showRenameDialogFor by remember { mutableStateOf<ListaCompartida?>(null) }
    var showDeleteDialogFor by remember { mutableStateOf<ListaCompartida?>(null) }

    var miAlias by remember { mutableStateOf("") }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    miAlias = doc.getString("alias") ?: "Usuario"
                }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mis Listas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(listas) { lista ->
                        NavigationDrawerItem(
                            label = { Text(lista.nombre) },
                            selected = lista.id == listaActiva?.id,
                            onClick = {
                                viewModel.seleccionarLista(lista)
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                Box {
                                    IconButton(onClick = { expandedMenuId = lista.id }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                    }
                                    DropdownMenu(
                                        expanded = expandedMenuId == lista.id,
                                        onDismissRequest = { expandedMenuId = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Cambiar Nombre") },
                                            onClick = {
                                                showRenameDialogFor = lista
                                                expandedMenuId = null
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar") },
                                            onClick = {
                                                showDeleteDialogFor = lista
                                                expandedMenuId = null
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }

                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Crear nueva lista") },
                    selected = false,
                    onClick = {
                        showCreateListDialog = true
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Unirse a una lista") },
                    selected = false,
                    onClick = {
                        showJoinListDialog = true
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(listaActiva?.nombre ?: "Cargando...") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        if (listaActiva != null) {
                            var showCodeDialog by remember { mutableStateOf(false) }

                            IconButton(onClick = { showCodeDialog = true }) {
                                Icon(Icons.Default.Share, contentDescription = "Invitar")
                            }

                            if (showCodeDialog) {
                                AlertDialog(
                                    onDismissRequest = { showCodeDialog = false },
                                    title = { Text("Invitar a alguien") },
                                    text = { Text("Pásale este código secreto a quien quieras invitar:\n\n${listaActiva!!.codigoInvitacion}") },
                                    confirmButton = {
                                        TextButton(onClick = { showCodeDialog = false }) { Text("Entendido") }
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                if (listaActiva != null) {
                    FloatingActionButton(onClick = { showAddProductDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir")
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (listaActiva == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes ninguna lista. Abre el menú para crear una.")
                    }
                } else {
                    val productosParaComprar = productos.filter { it.seleccionado }
                    val productosBase = productos.filter { !it.seleccionado }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        if (productosParaComprar.isNotEmpty()) {
                            item {
                                Text(
                                    text = "PARA COMPRAR",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(productosParaComprar) { producto ->
                                ProductoItem(
                                    producto = producto,
                                    onCheckedChange = { viewModel.cambiarEstado(producto) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (productosBase.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "PRODUCTOS",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(productosBase) { producto ->
                                ProductoItem(
                                    producto = producto,
                                    onCheckedChange = { viewModel.cambiarEstado(producto) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (productosParaComprar.isEmpty() && productosBase.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("La lista está vacía", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS EMERGENTES DE PRODUCTOS Y LISTAS ---

    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onConfirm = { nombreProducto ->
                viewModel.agregarProducto(nombreProducto, miAlias)
                showAddProductDialog = false
            }
        )
    }

    if (showCreateListDialog) {
        var newListName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateListDialog = false },
            title = { Text("Crear Lista") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Nombre de la lista (ej: Compra Casa)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newListName.isNotBlank()) {
                        viewModel.crearNuevaLista(newListName)
                        showCreateListDialog = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateListDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showJoinListDialog) {
        var inviteCode by remember { mutableStateOf("") }
        var intentosFallidos by remember { mutableStateOf(0) }
        var bloqueado by remember { mutableStateOf(false) }
        var segundosRestantes by remember { mutableStateOf(0) }
        var errorMensaje by remember { mutableStateOf("") }

        LaunchedEffect(bloqueado) {
            if (bloqueado) {
                segundosRestantes = 30
                while (segundosRestantes > 0) {
                    delay(1000)
                    segundosRestantes--
                }
                bloqueado = false
                intentosFallidos = 0
                errorMensaje = ""
            }
        }

        AlertDialog(
            onDismissRequest = { showJoinListDialog = false },
            title = { Text("Unirse a Lista") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it.uppercase() },
                        label = { Text("Código de 8 caracteres") },
                        singleLine = true,
                        enabled = !bloqueado,
                        isError = errorMensaje.isNotEmpty()
                    )

                    if (errorMensaje.isNotEmpty()) {
                        Text(
                            text = errorMensaje,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (bloqueado) {
                        Text(
                            text = "Demasiados intentos. Espera $segundosRestantes s.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !bloqueado && inviteCode.isNotBlank(),
                    onClick = {
                        viewModel.unirseALista(inviteCode) { exito ->
                            if (exito) {
                                showJoinListDialog = false
                            } else {
                                intentosFallidos++
                                if (intentosFallidos >= 5) {
                                    bloqueado = true
                                } else {
                                    errorMensaje = "Código incorrecto. Intentos: $intentosFallidos/5"
                                }
                            }
                        }
                    }
                ) { Text("Unirse") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinListDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // --- DIÁLOGOS DE EDICIÓN DE LA LISTA (Renombrar / Eliminar) ---

    if (showRenameDialogFor != null) {
        var nuevoNombre by remember { mutableStateOf(showRenameDialogFor!!.nombre) }
        AlertDialog(
            onDismissRequest = { showRenameDialogFor = null },
            title = { Text("Cambiar Nombre") },
            text = {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevoNombre.isNotBlank()) {
                        viewModel.cambiarNombreLista(showRenameDialogFor!!.id, nuevoNombre.trim())
                        showRenameDialogFor = null
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialogFor = null }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteDialogFor != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialogFor = null },
            title = { Text("Eliminar Lista") },
            text = { Text("¿Estás seguro de que quieres eliminar la lista '${showDeleteDialogFor!!.nombre}'? Esta acción no se puede deshacer y afectará a todos los miembros.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarLista(showDeleteDialogFor!!.id)
                    showDeleteDialogFor = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogFor = null }) { Text("Cancelar") }
            }
        )
    }
}