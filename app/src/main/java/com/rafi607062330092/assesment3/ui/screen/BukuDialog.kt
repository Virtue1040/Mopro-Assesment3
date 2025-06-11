package com.rafi607062330092.assesment3.ui.screen

import android.content.ContentResolver
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.rafi607062330092.assesment3.R
import com.rafi607062330092.assesment3.model.Buku
import com.rafi607062330092.assesment3.network.BukuApi
import com.rafi607062330092.assesment3.ui.theme.Mobpro1Theme

@Composable
fun BukuDialog(
    buku: Buku? = null,
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String, String, Bitmap?) -> Unit
) {
    val context = LocalContext.current

    var judul by remember { mutableStateOf("") }
    var penulis by remember { mutableStateOf("") }
    var penerbit by remember { mutableStateOf("") }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
    }

    if (buku != null) {
        judul = buku.judul
        penulis = buku.penulis
        penerbit = buku.penerbit
    }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (buku != null && bitmap == null) {
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
                } else {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        val options = CropImageContractOptions(
                            null, CropImageOptions(
                                imageSourceIncludeGallery = false,
                                imageSourceIncludeCamera = true,
                                fixAspectRatio = true
                            )
                        )
                        launcher.launch(options)
                    },
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.pilih_gambar)
                    )
                }
                OutlinedTextField(
                    value = judul,
                    onValueChange = {
                        judul = it
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.judul)
                        )
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = penulis,
                    onValueChange = {
                        penulis = it
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.penulis)
                        )
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = penerbit,
                    onValueChange = {
                        penerbit = it
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.penerbit)
                        )
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.batal)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            onConfirmation(judul, penulis, penerbit, bitmap)
                        },
                        enabled = judul.isNotEmpty() && penulis.isNotEmpty() && penerbit.isNotEmpty() && (buku != null || bitmap != null),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.simpan)
                        )
                    }
                }
            }
        }
    }
}


@Suppress("deprecation")
private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun AddDialogPreview() {
    Mobpro1Theme {
        BukuDialog (
            buku = null,
            onDismissRequest = {},
            onConfirmation = { _,_,_,_ ->}
        )
    }
}