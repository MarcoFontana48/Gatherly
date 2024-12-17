package social.events

import social.ddd.DomainEvent

interface FriendshipEvent : DomainEvent

data class FriendshipRequestSent(
    val sender: String,
    val receiver: String,
) : FriendshipEvent {
    companion object {
        const val TOPIC = "friendship-request-sent"
    }
}

data class FriendshipRequestAccepted(
    val sender: String,
    val receiver: String,
) : FriendshipEvent {
    companion object {
        const val TOPIC = "friendship-request-accepted"
    }
}

data class FriendshipRequestRejected(
    val sender: String,
    val receiver: String,
) : FriendshipEvent {
    companion object {
        const val TOPIC = "friendship-request-rejected"
    }
}
