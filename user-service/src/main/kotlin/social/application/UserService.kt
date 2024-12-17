package social.application

import io.vertx.core.AbstractVerticle
import io.vertx.core.Verticle
import social.ddd.Repository
import social.ddd.Service
import social.domain.User
import social.domain.UserID

interface UserService : Verticle, Service {
    fun addUser(user: User)
    fun getUser(userID: UserID): User?
    fun updateUser(user: User)
}

class UserServiceImpl(private val repository: Repository<UserID, User>) : AbstractVerticle(), UserService {
    override fun addUser(user: User) = repository.save(user)

    override fun getUser(userID: UserID): User? = repository.findById(userID)

    override fun updateUser(user: User) = repository.update(user)
}
