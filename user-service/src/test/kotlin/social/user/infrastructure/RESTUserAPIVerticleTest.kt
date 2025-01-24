package social.user.infrastructure

import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.ext.web.codec.BodyCodec
import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import social.common.endpoint.Endpoint
import social.common.endpoint.StatusCode
import social.user.application.UserServiceImpl
import social.user.infrastructure.controller.rest.RESTUserAPIVerticle
import social.user.infrastructure.persitence.sql.UserSQLRepository
import social.utils.docker.DockerTest
import java.io.File
import java.util.concurrent.CountDownLatch

class RESTUserAPIVerticleTest : DockerTest() {
    private val logger = LogManager.getLogger(this::class)
    private val repository = UserSQLRepository()
    private val service = UserServiceImpl(repository)
    private val api = RESTUserAPIVerticle(service)
    private val dockerComposePath = "/social/user/infrastructure/persistence/docker-compose.yml"
    private lateinit var webClient: WebClient
    private lateinit var dockerComposeFile: File
    private lateinit var vertx: Vertx

    @BeforeEach
    fun setUp() {
        val dockerComposeResource = this::class.java.getResource(dockerComposePath) ?: throw Exception("Resource not found")
        dockerComposeFile = File(dockerComposeResource.toURI())

        executeDockerComposeCmd(dockerComposeFile, "up", "--wait")
        repository.connect("127.0.0.1", "3306", "user", "test_user", "password")

        this.vertx = Vertx.vertx()
        deployVerticle(vertx, this.api)
        deployVerticle(vertx, this.service)

        webClient = WebClient.create(vertx, WebClientOptions().setDefaultPort(8080).setDefaultHost("localhost"))
    }

    private fun deployVerticle(
        vertx: Vertx,
        verticle: Verticle
    ) {
        val latch = CountDownLatch(1)
        vertx.deployVerticle(verticle).onComplete {
            latch.countDown()
            if (it.succeeded()) {
                logger.info("Verticle '{}' started", verticle.javaClass.simpleName)
            } else {
                logger.error("Failed to start verticle '{}': '{}'", verticle.javaClass.simpleName, it.cause().message)
            }
        }
        latch.await()
    }

    @AfterEach
    fun tearDown() {
        // stops and removes the container, also removes the volumes in order to start fresh each time
        executeDockerComposeCmd(dockerComposeFile, "down", "-v")

        val latch = CountDownLatch(1)
        vertx.close().onComplete {
            if (it.succeeded()) {
                logger.info("Vert.x instance closed")
            } else {
                logger.error("Failed to close Vert.x instance:", it.cause())
            }
            latch.countDown()
        }
    }

    @Timeout(5 * 60)
    @Test
    fun addValidUser() {
        lateinit var response: HttpResponse<String>
        val latch = CountDownLatch(1)

        val validUserJson = JsonObject()
            .put("email", "test@gmail.com")
            .put("username", 123.456)

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(validUserJson) { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to add user: '${ar.cause().message}'")
                }
            }

