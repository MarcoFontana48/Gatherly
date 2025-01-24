package social.user.application

import io.vertx.core.AbstractVerticle
import org.apache.logging.log4j.LogManager
import social.common.ddd.Repository
import social.common.ddd.Service
import social.common.events.UserCreated
import social.common.events.UserUpdated
import social.user.domain.User
import social.user.domain.User.UserID
import social.user.infrastructure.controller.event.KafkaUserProducerVerticle

interface UserService : Service {
    fun addUser(user: User)
    fun getUser(userID: UserID): User?
    fun updateUser(user: User)
}

class UserServiceImpl(private val repository: Repository<UserID, User>) : UserService, AbstractVerticle() {
    private val logger = LogManager.getLogger(this::class.java.name)
    private val kafkaProducer: KafkaProducerVerticle = KafkaUserProducerVerticle()

    override fun start() {
        vertx.deployVerticle(kafkaProducer).onComplete { result ->
            if (result.succeeded()) {
                logger.trace("Kafka producer verticle deployed")
            } else {
                logger.error("Failed to deploy Kafka producer verticle")
            }
        }
    }

    override fun addUser(user: User) {
        repository.save(user).let {
            kafkaProducer.publishEvent(UserCreated(user.username, user.email))
        }
    }

    override fun getUser(userID: UserID): User? = repository.findById(userID)

    override fun updateUser(user: User) {
        repository.update(user).let {
            kafkaProducer.publishEvent(UserUpdated(user.username, user.email))
        }
    }
}
