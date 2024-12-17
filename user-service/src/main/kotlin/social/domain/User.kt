package social.domain

import social.ddd.Entity
import social.ddd.ID

data class UserID(val userID: String) : ID<String>(userID)

class User(userID: UserID, private val username: String) : Entity<UserID>(userID)
