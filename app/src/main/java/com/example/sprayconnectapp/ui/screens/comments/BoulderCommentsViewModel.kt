// com.example.sprayconnectapp.ui.screens.comments/BoulderCommentsViewModel.kt
package com.example.sprayconnectapp.ui.screens.comments

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sprayconnectapp.data.repository.CommentRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sprayconnectapp.data.dto.CommentDto
import java.util.UUID

// BoulderCommentsViewModel.kt
class BoulderCommentsViewModel : ViewModel() {

    private val repo = CommentRepository()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _comments = mutableStateOf<List<CommentDto>>(emptyList())
    val comments: State<List<CommentDto>> = _comments

    // NEW: Post-States
    var sending by mutableStateOf(false); private set
    var postError by mutableStateOf<String?>(null); private set

    var deleting by mutableStateOf(false); private set
    var deleteError by mutableStateOf<String?>(null); private set



    var totalSends by mutableStateOf<Int?>(null); private set
    var avgGradeLabel by mutableStateOf<String?>(null); private set

    var gradesCount by mutableStateOf<Int?>(null); private set


    fun load(context: Context, boulderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _comments.value = repo.getComments(context, UUID.fromString(boulderId))

                // 2) Boulder + Stats
                val boulderApi = com.example.sprayconnectapp.network.RetrofitInstance.getBoulderApi(context)
                val resp = boulderApi.getBoulderById(UUID.fromString(boulderId))
                if (resp.isSuccessful) {
                    resp.body()?.let { b ->
                        totalSends   = b.totalSends?.toInt()
                        avgGradeLabel = b.avgGradeLabel
                        gradesCount   = b.gradesCount
                    }
                } else {

                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Fehler beim Laden"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh(context: Context, boulderId: String) = load(context, boulderId)


    fun addComment(
        context: Context,
        boulderId: String,
        text: String,
        onSuccess: () -> Unit
    ) {
        if (text.isBlank()) {
            postError = "Bitte Text eingeben."
            return
        }
        viewModelScope.launch {
            try {
                sending = true
                postError = null
                val ok = repo.addComment(context, UUID.fromString(boulderId), text.trim()) != null
                if (ok) onSuccess() else postError = "Fehler beim Speichern"
            } catch (e: Exception) {
                postError = e.message ?: "Unbekannter Fehler"
            } finally {
                sending = false
            }
        }
    }

    fun deleteComment(
        context: Context,
        commentId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                deleting = true
                deleteError = null
                val ok = repo.deleteComment(context, UUID.fromString(commentId))
                if (ok) onSuccess() else deleteError = "Löschen fehlgeschlagen"
            } catch (e: Exception) {
                deleteError = e.message ?: "Unbekannter Fehler"
            } finally {
                deleting = false
            }
        }
    }

}

