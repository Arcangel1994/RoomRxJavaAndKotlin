package miguel.isavanzados.com.mx.roomrxjavakotlin.Local

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import miguel.isavanzados.com.mx.roomrxjavakotlin.Local.UserDatabase.Companion.DATABASE_VERSION
import miguel.isavanzados.com.mx.roomrxjavakotlin.Model.User

@Database(entities = arrayOf(User::class), version = DATABASE_VERSION)
abstract class UserDatabase: RoomDatabase() {

    abstract fun userDAO(): UserDAO

    companion object {
        const val DATABASE_VERSION = 1
        val DATABASE_NAME="Database-Room"

        private var mInstance: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase{
            if (mInstance == null){
                mInstance = Room.databaseBuilder(context, UserDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return mInstance!!
        }

    }

}