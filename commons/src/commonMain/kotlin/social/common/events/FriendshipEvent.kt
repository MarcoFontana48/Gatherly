package social.common.events

import social.common.ddd.DomainEvent
import kotlin.js.JsExport

/**
 * Interface to represent a friendship event.
 */
@JsExport
interface FriendshipEvent : DomainEvent

/**
 * Data class to represent a friendship request sent event.
 * @param sender the user that sends the request
 * @param receiver the user that receives the request
 */
@JsExport
data class FriendshipRemoved(
    val sender: String,
    val receiver: String,
) : FriendshipEvent {
    companion object {
        const val TOPIC = "friendship-removed"
    }
}

/**
 * Data class to represent a friendship request sent event.
 * @param sender the user that sends the request
 * @param receiver the user that receives the request
 */
@JsExport
data class FriendshipRequestSent(
    val sender: String,
    val receiver: String,
) : FriendshipEvent {
    companion object {
        const val TOPIC = "friendship-request-sent"
    }
}

/**
 * Data class to represent a friendship request accepted event.
 * @param sender the user that sends the request
 * @param receiver the user that receives the request
 */
@JsExport
data class FriendshipRequestAccepted(
    val sender: String,
    val receiver: String,
) : FriendshipEvent {
    companion object {
        const val TOPIC = "friendship-request-accepted"
    }
}

/**
 * Data class to represent a friendship request rejected event.
 * @param sender the user that sends the request
 * @param receiver the user that receives the request
 */
@JsExport
data class FriendshipRequestRejected(
    val sender: String,
    val receiver: String,
) : FriendshipEvent {
    companion object {
        const val TOPIC = "friendship-request-rejected"
    }
}
