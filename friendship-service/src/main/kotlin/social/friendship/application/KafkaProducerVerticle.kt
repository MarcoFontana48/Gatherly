package social.friendship.application

import io.vertx.core.Verticle
import social.common.ddd.DomainEvent

interface KafkaProducerVerticle : Verticle {
    fun publishEvent(event: DomainEvent)
}
