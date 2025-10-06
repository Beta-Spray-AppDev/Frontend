package com.example.sprayconnectapp.ui.screens.BoulderView

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.dto.BoulderDTO
import com.example.sprayconnectapp.data.dto.CreateBoulderRequest
import com.example.sprayconnectapp.data.dto.Hold
import com.example.sprayconnectapp.data.dto.HoldType
import com.example.sprayconnectapp.data.dto.TickCreateRequest
import com.example.sprayconnectapp.data.local.AppDatabase
import com.example.sprayconnectapp.data.repository.CommentRepository
import com.example.sprayconnectapp.data.repository.BoulderRepository
import com.example.sprayconnectapp.network.RetrofitInstance
import com.example.sprayconnectapp.ui.screens.isOnline
import kotlinx.coroutines.launch
import java.util.*

class CreateBoulderViewModel : ViewModel() {

    private val _uiState = mutableStateOf(CreateBoulderUiState())
    val uiState: State<CreateBoulderUiState> = _uiState

    private val _boulders = mutableStateOf<List<BoulderDTO>>(emptyList())
    val boulders: State<List<BoulderDTO>> = _boulders

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private lateinit var repo: BoulderRepository

    private val commentRepo = CommentRepository()

    var commentSending by mutableStateOf(false); private set
    var commentError by mutableStateOf<String?>(null); private set
    var commentResult by mutableStateOf<Boolean?>(null); private set


    /** Lazy-Initialisierung des Repositories (DAOs aus Room holen) */
    fun initRepository(context: Context) {
        if (::repo.isInitialized) return
        val db = AppDatabase.getInstance(context)
        repo = BoulderRepository(db.boulderDao(), db.holdDao())
    }
    private fun ensureRepo(context: Context) { if (!::repo.isInitialized) initRepository(context) }

    /** Aktiven Hold-Typ (Farbe) wechseln */

    fun selectHoldType(type: HoldType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }


    /** Hold an normierter Position [0..1] hinzufügen */
    fun addHoldNorm(nx: Float, ny: Float) {
        val newHold = Hold(
            id = UUID.randomUUID().toString(),
            x = nx, y = ny,
            type = _uiState.value.selectedType.name
        )
        _uiState.value = _uiState.value.copy(holds = _uiState.value.holds + newHold)
    }

