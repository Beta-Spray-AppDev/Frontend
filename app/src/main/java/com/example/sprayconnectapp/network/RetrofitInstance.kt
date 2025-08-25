package com.example.sprayconnectapp.network

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.scalars.ScalarsConverterFactory

import com.example.sprayconnectapp.util.getTokenFromPrefs
import retrofit2.http.GET


/**
 * Zentrale Stelle zum Erzeugen/Zurückgeben von Retrofit-Instanzen und API-Interfaces.
 * - Fügt Logging hinzu
 * - Hängt bei privaten Endpunkten automatisch den Bearer-Token an
 */


object RetrofitInstance {


    private var retrofit: Retrofit? = null

    /** Setzt die gecachte Retrofit-Instanz zurück (nach Logout) */
    fun resetRetrofit() {
        retrofit = null
    }


    /**
     * Baut eine Retrofit-Instanz mit Auth-/Logging-Interceptors.
     * Token wird pro Request frisch aus SharedPreferences gelesen.
     */
    fun getRetrofit(context: Context): Retrofit {


        // wenn schon gebaut
        if (retrofit != null) return retrofit!!


        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Client konfigurieren
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)

            // Eigener Interceptor für Auth:
            // entscheidet, ob ein Authorization-Header angehängt wird
            // liest den Token pro Request frisch aus SharedPreferences
            .addInterceptor { chain ->
                val original = chain.request() // holt aktuelle Anfrage
                val requestBuilder = original.newBuilder() // builder um header zu ändern

                //Token nicht mitschicken bei auth endpoint
                val publicEndpoints = listOf("/auth/login", "/auth/register")
                val isPublic = publicEndpoints.any { original.url.encodedPath.startsWith(it) }

                if(!isPublic){

                    val freshToken = getTokenFromPrefs(context) // Token wird pro request frisch gelesen - nicht im Client cachen
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
            .addConverterFactory(ScalarsConverterFactory.create()) // für string responses
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }


    // Factory-Methoden für die API-Interfaces

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










}