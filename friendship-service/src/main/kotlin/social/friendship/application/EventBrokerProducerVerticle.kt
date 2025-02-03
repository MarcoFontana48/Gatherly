package social.friendship.application

import io.vertx.core.Verticle
import social.common.ddd.DomainEvent

/**
 * Interface for a verticle that produces events to the event broker
 */
interface EventBrokerProducerVerticle : Verticle {
    /**
     * Publish an event to the event broker
     * @param event the event to publish
     */
    fun publishEvent(event: DomainEvent)
}