    /** Boulder neu anlegen (Server) und State/DB aktualisieren */
    fun saveBoulder(context: Context, name: String, difficulty: String, spraywallId: String, setterNote: String? ) {

        if (name.isBlank()) {
            _errorMessage.value = "Name darf nicht leer sein."
            Log.e("Boulder", "saveBoulder abgebrochen: leerer Name")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                ensureRepo(context)

                val note = setterNote?.trim().takeIf { !it.isNullOrEmpty() }

                val req = CreateBoulderRequest(
                    name = name,
                    difficulty = difficulty,
                    spraywallId = spraywallId,
                    holds = _uiState.value.holds,
                    setterNote = note
                )

                val api = RetrofitInstance.getBoulderApi(context)
                val result = repo.createOnline(api, req)

                result.onSuccess { created ->
                    _uiState.value = _uiState.value.copy(
                        boulder = created,
                        holds = created.holds,
                        selectedHoldId = null
                    )
                }.onFailure { e ->
                    _errorMessage.value = e.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Position eines Holds im State aktualisieren */
    fun updateHoldPosition(id: String, newX: Float, newY: Float) {
        _uiState.value = _uiState.value.copy(
            holds = _uiState.value.holds.map { if (it.id == id) it.copy(x = newX, y = newY) else it }
        )
    }


    /** Hold selektieren (UI-Markierung) */
    fun selectHold(id: String) {
        _uiState.value = _uiState.value.copy(selectedHoldId = id)
    }

    fun removeHold(id: String) {
        _uiState.value = _uiState.value.copy(
            holds = _uiState.value.holds.filterNot { it.id == id },
            selectedHoldId = if (_uiState.value.selectedHoldId == id) null else _uiState.value.selectedHoldId
        )
    }


    /**
     * Boulder inkl. Holds laden:
     * - online bevorzugt → DB spiegeln
     * - offline Fallback aus Room
     */
    fun loadBoulder(context: Context, boulderId: String) {
        viewModelScope.launch {
            ensureRepo(context)
            try {
                if (isOnline(context)) {
                    val res = RetrofitInstance.getBoulderApi(context)
                        .getBoulderById(UUID.fromString(boulderId))

                    if (res.isSuccessful) {
                        val dto = res.body()!!
                        repo.upsertFromRemote(dto)  // spiegele in Room
                        _uiState.value = _uiState.value.copy(
                            boulder = dto,
                            holds = dto.holds,
                            selectedHoldId = null
                        )
                        android.util.Log.d("BoulderLoad","Loaded ONLINE id=${dto.id} holds=${dto.holds.size}")
                        return@launch
                    }
                }

                // Offline/Fehler: lokaler Fallback
                val local = repo.getLocalBoulderWithHolds(boulderId)
                if (local != null) {
                    _uiState.value = _uiState.value.copy(
                        boulder = local,
                        holds = local.holds,
                        selectedHoldId = null
                    )
                    android.util.Log.d("BoulderLoad","Loaded OFFLINE id=${local.id} holds=${local.holds.size}")
                } else {
                    _errorMessage.value = "Boulder offline nicht gefunden"
                    android.util.Log.e("BoulderLoad","No local record for $boulderId")
                }
            } catch (e: Exception) {
                val local = repo.getLocalBoulderWithHolds(boulderId)
                if (local != null) {
                    _uiState.value = _uiState.value.copy(
                        boulder = local,
                        holds = local.holds,
                        selectedHoldId = null
                    )
                    android.util.Log.d("BoulderLoad","Loaded OFFLINE (exception) id=${local.id} holds=${local.holds.size}")
                } else {
                    _errorMessage.value = e.message
                    android.util.Log.e("BoulderLoad","Exception", e)
                }
            }
        }
    }


    /** Boulder löschen (Server) und UI zurücksetzen */
    fun deleteBoulder(context: Context, boulderId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = RetrofitInstance.getBoulderApi(context).deleteBoulder(boulderId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        boulder = null, holds = emptyList(), selectedHoldId = null
                    )
                    onDone()
                } else {
                    _errorMessage.value = "Löschen fehlgeschlagen (${res.code()})"
                }
            } catch (t: Throwable) {
                _errorMessage.value = t.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    /** Boulder als "getickt" am Server markieren (toaster Feedback) */
    fun tickBoulder(context: Context, boulderId: String,  stars: Int? = null, proposedGrade: String? = null) {
        viewModelScope.launch {
            try {

                val body = if (stars != null || !proposedGrade.isNullOrBlank())
                    TickCreateRequest(stars, proposedGrade)
                else TickCreateRequest()

                val res = RetrofitInstance.getBoulderApi(context).tickBoulder(boulderId, body)
                if (res.isSuccessful) {
                    android.widget.Toast.makeText(context, "Eingetragen!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Eintragen fehlgeschlagen (${res.code()})", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Netzwerkfehler: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }


    /** Boulder aktualisieren (Server) und State/DB spiegeln */
    fun updateBoulder(
        context: Context,
        name: String,
        difficulty: String,
        spraywallId: String,
        boulderIdOverride: String? = null,
        setterNote: String?
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Name darf nicht leer sein."
            Log.e("BoulderUpdate", "abgebrochen: leerer Name")
            return
        }


        val effectiveId = boulderIdOverride ?: _uiState.value.boulder?.id
        if (effectiveId.isNullOrBlank()) {
            _errorMessage.value = "Kein Boulder zum Aktualisieren geladen (fehlende ID)."
            android.util.Log.e("BoulderUpdate", "ABORT: no id (override/state both null)")
            return
        }

        val updatedDto = BoulderDTO(
            id = effectiveId,
            name = name,
            difficulty = difficulty,
            setterNote = setterNote?.trim().takeIf { !it.isNullOrEmpty() },
            spraywallId = spraywallId, holds = _uiState.value.holds
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                ensureRepo(context)
                val api = RetrofitInstance.getBoulderApi(context)
                val result = repo.updateOnline(api, effectiveId, updatedDto)

                result.onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        boulder = updated, holds = updated.holds, selectedHoldId = null
                    )
                }.onFailure { e ->
                    _errorMessage.value = e.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                android.util.Log.e("BoulderUpdate", "EXCEPTION", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
