package com.mindmatrix.nammamela

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole { USER, ADMIN }
enum class SeatStatus { AVAILABLE, BOOKED, SELECTED }
enum class BookingStatus { ACTIVE, CANCELLED }

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val suspended: Boolean = false
)

@Entity(tableName = "natakas")
data class NatakaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val posterImage: String,
    val date: String,
    val startTime: String,
    val duration: String,
    val venue: String,
    val favorite: Boolean = false
)

@Entity(tableName = "castMembers")
data class CastEntity(
    @PrimaryKey val id: String,
    val natakaId: String,
    val fullName: String,
    val role: String,
    val photo: String,
    val biography: String,
    val experience: String,
    val socialLink: String = "",
    val otherNatakas: String = ""
)

@Entity(tableName = "seats")
data class SeatEntity(
    @PrimaryKey val id: String,
    val natakaId: String,
    val code: String,
    val rowName: String,
    val number: Int,
    val status: SeatStatus
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val natakaId: String,
    val seatCodes: String,
    val bookedAt: Long = System.currentTimeMillis(),
    val status: BookingStatus = BookingStatus.ACTIVE
)

@Entity(tableName = "fanComments")
data class FanCommentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val hidden: Boolean = false
)
