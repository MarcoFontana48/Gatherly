package social.user

import io.vertx.core.Verticle
import io.vertx.core.Vertx
import social.user.application.KafkaUserProducerVerticle
import social.user.application.UserServiceImpl
import social.user.infrastructure.controller.rest.RESTUserAPIVerticle
import social.user.infrastructure.persitence.sql.UserSQLRepository
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()

    val repository = UserSQLRepository()
    repository.connect(
        System.getenv("DB_HOST"),
        System.getenv("DB_PORT"),
        System.getenv("MYSQL_DATABASE"),
        System.getenv("MYSQL_USER"),
        Files.readString(Paths.get("/run/secrets/db_password")).trim(),
    )

    val service = UserServiceImpl(repository)
    val api = RESTUserAPIVerticle(service)
    val producer = KafkaUserProducerVerticle()

    deployVerticles(vertx, api, producer, service)
}

private fun deployVerticles(vertx: Vertx, vararg verticles: Verticle) {
    verticles.forEach {
        vertx.deployVerticle(it)
    }
}
