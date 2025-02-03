package social.friendship.infrastructure.controller.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import org.apache.logging.log4j.LogManager
import social.common.ddd.DomainEvent
import social.common.events.FriendshipRemoved
import social.common.events.FriendshipRequestAccepted
import social.common.events.FriendshipRequestRejected
import social.common.events.MessageSent
import social.friendship.application.EventBrokerProducerVerticle

/**
 * Verticle that produces events to Kafka
 */
class KafkaFriendshipProducerVerticle : AbstractVerticle(), EventBrokerProducerVerticle {
    private val logger = LogManager.getLogger(this::class)
    private val producerConfig = mapOf(
        "bootstrap.servers" to (System.getenv("KAFKA_HOST") ?: "localhost") + ":" + (System.getenv("KAFKA_PORT") ?: "9092"),
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "acks" to "1"
    )
    private val mapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }
    private lateinit var producer: KafkaProducer<String, String>

    /**
     * Start the producer
     */
    override fun start() {
        producer = KafkaProducer.create(vertx, producerConfig)
    }

    /**
     * Publish an event to Kafka.
     * @param event the event to publish. Possible events are:
     * - FriendshipRemoved
     * - FriendshipRequestRejected
     * - FriendshipRequestAccepted
     * - MessageSent
     */
    override fun publishEvent(event: DomainEvent) {
        when (event) {
            is FriendshipRemoved -> publish(FriendshipRemoved.Companion.TOPIC, mapper.writeValueAsString(event))
            is FriendshipRequestRejected -> publish(FriendshipRequestRejected.Companion.TOPIC, mapper.writeValueAsString(event))
            is FriendshipRequestAccepted -> publish(FriendshipRequestAccepted.Companion.TOPIC, mapper.writeValueAsString(event))
            is MessageSent -> publish(MessageSent.Companion.TOPIC, mapper.writeValueAsString(event))
        }
    }

    /**
     * Publish an event to Kafka
     * @param topic the topic to publish the event to
     * @param value the event to publish
     * @param key the key of the event
     */
    private fun publish(topic: String, value: String, key: String? = null) {
        val record = KafkaProducerRecord.create<String, String>(
            topic,
            key,
            value
        )
        producer.write(record)
        logger.trace("Published event: TOPIC:{}, KEY:{}, VALUE:{}", topic, key, value)
    }
}