        latch.await()
        assertEquals(StatusCode.CREATED, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun addUserWithNonValidEmail() {
        lateinit var response: HttpResponse<String>
        val latch = CountDownLatch(1)

        val wrongEmailUserJson = JsonObject()
            .put("email", "not-a-valid-email")
            .put("username", "test")

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(wrongEmailUserJson) { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to add user: '${ar.cause().message}'")
                }
            }
        latch.await()
        assertEquals(StatusCode.BAD_REQUEST, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun addUserWithMissingEmail() {
        lateinit var response: HttpResponse<String>
        val latch = CountDownLatch(1)

        val missingEmailUserJson = JsonObject()
            .put("username", "test")

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(missingEmailUserJson) { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to add user: '${ar.cause().message}'")
                }
            }
        latch.await()
        assertEquals(StatusCode.BAD_REQUEST, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun addUserWithMissingUsername() {
        lateinit var response: HttpResponse<String>
        val latch = CountDownLatch(1)

        val missingUsernameUserJson = JsonObject()
            .put("email", "test@gmail.com")

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(missingUsernameUserJson) { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to add user: '${ar.cause().message}'")
                }
            }

        latch.await()
        assertEquals(StatusCode.BAD_REQUEST, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun addDuplicateUser() {
        lateinit var firstUserAddedResponse: HttpResponse<String>
        lateinit var secondUserAddedResponse: HttpResponse<String>
        val completedLatch = CountDownLatch(2)
        val secondRequestLatch = CountDownLatch(1)

        val firstUserJson = JsonObject()
            .put("email", "test@gmail.com")
            .put("username", "test1")
        val secondUserJson = JsonObject()
            .put("email", "test@gmail.com")
            .put("username", "test2")

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(firstUserJson) { ar ->
                completedLatch.countDown()
                secondRequestLatch.countDown()
                if (ar.succeeded()) {
                    firstUserAddedResponse = ar.result()
                } else {
                    fail("Failed to add user: '${ar.cause().message}'")
                }
            }

        secondRequestLatch.await()

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(secondUserJson) { ar ->
                completedLatch.countDown()
                if (ar.succeeded()) {
                    secondUserAddedResponse = ar.result()
                } else {
                    fail("Failed to add user: '${ar.cause().message}'")
                }
            }

        completedLatch.await()
        assertAll(
            { assertEquals(StatusCode.CREATED, firstUserAddedResponse.statusCode()) },
            { assertEquals(StatusCode.FORBIDDEN, secondUserAddedResponse.statusCode()) }
        )
    }

    @Timeout(5 * 60)
    @Test
    fun getUser() {
        lateinit var response: HttpResponse<String>
        val completedLatch = CountDownLatch(2)
        val secondRequestLatch = CountDownLatch(1)

        val userJson = JsonObject()
            .put("email", "test@gmail.com")
            .put("username", "test")

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(userJson) {
                completedLatch.countDown()
                secondRequestLatch.countDown()
            }

        secondRequestLatch.await()

        webClient.get(Endpoint.USER)
            .addQueryParam("email", "test@gmail.com")
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .send { ar ->
                completedLatch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to get user: '${ar.cause().message}'")
                }
            }

        completedLatch.await()
        assertAll(
            { assertEquals(StatusCode.OK, response.statusCode()) },
            { assertEquals(userJson, JsonObject(response.body())) }
        )
    }

    @Timeout(5 * 60)
    @Test
    fun getUserWithoutEmailParam() {
        lateinit var response: HttpResponse<String>
        val latch = CountDownLatch(1)

        webClient.get(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .send { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to get user: '${ar.cause().message}'")
                }
            }

        latch.await()
        assertEquals(StatusCode.BAD_REQUEST, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun getNonExistingUser() {
        lateinit var response: HttpResponse<String>
        val latch = CountDownLatch(1)

        webClient.get(Endpoint.USER)
            .addQueryParam("email", "test@gmail.com")
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .send { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to get user: '${ar.cause().message}'")
                }
            }

        latch.await()
        assertEquals(StatusCode.NOT_FOUND, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun updateUser() {
        lateinit var response: HttpResponse<String>
        val completedLatch = CountDownLatch(2)
        val secondRequestLatch = CountDownLatch(1)

        val userJson = JsonObject()
            .put("email", "test@gmail.com")
            .put("username", "test")

        val updatedUserJson = JsonObject()
            .put("email", "test@gmail.com")
            .put("username", "updated")

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(userJson) {
                completedLatch.countDown()
                secondRequestLatch.countDown()
            }

        secondRequestLatch.await()

        webClient.put(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(updatedUserJson) { ar ->
                completedLatch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to update user: '${ar.cause().message}'")
                }
            }

        completedLatch.await()
        assertEquals(StatusCode.OK, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun updateNonExistingUser() {
        lateinit var response: HttpResponse<String>
        val latch = CountDownLatch(1)

        val updatedUserJson = JsonObject()
            .put("email", "test@gmail.com")
            .put("username", "updated")

        webClient.put(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(updatedUserJson) { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to update user: '${ar.cause().message}'")
                }
            }

        latch.await()
        assertEquals(StatusCode.FORBIDDEN, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun updateUserWithoutSpecifyingEmail() {
        lateinit var response: HttpResponse<String>
        val completedLatch = CountDownLatch(2)
        val secondRequestLatch = CountDownLatch(1)

        val userJson = JsonObject()
            .put("username", "test")

        val updatedUserJson = JsonObject()
            .put("username", "updated")

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(userJson) {
                completedLatch.countDown()
                secondRequestLatch.countDown()
            }

        secondRequestLatch.await()

        webClient.put(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(updatedUserJson) { ar ->
                completedLatch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to update user: '${ar.cause().message}'")
                }
            }

        completedLatch.await()
        assertEquals(StatusCode.BAD_REQUEST, response.statusCode())
    }

    @Timeout(5 * 60)
    @Test
    fun updateUserWithoutSpecifyingUsername() {
        lateinit var response: HttpResponse<String>
        val completedLatch = CountDownLatch(2)
        val secondRequestLatch = CountDownLatch(1)

        val userJson = JsonObject()
            .put("email", "test@gmail.com")
            .put("username", "test")

        val updatedUserJson = JsonObject()
            .put("email", "test@gmail.com")

        webClient.post(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(userJson) {
                completedLatch.countDown()
                secondRequestLatch.countDown()
            }

        secondRequestLatch.await()

        webClient.put(Endpoint.USER)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(updatedUserJson) { ar ->
                completedLatch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    fail("Failed to update user: '${ar.cause().message}'")
                }
            }

        completedLatch.await()
        assertEquals(StatusCode.BAD_REQUEST, response.statusCode())
    }
}
