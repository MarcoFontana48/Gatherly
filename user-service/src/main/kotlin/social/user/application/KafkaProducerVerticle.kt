package social.user.application

import io.vertx.core.Verticle
import social.common.ddd.DomainEvent

/**
 * Interface for a verticle that produces events to Kafka
 */
interface KafkaProducerVerticle : Verticle {
    fun publishEvent(event: DomainEvent)
}
