package com.rafi607062330092.assesment3.ui.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.rafi607062330092.assesment3.BuildConfig
import com.rafi607062330092.assesment3.R
import com.rafi607062330092.assesment3.model.Buku
import com.rafi607062330092.assesment3.model.User
import com.rafi607062330092.assesment3.network.ApiStatus
import com.rafi607062330092.assesment3.network.BukuApi
import com.rafi607062330092.assesment3.network.UserDataStore
import com.rafi607062330092.assesment3.ui.theme.Mobpro1Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showBukuDialog by remember { mutableStateOf(false) }
    var showHapusDialog by remember { mutableStateOf(false) }
    var hapusID by remember { mutableLongStateOf(0L) }

    Mobpro1Theme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                id = R.string.app_name
                            )
                        )

                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(
                            onClick = {
                                if (user.token.isEmpty()) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        signIn(viewModel, context, dataStore)
                                    }
                                } else {
                                    showDialog = true
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.account_circle_24),
                                contentDescription = stringResource(R.string.profil),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.retrieveData(user.token)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_refresh_24),
                                contentDescription = stringResource(R.string.refresh),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (user.token.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = {
                            showBukuDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.tambah_buku)
                        )
                    }
                }
            }
        ) { innerPadding ->
            ScreenContent(viewModel, user.token,
                {
                    id ->
                    hapusID = id
                    showHapusDialog = true
                },
                Modifier.padding(innerPadding)
            )

            if (showDialog) {
                ProfilDialog(
                    user = user,
                    onDismissRequest = { showDialog = false }
                ) { CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                    showDialog = false
                }
            }

            if (showBukuDialog) {
                BukuDialog(
                    onDismissRequest = { showBukuDialog = false }
                ) {
                    judul, penulis, penerbit, bitmap ->
                    showBukuDialog = false
                    viewModel.saveData(user.token, judul, penulis, penerbit, bitmap!!)
                }
            }

            if (showHapusDialog) {
                HapusDialog(
                    onDismissRequest = { showHapusDialog = false }
                ) {
                    viewModel.deleteData(user.token, hapusID)
                    Toast.makeText(context, context.getString(R.string.terhapus), Toast.LENGTH_LONG).show()
                    showHapusDialog = false
                }
            }

            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }
    }
}

@Composable
fun ScreenContent(viewModel: MainViewModel, token: String, onHapus: (id: Long) -> Unit, modifier: Modifier = Modifier) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()
    var showDetailDialog by remember { mutableStateOf<Buku?>(null) }

    LaunchedEffect(token) {
        viewModel.retrieveData(token)
    }

    if (showDetailDialog != null) {
        BukuDialog (
            buku = showDetailDialog!!,
            onDismissRequest = { showDetailDialog = null },
            onConfirmation = { judul, penulis, penerbit, bitmap ->
                viewModel.updateData(token, showDetailDialog!!.id_buku, judul, penulis, penerbit, bitmap)
                showDetailDialog = null
            }
        )
    }

    when (status) {
        ApiStatus.LOADING -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ApiStatus.SUCCESS -> {
                LazyVerticalGrid(
                    modifier = modifier.fillMaxSize().padding(4.dp),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    data?.let { it ->
                        items(it.data) {
                            ListItem(buku = it, onHapus) {
                                showDetailDialog = it
                            }
                        }
                    }
                }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.error),
                )
                Button(
                    onClick = { viewModel.retrieveData(token) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}

@Composable
fun ListItem(buku: Buku, onHapus: (id : Long) -> Unit, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier.padding(4.dp).border(1.dp, Color.Gray).clickable {
            if (buku.mine == "1") {
                onClick()
            }
        },
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    BukuApi.getImageUrl(buku.id_buku)
                )
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gambar, buku.judul),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.broken_img),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
                .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                .padding(4.dp)
        ) {
            Column{
                Text(
                    text = buku.judul,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = buku.penulis,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = buku.penerbit,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            if (buku.mine == "1") {
                IconButton(
                    onClick = {
                        onHapus(buku.id_buku)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.hapus)
                    )
                }
            }
        }
    }
}

private suspend fun signIn(viewModel: MainViewModel, context: Context, dataStore: UserDataStore) {
    val googleIdOption : GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(viewModel, result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    viewModel: MainViewModel,
    result: GetCredentialResponse,
    dataStore: UserDataStore
) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            val token = googleId.idToken
            if (token.isNotEmpty()) {
                val tokenSanctum = viewModel.register(nama, email, token)

                if (tokenSanctum.isEmpty()) {
                    Log.e("SIGN-IN", "Error: registration failed")
                    return
                }

                dataStore.saveData(
                    User(
                        token = "Bearer $tokenSanctum",
                        name = nama,
                        email = email,
                        photoUrl = photoUrl
                    )
                )
                Log.d("SIGN-IN", "Success: $nama, $email, $photoUrl, $tokenSanctum")
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Mobpro1Theme {
        MainScreen()
    }
}