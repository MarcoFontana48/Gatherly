package social.friendship

import io.vertx.core.Verticle
import io.vertx.core.Vertx
import social.friendship.application.FriendshipServiceVerticle
import social.friendship.infrastructure.controller.event.KafkaFriendshipConsumerVerticle
import social.friendship.infrastructure.controller.event.KafkaFriendshipProducerVerticle
import social.friendship.infrastructure.controller.rest.RESTFriendshipAPIVerticleImpl
import social.friendship.infrastructure.persistence.sql.FriendshipRequestSQLRepository
import social.friendship.infrastructure.persistence.sql.FriendshipSQLRepository
import social.friendship.infrastructure.persistence.sql.MessageSQLRepository
import social.friendship.infrastructure.persistence.sql.UserSQLRepository

fun main(args: Array<String>) {
    val vertx: Vertx = Vertx.vertx()

    val userRepository = UserSQLRepository()
    val friendshipRepository = FriendshipSQLRepository()
    val friendshipRequestRepository = FriendshipRequestSQLRepository()
    val messageRepository = MessageSQLRepository()
    val kafkaProducer = KafkaFriendshipProducerVerticle()

    val service = FriendshipServiceVerticle(
        userRepository,
        friendshipRepository,
        friendshipRequestRepository,
        messageRepository,
        kafkaProducer,
    )

    val api = RESTFriendshipAPIVerticleImpl(service)
    val producer = KafkaFriendshipProducerVerticle()
    val consumer = KafkaFriendshipConsumerVerticle(service)

    deployVerticles(vertx, api, consumer, producer, service)
}

private fun deployVerticles(vertx: Vertx, vararg verticles: Verticle) {
    verticles.forEach {
        vertx.deployVerticle(it)
    }
}
