package social.events

import social.ddd.DomainEvent

interface MessageEvent : DomainEvent

data class MessageSent(
    val sender: String,
    val receiver: String,
    val message: String,
): MessageEvent {
    companion object {
        const val TOPIC = "message-sent"
    }
}

data class MessageReceived(
    val sender: String,
    val receiver: String,
    val message: String,
): MessageEvent {
    companion object {
        const val TOPIC = "message-received"
    }
}