package com.example.sprayconnectapp.network

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.scalars.ScalarsConverterFactory

import com.example.sprayconnectapp.util.getTokenFromPrefs
import retrofit2.http.GET


object RetrofitInstance {

    private var retrofit: Retrofit? = null

    fun resetRetrofit() {
        retrofit = null
    }


    // Übergabe des Contexts für Zugriff auf SharedPreferences
    fun getRetrofit(context: Context): Retrofit {

        if (retrofit != null) return retrofit!!

        val token = getTokenFromPrefs(context)

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                //Token nicht mitschicken bei auth endpoint
                val publicEndpoints = listOf("/auth/login", "/auth/register")
                val isPublic = publicEndpoints.any { original.url.encodedPath.startsWith(it) }

                if(!isPublic){

                    val freshToken = getTokenFromPrefs(context)
                    // Wenn Token vorhanden: Authorization-Header hinzufügen
                    freshToken?.let {
                        requestBuilder.addHeader("Authorization", "Bearer $it")
                    }
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("http://leitln.at:8090/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun getApi(context: Context): AuthApi {
        return getRetrofit(context).create(AuthApi::class.java)
    }

    fun getGymApi(context: Context): GymApi {
        return getRetrofit(context).create(GymApi::class.java)
    }

    fun getSpraywallApi(context: Context): SpraywallApi {
        return getRetrofit(context).create(SpraywallApi::class.java)
    }
    fun getBoulderApi(context: Context): BoulderApi {
        return getRetrofit(context).create(BoulderApi::class.java)
    }

    fun getFeedbackApi(context: Context): FeedbackApi =
        getRetrofit(context).create(FeedbackApi::class.java)












}