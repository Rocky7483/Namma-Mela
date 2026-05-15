package com.mindmatrix.nammamela

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

enum class NatakaFilter { TODAY, TOMORROW, WEEK }

class MelaViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as NammaMelaApp).repository
    val currentUser = MutableStateFlow<UserEntity?>(null)
    val selectedRole = MutableStateFlow(UserRole.USER)
    val filter = MutableStateFlow(NatakaFilter.TODAY)
    val query = MutableStateFlow("")
    val selectedNatakaId = MutableStateFlow<String?>(null)
    val selectedCastId = MutableStateFlow<String?>(null)
    val selectedSeats = MutableStateFlow<Set<String>>(emptySet())
    val message = MutableStateFlow<String?>(null)

    val natakas = repo.natakas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val users = repo.users.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBookings = repo.allBookings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val comments = repo.comments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCast = repo.allCast.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val seats = selectedNatakaId.flatMapLatest { id -> id?.let { repo.seats(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cast = selectedNatakaId.flatMapLatest { id -> id?.let { repo.castFor(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val myBookings = currentUser.flatMapLatest { user -> user?.let { repo.bookingsFor(it.id) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repo.seedIfNeeded()
            currentUser.value = repo.currentUser()
            selectedRole.value = repo.currentRole() ?: UserRole.USER
        }
    }

    fun filteredNatakas(): List<NatakaEntity> {
        val today = LocalDate.now()
        val search = query.value.trim().lowercase()
        return natakas.value.filter { item ->
            val date = LocalDate.parse(item.date)
            val dateOk = when (filter.value) {
                NatakaFilter.TODAY -> date == today
                NatakaFilter.TOMORROW -> date == today.plusDays(1)
                NatakaFilter.WEEK -> !date.isBefore(today) && !date.isAfter(today.plusDays(7))
            }
            val searchOk = search.isBlank() || item.title.lowercase().contains(search) || item.venue.lowercase().contains(search)
            dateOk && searchOk
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        repo.login(email, password, selectedRole.value)
            .onSuccess { currentUser.value = it }
            .onFailure { message.value = it.message }
    }
    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        repo.signup(name, email, password, selectedRole.value)
            .onSuccess { currentUser.value = it }
            .onFailure { message.value = it.message }
    }
    fun forgotPassword(email: String) { message.value = if (email.isBlank()) "Enter registered email" else "Password reset link simulated for $email" }
    fun logout() { repo.logout(); currentUser.value = null; selectedSeats.value = emptySet() }
    fun selectNataka(id: String) { selectedNatakaId.value = id; selectedSeats.value = emptySet() }
    fun toggleSeat(code: String) {
        selectedSeats.value = selectedSeats.value.toMutableSet().also { if (!it.add(code)) it.remove(code) }
    }
    fun bookSelectedSeats() = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        val natakaId = selectedNatakaId.value ?: return@launch
        repo.bookSeats(user.id, natakaId, selectedSeats.value)
            .onSuccess { selectedSeats.value = emptySet(); message.value = "Booking confirmed" }
            .onFailure { message.value = it.message }
    }
    fun cancelBooking(id: String) = viewModelScope.launch { repo.cancelBooking(id); message.value = "Booking cancelled" }
    fun saveNataka(existingId: String?, title: String, desc: String, poster: String, date: String, time: String, duration: String, venue: String) =
        viewModelScope.launch {
            if (title.isBlank() || date.isBlank()) { message.value = "Title and date are required"; return@launch }
            repo.saveNataka(NatakaEntity(existingId ?: UUID.randomUUID().toString(), title, desc, poster, date, time, duration, venue))
            message.value = "Nataka saved"
        }
    fun deleteNataka(id: String) = viewModelScope.launch { repo.deleteNataka(id); message.value = "Nataka deleted" }
    fun saveCast(natakaId: String, name: String, role: String, photo: String, bio: String, exp: String) = viewModelScope.launch {
        repo.saveCast(CastEntity(UUID.randomUUID().toString(), natakaId, name, role, photo, bio, exp))
        message.value = "Cast member saved"
    }
    fun deleteCast(id: String) = viewModelScope.launch { repo.deleteCast(id) }
    fun suspendUser(id: String, suspend: Boolean) = viewModelScope.launch { repo.suspendUser(id, suspend) }
    fun deleteUser(id: String) = viewModelScope.launch { repo.deleteUser(id) }
    fun favorite(id: String, favorite: Boolean) = viewModelScope.launch { repo.setFavorite(id, favorite) }
    fun addComment(text: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        if (text.isBlank()) { message.value = "Comment cannot be empty"; return@launch }
        repo.addComment(user, text)
    }
    fun likeComment(id: String) = viewModelScope.launch { repo.likeComment(id) }
    fun deleteComment(id: String) = viewModelScope.launch { repo.deleteComment(id) }
    fun moderateComment(id: String) = viewModelScope.launch { repo.moderateComment(id, true) }
}
