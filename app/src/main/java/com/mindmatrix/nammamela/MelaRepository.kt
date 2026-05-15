package com.mindmatrix.nammamela

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

class MelaRepository(private val context: Context, private val dao: MelaDao) {
    private val prefs = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)
    val users: Flow<List<UserEntity>> = dao.observeUsers()
    val natakas: Flow<List<NatakaEntity>> = dao.observeNatakas()
    val allBookings: Flow<List<BookingEntity>> = dao.observeAllBookings()
    val comments: Flow<List<FanCommentEntity>> = dao.observeComments()
    val allComments: Flow<List<FanCommentEntity>> = dao.observeAllComments()
    val allCast: Flow<List<CastEntity>> = dao.observeAllCast()

    suspend fun seedIfNeeded() {
        if (dao.userCount() > 0) return
        val admin = UserEntity("admin-1", "Mela Admin", "admin@nammamela.com", "admin123", UserRole.ADMIN)
        val user = UserEntity("user-1", "Bharath Fan", "user@nammamela.com", "user123", UserRole.USER)
        dao.upsertUser(admin)
        dao.upsertUser(user)
        val today = LocalDate.now()
        val events = listOf(
            NatakaEntity("nataka-1", "Kittur Rani Chennamma", "A proud village-stage tribute to courage, community, and Karnataka tradition.", "https://images.unsplash.com/photo-1503095396549-807759245b35", today.toString(), "7:30 PM", "2h 20m", "Namma Mela Main Stage"),
            NatakaEntity("nataka-2", "Sangolli Rayanna", "A bold historical drama with music, comedy, and patriotic storytelling.", "https://images.unsplash.com/photo-1514306191717-452ec28c7814", today.plusDays(1).toString(), "8:00 PM", "2h 10m", "Village Grounds"),
            NatakaEntity("nataka-3", "Mayura Nataka", "A family-friendly cultural performance from a touring company nataka troupe.", "https://images.unsplash.com/photo-1517457373958-b7bdd4587205", today.plusDays(4).toString(), "6:45 PM", "2h 30m", "Temple Fair Stage")
        )
        events.forEach { nataka ->
            dao.upsertNataka(nataka)
            dao.upsertSeats(('A'..'F').flatMap { row ->
                (1..8).map { number ->
                    val booked = row == 'A' && number <= 2
                    SeatEntity("${nataka.id}-$row$number", nataka.id, "$row$number", row.toString(), number, if (booked) SeatStatus.BOOKED else SeatStatus.AVAILABLE)
                }
            })
            listOf(
                CastEntity("${nataka.id}-cast-1", nataka.id, "Shankar Master", "Lead Actor", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e", "Known for expressive historical roles and powerful stage presence.", "18 years in company nataka", "https://example.com", "Kurukshetra, Mayura"),
                CastEntity("${nataka.id}-cast-2", nataka.id, "Meera Bai", "Singer", "https://images.unsplash.com/photo-1494790108377-be9c29b29330", "A respected folk and theatre singer with a devotional repertoire.", "12 years performing across Karnataka", "", "Bhakta Prahlada")
            ).forEach { dao.upsertCast(it) }
        }
        dao.upsertComment(FanCommentEntity(UUID.randomUUID().toString(), user.id, user.name, "The music and acting made the whole village proud.", likes = 3))
    }

    fun currentUserId(): String? = prefs.getString("userId", null)
    fun currentRole(): UserRole? = prefs.getString("role", null)?.let { UserRole.valueOf(it) }
    suspend fun currentUser(): UserEntity? = currentUserId()?.let { dao.userById(it) }
    fun logout() = prefs.edit().clear().apply()

    suspend fun login(email: String, password: String, role: UserRole): Result<UserEntity> {
        val user = dao.login(email.trim(), password, role) ?: return Result.failure(IllegalArgumentException("Invalid credentials for selected role"))
        if (user.suspended) return Result.failure(IllegalArgumentException("This account is suspended"))
        saveSession(user)
        return Result.success(user)
    }

    suspend fun signup(name: String, email: String, password: String, role: UserRole): Result<UserEntity> {
        if (name.isBlank() || email.isBlank() || password.length < 6) return Result.failure(IllegalArgumentException("Enter name, email, and a 6+ character password"))
        if (dao.userByEmail(email.trim()) != null) return Result.failure(IllegalArgumentException("Email already registered"))
        val user = UserEntity(UUID.randomUUID().toString(), name.trim(), email.trim(), password, role)
        dao.upsertUser(user)
        saveSession(user)
        return Result.success(user)
    }

    private fun saveSession(user: UserEntity) {
        prefs.edit().putString("userId", user.id).putString("role", user.role.name).apply()
    }

    fun seats(natakaId: String) = dao.observeSeats(natakaId)
    fun castFor(natakaId: String) = dao.observeCastForNataka(natakaId)
    fun bookingsFor(userId: String) = dao.observeBookingsForUser(userId)

    suspend fun bookSeats(userId: String, natakaId: String, selected: Set<String>): Result<Unit> {
        if (selected.isEmpty()) return Result.failure(IllegalArgumentException("Select at least one seat"))
        val available = dao.availableSeats(natakaId).map { it.code }.toSet()
        if (!available.containsAll(selected)) return Result.failure(IllegalStateException("One or more seats were already booked"))
        dao.updateSeats(natakaId, selected.toList(), SeatStatus.BOOKED)
        dao.upsertBooking(BookingEntity(UUID.randomUUID().toString(), userId, natakaId, selected.sorted().joinToString(",")))
        return Result.success(Unit)
    }

    suspend fun cancelBooking(id: String) {
        val booking = dao.bookingById(id) ?: return
        if (booking.status == BookingStatus.CANCELLED) return
        dao.updateBookingStatus(id, BookingStatus.CANCELLED)
        dao.updateSeats(booking.natakaId, booking.seatCodes.split(",").filter { it.isNotBlank() }, SeatStatus.AVAILABLE)
    }

    suspend fun saveNataka(nataka: NatakaEntity) {
        val isNew = dao.natakaById(nataka.id) == null
        dao.upsertNataka(nataka)
        if (isNew) dao.upsertSeats(('A'..'F').flatMap { row ->
            (1..8).map { number -> SeatEntity("${nataka.id}-$row$number", nataka.id, "$row$number", row.toString(), number, SeatStatus.AVAILABLE) }
        })
    }
    suspend fun deleteNataka(id: String) = dao.deleteNataka(id)
    suspend fun saveCast(cast: CastEntity) = dao.upsertCast(cast)
    suspend fun deleteCast(id: String) = dao.deleteCast(id)
    suspend fun suspendUser(id: String, suspend: Boolean) = dao.setUserSuspended(id, suspend)
    suspend fun deleteUser(id: String) = dao.deleteUser(id)
    suspend fun setFavorite(id: String, favorite: Boolean) = dao.setFavorite(id, favorite)
    suspend fun addComment(user: UserEntity, message: String) = dao.upsertComment(FanCommentEntity(UUID.randomUUID().toString(), user.id, user.name, message))
    suspend fun likeComment(id: String) = dao.likeComment(id)
    suspend fun deleteComment(id: String) = dao.deleteComment(id)
    suspend fun moderateComment(id: String, hidden: Boolean) = dao.moderateComment(id, hidden)
}
