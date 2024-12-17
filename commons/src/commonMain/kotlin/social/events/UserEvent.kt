package social.events

import social.ddd.DomainEvent

/**
 * Interface to represent a user event.
 */
interface UserEvent : DomainEvent

/**
 * Event to represent a user creation.
 */
data class UserCreated(
    val username: String,
    val email: String,
) : UserEvent {
    companion object {
        const val TOPIC = "user-created"
    }
}

/**
 * Event to represent a user deletion.
 */
data class UserBlocked(
    val username: String,
) : UserEvent {
    companion object {
        const val TOPIC = "user-blocked"
    }
}

/**
 * Event to represent a user unblock.
 */
data class UserUnblocked(
    val username: String,
) : UserEvent {
    companion object {
        const val TOPIC = "user-unblocked"
    }
}

/**
 * Event to represent a user deletion.
 */
data class UserLoggedOut(
    val username: String,
) : UserEvent {
    companion object {
        const val TOPIC = "user-logged-out"
    }
}

/**
 * Event to represent admin unblock.
 */
data class AdminLoggedOut(
    val username: String,
) : UserEvent {
    companion object {
        const val TOPIC = "admin-logged-out"
    }
}

/**
 * Event to represent a user login.
 */
data class UserLoggedIn(
    val username: String,
) : UserEvent {
    companion object {
        const val TOPIC = "user-logged-in"
    }
}

/**
 * Event to represent admin login.
 */
data class AdminLoggedIn(
    val username: String,
) : UserEvent {
    companion object {
        const val TOPIC = "admin-logged-in"
    }
}
