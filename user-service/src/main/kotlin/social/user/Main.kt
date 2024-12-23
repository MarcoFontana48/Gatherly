package social.user

import io.vertx.core.Vertx
import social.user.application.UserServiceImpl
import social.user.infrastructure.RESTUserAPIVerticle
import social.user.infrastructure.persitence.sql.UserSQLRepository

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()

    val repository = UserSQLRepository()
    repository.connect(
        "user-sql-db",
        "3306",
        "user",
        "root",
        "password"
    )
    val service = UserServiceImpl(repository)
    val api = RESTUserAPIVerticle(service)

    vertx.deployVerticle(api)
}
