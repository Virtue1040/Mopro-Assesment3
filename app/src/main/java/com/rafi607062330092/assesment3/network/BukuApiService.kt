package com.rafi607062330092.assesment3.network

import com.rafi607062330092.assesment3.model.Buku
import com.rafi607062330092.assesment3.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private const val BASE_URL = "https://gh.d3ifcool.org/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface BukuApiService {
    @GET("buku")
    suspend fun getBuku(
        @Header("Authorization") token: String
    ): List<Buku>

    @Multipart
    @POST("buku")
    suspend fun postBuku(
        @Header("Authorization") token: String,
        @Part("nama") nama: RequestBody,
        @Part("namaLatin") namaLatin: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus

    @DELETE("buku")
    suspend fun deleteBuku(
        @Header("Authorization") token: String,
        @Query("id") id: String,
    ): OpStatus

    @POST("register")
    suspend fun postRegister(
        @Part("nama") nama: RequestBody,
        @Part("email") namaLatin: RequestBody,
        @Part("password") idToken: RequestBody,
    ): OpStatus
}

object BukuApi {
    val service: BukuApiService by lazy {
        retrofit.create(BukuApiService::class.java)
    }

    fun getBukuUrl(imageId: String): String {
        return "${BASE_URL}image.php?id=$imageId"
    }
}

enum class ApiStatus {
    LOADING,
    SUCCESS,
    FAILED
}