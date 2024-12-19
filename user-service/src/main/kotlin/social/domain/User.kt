package social.domain

import social.ddd.Entity
import social.ddd.Factory
import social.ddd.ID

class User private constructor(val email: String, val username: String) : Entity<User.UserID>(UserID(email)) {
    /**
     * Data class to represent the user ID.
     */
    data class UserID(val value: String) : ID<String>(value)

    companion object : Factory<User> {
        fun of(email: String, username: String): User = User(asId(email), username)

        private fun asId(email: String): String {
            // check if the email is valid, if no match is found, returns null, else returns the email as UserID
            return Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\$")
                .find(email)?.value ?: throw IllegalArgumentException("Invalid email")
        }
    }
}
