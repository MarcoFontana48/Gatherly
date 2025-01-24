package test.friendship.infrastructure.controller.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import social.common.ddd.DomainEvent
import social.common.events.UserCreated
import social.friendship.application.FriendshipServiceVerticle
import social.friendship.domain.User
import social.friendship.infrastructure.controller.event.KafkaFriendshipConsumerVerticle
import social.friendship.infrastructure.controller.event.KafkaFriendshipProducerVerticle
import social.friendship.infrastructure.persistence.sql.DatabaseCredentialsImpl
import social.friendship.infrastructure.persistence.sql.FriendshipRequestSQLRepository
import social.friendship.infrastructure.persistence.sql.FriendshipSQLRepository
import social.friendship.infrastructure.persistence.sql.MessageSQLRepository
import social.friendship.infrastructure.persistence.sql.UserSQLRepository
import test.friendship.infrastructure.DockerSQLTest
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class KafkaFriendshipVerticleTest : DockerSQLTest() {
    private val logger = LogManager.getLogger(this::class.java)
    private val user1 = User.of("user1")
    private val userRepository = UserSQLRepository()
    private val friendshipRepository = FriendshipSQLRepository()
    private val friendshipRequestRepository = FriendshipRequestSQLRepository()
    private val messageRepository = MessageSQLRepository()
    private val kafkaProducer = KafkaFriendshipProducerVerticle()
    private val dockerComposePath = "/social/friendship/infrastructure/controller/event/docker-compose.yml"
    private val vertx = Vertx.vertx()
    lateinit var dockerComposeFile: File
    lateinit var producer: KafkaFriendshipProducerVerticleTestClass
    lateinit var consumer: KafkaFriendshipConsumerVerticle

    @BeforeEach
    fun setUp() {
        val dockerComposeResource = this::class.java.getResource(dockerComposePath) ?: throw Exception("Resource not found")
        dockerComposeFile = File(dockerComposeResource.toURI())
        executeDockerComposeCmd(dockerComposeFile, "up", "--wait")

        val service = FriendshipServiceVerticle(
            userRepository,
            friendshipRepository,
            friendshipRequestRepository,
            messageRepository,
            kafkaProducer,
            DatabaseCredentialsImpl(localhostIP, "3307", database, user, password)
        )

        producer = KafkaFriendshipProducerVerticleTestClass()
        consumer = KafkaFriendshipConsumerVerticle(service)
        deployVerticle(vertx, producer, consumer, service)
        connectToDatabase()
    }

    private fun deployVerticle(vertx: Vertx, vararg verticles: AbstractVerticle) {
        val latch = CountDownLatch(verticles.size)
        verticles.forEach { verticle ->
            vertx.deployVerticle(verticle).onComplete {
                latch.countDown()
                if (it.succeeded()) {
                    logger.info("Verticle '{}' started", verticle.javaClass.simpleName)
                } else {
                    logger.error("Failed to start verticle '{}':", verticle.javaClass.simpleName, it.cause())
                }
            }
        }
        latch.await()
    }

    private fun connectToDatabase() {
        listOf(userRepository, friendshipRepository, friendshipRequestRepository).forEach {
            it.connect(localhostIP, "3307", database, user, password)
        }
    }

    @AfterEach
    fun tearDown() {
        executeDockerComposeCmd(dockerComposeFile, "down", "-v")
    }

    @Timeout(5 * 60)
    @Test
    fun updatesDatabaseUponReceivingUserCreatedEvent() {
        val before = userRepository.findById(user1.id)
        producer.publishEvent(UserCreated("test_username", user1.id.value))

        // waits for the event to be processed
        val latch = CountDownLatch(1)
        lateinit var after: User
        vertx.eventBus().consumer<String>(UserCreated.TOPIC) {
            userRepository.findById(user1.id)?.let {
                after = it
                latch.countDown()
            } ?: run {
                logger.error("User not found")
                latch.countDown()
            }
        }
        latch.await(4, TimeUnit.MINUTES)

        assertAll(
            { assertEquals(null, before) },
            { assertEquals(user1, after) }
        )
    }
}

/**
 * Class defined only to test events that this microservice does not produce, in order to test its consumer.
 */
class KafkaFriendshipProducerVerticleTestClass : AbstractVerticle() {
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

    override fun start() {
        producer = KafkaProducer.create(vertx, producerConfig)
    }

    fun publishEvent(event: DomainEvent) {
        when (event) {
            is UserCreated -> publish(UserCreated.TOPIC, mapper.writeValueAsString(event))
        }
    }

    private fun publish(topic: String, value: String, key: String? = null) {
        val record = KafkaProducerRecord.create<String, String>(
            topic,
            key,
            value
        )
        producer.write(record).onComplete {
            if (it.succeeded()) {
                logger.trace("Published event: TOPIC:{}, KEY:{}, VALUE:{}", topic, key, value)
            } else {
                logger.error("Failed to publish event: TOPIC:{}, KEY:{}, VALUE:{}", topic, key, value)
            }
        }
        logger.trace("Published event: TOPIC:{}, KEY:{}, VALUE:{}", topic, key, value)
    }
}
