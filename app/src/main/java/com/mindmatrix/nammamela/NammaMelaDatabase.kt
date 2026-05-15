package com.mindmatrix.nammamela

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter fun roleToString(role: UserRole): String = role.name
    @TypeConverter fun stringToRole(value: String): UserRole = UserRole.valueOf(value)
    @TypeConverter fun seatStatusToString(status: SeatStatus): String = status.name
    @TypeConverter fun stringToSeatStatus(value: String): SeatStatus = SeatStatus.valueOf(value)
    @TypeConverter fun bookingStatusToString(status: BookingStatus): String = status.name
    @TypeConverter fun stringToBookingStatus(value: String): BookingStatus = BookingStatus.valueOf(value)
}

@Database(
    entities = [
        UserEntity::class,
        NatakaEntity::class,
        CastEntity::class,
        SeatEntity::class,
        BookingEntity::class,
        FanCommentEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NammaMelaDatabase : RoomDatabase() {
    abstract fun melaDao(): MelaDao

    companion object {
        @Volatile private var instance: NammaMelaDatabase? = null
        fun getDatabase(context: Context): NammaMelaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NammaMelaDatabase::class.java,
                    "namma_mela.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
