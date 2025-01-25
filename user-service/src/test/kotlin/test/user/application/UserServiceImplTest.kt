package test.user.application

import io.vertx.core.Vertx
import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import social.common.events.UserCreated
import social.common.events.UserUpdated
import social.user.application.UserRepository
import social.user.application.UserServiceImpl
import social.user.domain.User
import social.user.domain.User.UserID
import social.user.infrastructure.controller.event.KafkaUserProducerVerticle
import social.user.infrastructure.persitence.sql.UserSQLRepository
import java.lang.reflect.Field
import java.util.concurrent.CountDownLatch

class UserServiceImplTest {
    private val logger = LogManager.getLogger(this::class.java)
    private lateinit var vertx: Vertx
    private val repository: UserRepository = mock<UserSQLRepository>()
    private val mockKafkaProducer: KafkaUserProducerVerticle = mock()
    private lateinit var service: UserServiceImpl
    private val user = User.of("test.email76@gmail.com", "username")
    private val nonExistingUserID = UserID("nonExistingUserID")

    init {
        `when`(repository.findById(user.id)).thenReturn(user)
        `when`(repository.findById(nonExistingUserID)).thenReturn(null)
        doNothing().`when`(mockKafkaProducer).publishEvent(UserCreated(user.id.value, user.username))
        doNothing().`when`(mockKafkaProducer).publishEvent(UserUpdated(user.id.value, user.username))
    }

    @BeforeEach
    fun setUp() {
        val latch = CountDownLatch(1)
        vertx = Vertx.vertx()

        service = UserServiceImpl(repository, mockKafkaProducer)

        val kafkaProducerField: Field = UserServiceImpl::class.java.getDeclaredField("kafkaProducer")
        kafkaProducerField.isAccessible = true
        kafkaProducerField.set(service, mockKafkaProducer)

        vertx.deployVerticle(service).onComplete {
            latch.countDown()
            if (it.succeeded()) {
                logger.info("UserServiceImpl Verticle started")
            } else {
                logger.error("Failed to start UserServiceImpl Verticle")
            }
        }
        latch.await()
    }

    @AfterEach
    fun tearDown() {
        val latch = CountDownLatch(1)
        vertx.close().onComplete {
            latch.countDown()
            if (it.succeeded()) {
                logger.info("Vertx closed")
            } else {
                logger.error("Failed to close Vertx")
            }
        }
        latch.await()
    }

    @Test
    fun addUser() {
        assertDoesNotThrow { service.addUser(user) }
    }

    @Test
    fun getUser() {
        val actual = service.getUser(user.id)
        assertEquals(user, actual)
    }

    @Test
    fun getNonExistingUser() {
        val actual = service.getUser(nonExistingUserID)
        assertEquals(null, actual)
    }

    @Test
    fun updateUser() {
        assertDoesNotThrow { service.updateUser(user) }
    }
}
