package com.example.projectuasmobile.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.projectuasmobile.models.Notes
import com.example.projectuasmobile.repository.StorageRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser

class DetailViewModel(
    private val repository: StorageRepository = StorageRepository()
):ViewModel() {
    var detailUiState by mutableStateOf(DetailUiState())
        private set

    private val hasUser: Boolean
        get() = repository.hasUser()

    private val user: FirebaseUser?
        get() = repository.user()

    fun onColorChange(colorIndex: Int){
        detailUiState = detailUiState.copy(colorIndex = colorIndex)
    }

    fun onTitleChange(title: String){
        detailUiState = detailUiState.copy(title = title)
    }

    fun onNoteChange(note: String){
        detailUiState = detailUiState.copy(note = note)
    }

    fun onAddressChange(address: String){
        detailUiState = detailUiState.copy(address = address)
    }

    fun addNote(){
        if (hasUser){
            repository.addNote(
                userId = user!!.uid,
                title = detailUiState.title,
                description = detailUiState.note,
                color = detailUiState.colorIndex,
                address = detailUiState.address,
                timestamp = Timestamp.now()
            ){
                detailUiState = detailUiState.copy(noteAddedStatus = it)
            }
        }
    }

    fun setEditFields(note: Notes){
        detailUiState = detailUiState.copy(
            colorIndex = note.colorIndex,
            title = note.title,
            note = note.description,
            address = note.address
        )
    }

    fun getNote(noteId: String){
        repository.getNote(noteId = noteId, onError = {}){
            detailUiState = detailUiState.copy(selectedNote = it)
            detailUiState.selectedNote?.let {it1 -> setEditFields(it1)}
        }
    }

    fun updateNote(noteId: String){
        repository.updateNote(
            title = detailUiState.title,
            note = detailUiState.note,
            noteId = noteId,
            color = detailUiState.colorIndex,
            address = detailUiState.address
            ){
            detailUiState = detailUiState.copy(updateNoteStatus = it)
        }
    }

    fun resetNoteAddesStatus(){
        detailUiState = detailUiState.copy(noteAddedStatus = false, updateNoteStatus = false)
    }

    fun resetState(){
        detailUiState = DetailUiState()
    }

}

data class DetailUiState(
    val colorIndex: Int = 0,
    val title: String = "",
    val note: String = "",
    val address: String = "",
    val noteAddedStatus: Boolean = false,
    val updateNoteStatus: Boolean = false,
    val selectedNote: Notes? = null
)