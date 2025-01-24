package social.friendship

import io.vertx.core.Verticle
import io.vertx.core.Vertx
import social.friendship.application.FriendshipServiceVerticle
import social.friendship.infrastructure.controller.event.KafkaFriendshipConsumerVerticle
import social.friendship.infrastructure.controller.event.KafkaFriendshipProducerVerticle
import social.friendship.infrastructure.controller.rest.RESTFriendshipAPIVerticleImpl

fun main(args: Array<String>) {
    val vertx: Vertx = Vertx.vertx()

    val service = FriendshipServiceVerticle()
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
