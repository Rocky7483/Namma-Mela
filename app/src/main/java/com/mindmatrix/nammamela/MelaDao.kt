package com.mindmatrix.nammamela

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MelaDao {
    @Query("SELECT * FROM users ORDER BY name")
    fun observeUsers(): Flow<List<UserEntity>>
    @Query("SELECT * FROM users WHERE email = :email AND password = :password AND role = :role LIMIT 1")
    suspend fun login(email: String, password: String, role: UserRole): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun userById(id: String): UserEntity?
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun userByEmail(email: String): UserEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)
    @Query("UPDATE users SET suspended = :suspended WHERE id = :id")
    suspend fun setUserSuspended(id: String, suspended: Boolean)
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("SELECT * FROM natakas ORDER BY date, startTime")
    fun observeNatakas(): Flow<List<NatakaEntity>>
    @Query("SELECT * FROM natakas WHERE id = :id LIMIT 1")
    suspend fun natakaById(id: String): NatakaEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNataka(nataka: NatakaEntity)
    @Query("DELETE FROM natakas WHERE id = :id")
    suspend fun deleteNataka(id: String)
    @Query("UPDATE natakas SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("SELECT * FROM castMembers WHERE natakaId = :natakaId ORDER BY fullName")
    fun observeCastForNataka(natakaId: String): Flow<List<CastEntity>>
    @Query("SELECT * FROM castMembers ORDER BY fullName")
    fun observeAllCast(): Flow<List<CastEntity>>
    @Query("SELECT * FROM castMembers WHERE id = :id LIMIT 1")
    suspend fun castById(id: String): CastEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCast(cast: CastEntity)
    @Query("DELETE FROM castMembers WHERE id = :id")
    suspend fun deleteCast(id: String)

    @Query("SELECT * FROM seats WHERE natakaId = :natakaId ORDER BY rowName, number")
    fun observeSeats(natakaId: String): Flow<List<SeatEntity>>
    @Query("SELECT * FROM seats WHERE natakaId = :natakaId AND status = 'AVAILABLE'")
    suspend fun availableSeats(natakaId: String): List<SeatEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSeats(seats: List<SeatEntity>)
    @Query("UPDATE seats SET status = :status WHERE natakaId = :natakaId AND code IN (:codes)")
    suspend fun updateSeats(natakaId: String, codes: List<String>, status: SeatStatus)

    @Query("SELECT * FROM bookings ORDER BY bookedAt DESC")
    fun observeAllBookings(): Flow<List<BookingEntity>>
    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY bookedAt DESC")
    fun observeBookingsForUser(userId: String): Flow<List<BookingEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooking(booking: BookingEntity)
    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun bookingById(id: String): BookingEntity?
    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: String, status: BookingStatus)

    @Query("SELECT * FROM fanComments WHERE hidden = 0 ORDER BY createdAt DESC")
    fun observeComments(): Flow<List<FanCommentEntity>>
    @Query("SELECT * FROM fanComments ORDER BY createdAt DESC")
    fun observeAllComments(): Flow<List<FanCommentEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertComment(comment: FanCommentEntity)
    @Query("UPDATE fanComments SET likes = likes + 1 WHERE id = :id")
    suspend fun likeComment(id: String)
    @Query("DELETE FROM fanComments WHERE id = :id")
    suspend fun deleteComment(id: String)
    @Query("UPDATE fanComments SET hidden = :hidden WHERE id = :id")
    suspend fun moderateComment(id: String, hidden: Boolean)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun userCount(): Int
}
