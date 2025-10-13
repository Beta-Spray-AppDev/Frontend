package com.example.sprayconnectapp.network


import kotlinx.coroutines.runBlocking
import android.content.Context
import android.util.Log
import com.example.sprayconnectapp.data.dto.RefreshRequest
import com.example.sprayconnectapp.data.dto.TokenResponse
import com.example.sprayconnectapp.util.TokenStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


import retrofit2.http.GET


/**
 * Zentrale Stelle zum Erzeugen/Zurückgeben von Retrofit-Instanzen und API-Interfaces.
 * - Fügt Logging hinzu
 * - Hängt bei privaten Endpunkten automatisch den Bearer-Token an
 */


object RetrofitInstance {


    private const val BASE_URL = "http://leitln.at:8090/"


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
            level = HttpLoggingInterceptor.Level.NONE
        }

        val store = TokenStore.create(context)

        val authApi: AuthApi = buildAuthOnlyRetrofit(logger).create(AuthApi::class.java)



        // Client konfigurieren
        val client = OkHttpClient.Builder()
          //  .addInterceptor(logger) war zum loggen
            // Eigener Interceptor für Auth:
            // entscheidet, ob ein Authorization-Header angehängt wird
            // liest den Token pro Request frisch aus SharedPreferences
            .addInterceptor(AuthInterceptor(store))
            .authenticator(TokenAuthenticator(authApi, store))
            .build()


        val built = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create()) // falls einige Endpoints String liefern
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        retrofit = built
        return built
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

    fun getFeedbackApi(context: Context): FeedbackApi =
        getRetrofit(context).create(FeedbackApi::class.java)

    fun getCommentApi(context: Context): CommentApi {
        return getRetrofit(context).create(CommentApi::class.java)
    }




    private fun buildAuthOnlyRetrofit(logger: HttpLoggingInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
          //  .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }






    /** Hängt Bearer <access> an alle nicht-öffentlichen Requests */
    class AuthInterceptor(private val store: TokenStore) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val req = chain.request()

            val path = req.url.encodedPath // beginnt immer mit '/'
            // Alles unter /auth/* ist öffentlich (login/register/refresh/logout)
            val isPublic = path == "/auth/login" ||
                    path == "/auth/register" ||
                    path == "/auth/refresh" ||
                    path == "/auth/logout"
            val b = req.newBuilder()
            if (!isPublic) {
                store.accessToken()?.let { b.header("Authorization", "Bearer $it") }
            }
            // (Optional) Geräte-ID mitschicken:
            // b.header("X-Device-Id", <persisted-uuid>)
            return chain.proceed(b.build())
        }
    }

    /** Führt bei 401 automatisch einen Refresh durch und wiederholt den Request */
    class TokenAuthenticator(
        private val authApi: AuthApi,
        private val store: TokenStore
    ) : Authenticator {

        private val mutex = Mutex()

        override fun authenticate(route: Route?, response: Response): Request? {
            // Endlosschleifen vermeiden
            if (response.priorResponse != null) return null

            return runBlocking {
                mutex.withLock {
                    // wurde woanders schon refreshed?
                    val currentAccess = store.accessToken()
                    val failedAccess = response.request.header("Authorization")?.removePrefix("Bearer ")
                    if (!currentAccess.isNullOrBlank() && currentAccess != failedAccess) {
                        return@withLock response.request.newBuilder()
                            .header("Authorization", "Bearer $currentAccess")
                            .build()
                    }

                    val rt = store.refreshToken() ?: return@withLock null


                    Log.d("Auth", "401 erhalten -> versuche Refresh")
                    // WICHTIG: KEIN .execute() bei suspend-Funktionen!
                    val refreshResp = try {
                        authApi.refresh(RefreshRequest(rt))   // suspend -> wir sind in runBlocking
                    } catch (e: Exception) {
                        Log.e("Auth", "Refresh-Call fehlgeschlagen: ${e.localizedMessage}")
                        null
                    }

                    if (refreshResp == null || !refreshResp.isSuccessful || refreshResp.body() == null) {
                        Log.w("Auth", "Refresh ungültig -> logout")
                        store.clear() // Refresh fehlgeschlagen -> ausloggen
                        return@withLock null
                    }

                    val tokens = refreshResp.body()!!
                    Log.d("Auth", "Refresh OK -> neuen Access gesetzt & Request wiederholen")

                    store.save(tokens.accessToken, tokens.refreshToken)

                    // ursprünglichen Request mit neuem Access wiederholen
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${tokens.accessToken}")
                        .build()
                }
            }
        }

    }











}