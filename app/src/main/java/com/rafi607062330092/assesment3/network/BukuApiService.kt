package com.rafi607062330092.assesment3.network

import com.rafi607062330092.assesment3.model.BukuStatus
import com.rafi607062330092.assesment3.model.OpStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


private const val BASE_URL = "https://buku-api.michael-kaiser.my.id/api/"

private val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface BukuApiService {
    @GET("buku")
    suspend fun getBuku(
        @Header("Authorization") token: String
    ): BukuStatus

    @Multipart
    @POST("buku")
    suspend fun postBuku(
        @Header("Authorization") token: String,
        @Part("judul") judul: RequestBody,
        @Part("penulis") penulis: RequestBody,
        @Part("penerbit") penerbit: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus

    @Multipart
    @POST("buku/{id_buku}")
    suspend fun updateBuku(
        @Header("Authorization") token: String,
        @Part("_method") method: RequestBody,
        @Path("id_buku") id_buku: Long,
        @Part("judul") judul: RequestBody,
        @Part("penulis") penulis: RequestBody,
        @Part("penerbit") penerbit: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): OpStatus

    @DELETE("buku/{id_buku}")
    suspend fun deleteBuku(
        @Header("Authorization") token: String,
        @Path("id_buku") id_buku: Long
    ): OpStatus

    @FormUrlEncoded
    @POST("register")
    suspend fun postRegister(
        @Field("name") nama: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): OpStatus
}

object BukuApi {
    val service: BukuApiService by lazy {
        retrofit.create(BukuApiService::class.java)
    }

    fun getImageUrl(id: Long): String {
        return "${BASE_URL}buku/image/$id"
    }
}

enum class ApiStatus {
    LOADING,
    SUCCESS,
    FAILED
}