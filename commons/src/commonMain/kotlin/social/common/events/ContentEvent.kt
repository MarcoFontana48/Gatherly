package social.common.events

import social.common.ddd.DomainEvent

interface ContentEvent : DomainEvent

interface Alert : DomainEvent

data class PostPublished(
    val title: String,
    val username: String,
    val content: String,
) : ContentEvent {
    companion object {
        const val TOPIC = "post-published"
    }
}

data class ContentAlert(
    val title: String,
    val username: String,
    val content: String,
) : Alert {
    companion object {
        const val TOPIC = "content-alert"
    }
}
