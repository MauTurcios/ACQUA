package com.sismantec.acqua.apiservices

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

object RetrofitCliente {

    private var retrofit: Retrofit? = null
    private var ultimaBaseUrl: String? = null

    fun obtenerApi(baseUrl: String): APIServices {
        // Aseguramos que la URL termine con "/"
        val urlFinal = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        // Si cambia la base URL, recreamos el retrofit
        if (retrofit == null || ultimaBaseUrl != urlFinal) {
            ultimaBaseUrl = urlFinal

            // Interceptor para log de peticiones
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(urlFinal)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        return retrofit!!.create(APIServices::class.java)
    }

}