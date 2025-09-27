package com.example.sprayconnectapp.ui.screens.Profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.UpdateProfileRequest
import com.example.sprayconnectapp.data.dto.UserProfile
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * ViewModel für Profil:
 * - lädt/aktualisiert UserProfile
 * - lädt eigene Boulder & Ticks
 * - Logout (Token löschen + Retrofit reset)
 */

class ProfileViewModel : ViewModel() {

    private val session = SessionManager()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _myBoulders = MutableStateFlow<List<BoulderDTO>>(emptyList())
    val myBoulders: StateFlow<List<BoulderDTO>> = _myBoulders


    private val _myTicks = MutableStateFlow<List<BoulderDTO>>(emptyList())
    val myTicks: StateFlow<List<BoulderDTO>> = _myTicks


    private val _isDeletingTicks = MutableStateFlow(false)
    val isDeletingTicks: StateFlow<Boolean> = _isDeletingTicks


    private val _isDeletingBoulders = MutableStateFlow(false)
    val isDeletingBoulders: StateFlow<Boolean> = _isDeletingBoulders



    /** Löscht mehrere eigene Boulder am Server und refresht Listen. */
    fun deleteBoulders(context: Context, boulderIds: List<String>, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isDeletingBoulders.value = true
            try {
                val api = RetrofitInstance.getBoulderApi(context)
                boulderIds.forEach { id ->
                    try { api.deleteBoulder(id) } catch (_: Exception) { /* optional: log */ }
                }
                // danach neu laden (Boulder weg, evtl. auch Tick-Liste ändern)
                loadMyBoulders(context)
                loadMyTicks(context)
            } finally {
                _isDeletingBoulders.value = false
                onDone()
            }
        }
    }


    /** Löscht Ticks (mehrere Boulder-IDs) beim Backend und refresht die Tick-Liste. */
    fun deleteTicks(context: Context, boulderIds: List<String>, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isDeletingTicks.value = true
            try {
                val api = RetrofitInstance.getBoulderApi(context)
                // nacheinander; reicht ohne Bulk
                boulderIds.forEach { id ->
                    try { api.deleteTick(id) } catch (_: Exception) {}
                }
                // danach neu laden
                loadMyTicks(context)
                // optional: falls du irgendwo Tick-Status in "Meine Boulder" zeigst:
                // loadMyBoulders(context)
            } catch (e: Exception) {
                Log.e("ProfileVM", "Ticks löschen fehlgeschlagen: ${e.localizedMessage}")
            } finally {
                _isDeletingTicks.value = false
                onDone()
            }
        }
    }



    /** Holt das Profil des aktuellen Nutzers. */
    fun loadProfile(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.getApi(context).getProfile()
                if (response.isSuccessful) {
                    _profile.value = response.body()
                } else {
                    _error.value = "Fehler: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Netzwerkfehler: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }




    /**
     * Aktualisiert Profilfelder.
     * - Leerstring → wird zu null gemappt (Server: Feld nicht ändern)
     * - onSuccess/onError für UI-Feedback
     */

    fun updateProfile(
        context: Context,
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val dto = UpdateProfileRequest(
                    username = username.ifBlank { null },
                    email = email.ifBlank { null },
                    password = password.ifBlank { null }
                )


                val response = RetrofitInstance.getApi(context).updateProfile(dto)
                val errorBody = response.errorBody()?.string()

                if (response.isSuccessful) {
                    _profile.value = response.body()
                    onSuccess()
                }

                else{
                    when (errorBody) {

                        "username_taken" -> onError("Benutzername ist bereits vergeben.")
                        "email_taken" -> onError("E-Mail ist bereits vergeben.")
                        else -> onError("Fehler: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                onError("Fehler: ${e.localizedMessage}")
            }
        }
    }




    /** Lädt eigene Boulder. */
    fun loadMyBoulders(context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getBoulderApi(context).getMyBoulders()
                if (response.isSuccessful) {
                    _myBoulders.value = response.body() ?: emptyList()
                } else {
                    Log.e("ProfileVM", "Fehler beim Laden der Boulder: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Netzwerkfehler: ${e.localizedMessage}")
            }
        }
    }


    /** Lädt getickte Boulder. */
    fun loadMyTicks(context: Context) {
        viewModelScope.launch {
            try {
                val res = RetrofitInstance.getBoulderApi(context).getMyTickedBoulders()
                if (res.isSuccessful) _myTicks.value = res.body() ?: emptyList()
            } catch (_: Exception) { }
        }
    }



    /** Logout: Token löschen, Retrofit-Instanz verwerfen. */
    fun logout(context: Context) = session.logout(context)




}
