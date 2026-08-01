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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.javirmnn.listacompra.data.ListaCompartida
import com.javirmnn.listacompra.data.Producto
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

    var expandedMenuId by remember { mutableStateOf<String?>(null) }
    var showRenameDialogFor by remember { mutableStateOf<ListaCompartida?>(null) }
    var showDeleteDialogFor by remember { mutableStateOf<ListaCompartida?>(null) }

    var showEditProductDialogFor by remember { mutableStateOf<Producto?>(null) }
    var showDeleteProductDialogFor by remember { mutableStateOf<Producto?>(null) }

    var joinIntentosFallidos by remember { mutableStateOf(0) }
    var joinBloqueado by remember { mutableStateOf(false) }
    var joinSegundosRestantes by remember { mutableStateOf(0) }
    var joinPenalizacion by remember { mutableStateOf(30) }
    var joinErrorMensaje by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }

    LaunchedEffect(joinBloqueado) {
        if (joinBloqueado) {
            joinSegundosRestantes = joinPenalizacion
            while (joinSegundosRestantes > 0) {
                delay(1000)
                joinSegundosRestantes--
            }
            joinBloqueado = false
            joinPenalizacion *= 2
            joinErrorMensaje = ""
        }
    }

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
                                            text = { Text("Editar") },
                                            onClick = {
                                                showRenameDialogFor = lista
                                                expandedMenuId = null
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
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
                        // AQUÍ ESTÁ EL CAMBIO: Ya no cerramos el menú lateral
                        showCreateListDialog = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Unirse a una lista") },
                    selected = false,
                    onClick = {
                        // AQUÍ ESTÁ EL CAMBIO: Ya no cerramos el menú lateral
                        showJoinListDialog = true
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
                    title = { Text(listaActiva?.nombre ?: "Elige una lista") },
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
                        Text("Abre el menú para empezar.")
                    }
                } else {
                    val productosParaComprar = productos
                        .filter { it.seleccionado }
                        .sortedBy { it.nombre.lowercase() }

                    val productosBase = productos
                        .filter { !it.seleccionado }
                        .sortedBy { it.nombre.lowercase() }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = "PARA COMPRAR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        if (productosParaComprar.isEmpty()) {
                            item {
                                Text(
                                    text = "Vacío",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        } else {
                            items(productosParaComprar) { producto ->
                                ProductoItem(
                                    producto = producto,
                                    onCheckedChange = { viewModel.cambiarEstado(producto) },
                                    onUpdateCantidad = { nuevaCant -> viewModel.actualizarCantidad(producto, nuevaCant) },
                                    onUpdateComentario = { nuevoComent -> viewModel.actualizarComentario(producto, nuevoComent) },
                                    onEditClick = { showEditProductDialogFor = producto },
                                    onDeleteClick = { showDeleteProductDialogFor = producto }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "PRODUCTOS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        if (productosBase.isEmpty()) {
                            item {
                                Text(
                                    text = "Vacío",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        } else {
                            items(productosBase) { producto ->
                                ProductoItem(
                                    producto = producto,
                                    onCheckedChange = { viewModel.cambiarEstado(producto) },
                                    onUpdateCantidad = { nuevaCant -> viewModel.actualizarCantidad(producto, nuevaCant) },
                                    onUpdateComentario = { nuevoComent -> viewModel.actualizarComentario(producto, nuevoComent) },
                                    onEditClick = { showEditProductDialogFor = producto },
                                    onDeleteClick = { showDeleteProductDialogFor = producto }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onConfirm = { nombreProducto ->
                viewModel.agregarProducto(nombreProducto, miAlias)
                showAddProductDialog = false
            }
        )
    }

    if (showEditProductDialogFor != null) {
        var editProductName by remember {
            mutableStateOf(
                TextFieldValue(
                    text = showEditProductDialogFor!!.nombre,
                    selection = TextRange(showEditProductDialogFor!!.nombre.length)
                )
            )
        }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { showEditProductDialogFor = null },
            title = { Text("Editar Producto") },
            text = {
                OutlinedTextField(
                    value = editProductName,
                    onValueChange = { editProductName = it },
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editProductName.text.isNotBlank()) {
                        viewModel.cambiarNombreProducto(showEditProductDialogFor!!.id, editProductName.text.trim())
                        showEditProductDialogFor = null
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProductDialogFor = null }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteProductDialogFor != null) {
        AlertDialog(
            onDismissRequest = { showDeleteProductDialogFor = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Eliminar '${showDeleteProductDialogFor!!.nombre}' de la lista?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(showDeleteProductDialogFor!!.id)
                    showDeleteProductDialogFor = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProductDialogFor = null }) { Text("Cancelar") }
            }
        )
    }

    if (showCreateListDialog) {
        var newListName by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { showCreateListDialog = false },
            title = { Text("Crear Lista") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Nombre de la lista") },
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
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
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { showJoinListDialog = false },
            title = { Text("Unirse a Lista") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = {
                            if (it.length <= 8) inviteCode = it.uppercase()
                        },
                        label = { Text("Código de 8 caracteres") },
                        singleLine = true,
                        enabled = !joinBloqueado,
                        isError = joinErrorMensaje.isNotEmpty(),
                        modifier = Modifier.focusRequester(focusRequester)
                    )

                    if (joinErrorMensaje.isNotEmpty()) {
                        Text(
                            text = joinErrorMensaje,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (joinBloqueado) {
                        Text(
                            text = "Espera $joinSegundosRestantes s.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !joinBloqueado && inviteCode.length == 8,
                    onClick = {
                        viewModel.unirseALista(inviteCode) { exito ->
                            if (exito) {
                                showJoinListDialog = false
                                joinIntentosFallidos = 0
                                inviteCode = ""
                            } else {
                                joinIntentosFallidos++
                                if (joinIntentosFallidos >= 5) {
                                    joinBloqueado = true
                                } else {
                                    joinErrorMensaje = "Código incorrecto"
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

    if (showRenameDialogFor != null) {
        var nuevoNombre by remember {
            mutableStateOf(
                TextFieldValue(
                    text = showRenameDialogFor!!.nombre,
                    selection = TextRange(showRenameDialogFor!!.nombre.length)
                )
            )
        }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { showRenameDialogFor = null },
            title = { Text("Editar Nombre") },
            text = {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevoNombre.text.isNotBlank()) {
                        viewModel.cambiarNombreLista(showRenameDialogFor!!.id, nuevoNombre.text.trim())
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
            text = { Text("¿Eliminar la lista '${showDeleteDialogFor!!.nombre}'?") },
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