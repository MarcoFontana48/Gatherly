package social.friendship.application

import io.vertx.core.Verticle
import social.common.ddd.DomainEvent

/**
 * Interface for a verticle that produces events to Kafka
 */
interface KafkaProducerVerticle : Verticle {
    /**
     * Publish an event to Kafka
     * @param event the event to publish
     */
    fun publishEvent(event: DomainEvent)
}
