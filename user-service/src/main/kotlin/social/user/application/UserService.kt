package social.user.application

import social.common.ddd.Repository
import social.common.ddd.Service
import social.user.domain.User
import social.user.domain.User.UserID

interface UserService : Service {
    fun addUser(user: User)
    fun getUser(userID: UserID): User?
    fun updateUser(user: User)
}

class UserServiceImpl(private val repository: Repository<UserID, User>) : UserService {
    override fun addUser(user: User) = repository.save(user)

    override fun getUser(userID: UserID): User? = repository.findById(userID)

    override fun updateUser(user: User) = repository.update(user)
}
