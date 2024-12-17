package social.domain

import social.ddd.Entity
import social.ddd.Factory
import social.ddd.ID

class User (userID: UserID, private val username: String) : Entity<UserID>(userID)

data class UserID (val userID: String) : ID<String>(userID)

object UserFactory : Factory<User> {
    fun userOf(email: String, username: String): User = User(userIDOf(email), username)

    private fun userIDOf(email: String): UserID {
        // check if the email is valid, if no match is found, returns null, else returns the email as UserID
        return Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\$").find(email)?.value?.let {
            UserID(it)
        }
        // if there is no match (null), throw an exception
            ?: throw IllegalArgumentException("Invalid email")
    }
}
