package miguel.isavanzados.com.mx.roomrxjavakotlin.Database

import io.reactivex.Flowable
import miguel.isavanzados.com.mx.roomrxjavakotlin.Model.User

interface IUserDataSource {

    val allUsers: Flowable<List<User>>

    fun getUserById(userId: Int):Flowable<User>

    fun insertUser(vararg users: User)

    fun updateUser(vararg users: User)

    fun deleteUser(user: User)

    fun deleteAllUsers()

}