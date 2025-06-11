package com.rafi607062330092.assesment3.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafi607062330092.assesment3.model.Buku
import com.rafi607062330092.assesment3.model.BukuStatus
import com.rafi607062330092.assesment3.network.ApiStatus
import com.rafi607062330092.assesment3.network.BukuApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {
    var data = mutableStateOf<BukuStatus?>(null)
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData() {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = BukuApi.service.getBuku()
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    suspend fun register(nama: String, email: String, password: String): String {
        var token = ""
        try {
            val result = BukuApi.service.postRegister(
                nama,
                email,
                password
            )

            if (result.success) {
                token = result.data ?: ""
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failure: ${e.message}")
        }

        return token
    }

    fun saveData(token: String, judul: String, penulis: String, penerbit: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = BukuApi.service.postBuku(
                    token,
                    judul.toRequestBody("text/plain".toMediaTypeOrNull()),
                    penulis.toRequestBody("text/plain".toMediaTypeOrNull()),
                    penerbit.toRequestBody("text/plain".toMediaTypeOrNull()),
                    bitmap.toMultipartBody()
                )

                if (result.success)
                    retrieveData()
                else
                    throw Exception(result.message)
            } catch (e: Exception) {
                errorMessage.value = e.message
                Log.d("MainViewModel", "Failure: ${e.message}")
            }
        }
    }

    fun deleteData(userId: String, id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = BukuApi.service.deleteBuku(
                    userId,
                    id
                )

                if (result.success)
                    retrieveData()
                else
                    throw Exception(result.message)
            } catch (e: Exception) {
                errorMessage.value = e.message
                Log.d("MainViewModel", "Failure: ${e.message}")
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size)
        return MultipartBody.Part.createFormData(
            "image","image.jpg", requestBody
        )
    }

    fun clearMessage() { errorMessage.value = null }
}