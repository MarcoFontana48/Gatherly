package social.common.events

import social.common.ddd.DomainEvent
import kotlin.js.JsExport

@JsExport
interface ContentEvent : DomainEvent

@JsExport
interface Alert : DomainEvent

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
