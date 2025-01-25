package social.common.events

import social.common.ddd.DomainEvent
import kotlin.js.JsExport

/**
 * Interface to represent a content event.
 */
@JsExport
interface ContentEvent : DomainEvent

/**
 * Interface to represent an alert.
 */
@JsExport
interface Alert : DomainEvent

/**
 * Event that is published when a post is published.
 * @param title the title of the post
 * @param username the username of the user that published the post
 * @param content the content of the post
 */
@JsExport
data class PostPublished(
    val title: String,
    val username: String,
    val content: String,
) : ContentEvent {
    companion object {
        const val TOPIC = "post-published"
    }
}

/**
 * Event that is published when an alert about contents is published.
 * @param title the title of the post
 * @param username the username of the user that published the comment
 * @param content the content of the comment
 */
@JsExport
data class ContentAlert(
    val title: String,
    val username: String,
    val content: String,
) : Alert {
    companion object {
        const val TOPIC = "content-alert"
    }
}
