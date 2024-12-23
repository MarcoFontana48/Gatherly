package social.user.infrastructure

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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import social.common.endpoint.Endpoint
import social.common.endpoint.StatusCode
import social.user.application.UserServiceImpl
import social.user.infrastructure.persitence.sql.UserSQLRepository
import java.io.File
import java.util.concurrent.CountDownLatch

object RESTUserAPIVerticleTest {
    private val logger = LogManager.getLogger(this::class)
    private const val MINUTE = 60_000L
    private val repository = UserSQLRepository()
    private val service = UserServiceImpl(repository)
    private val api = RESTUserAPIVerticle(service)
    private lateinit var webClient: WebClient

    /**
     * Starts a process with the given command line and working directory.
     *
     * @param workDir the working directory of the process
     * @param cmdLine the command line of the process
     * @return the started process
     */
    private fun startProcess(workDir: File, vararg cmdLine: String): Process {
        logger.trace("Starting process on dir '{}', with command line: '{}'", workDir, cmdLine.contentToString())
        val prefix: String = RESTUserAPIVerticleTest::class.java.getName() + "-" + cmdLine.contentHashCode()
        val stdOut = File.createTempFile("$prefix-stdout", ".txt")
        stdOut.deleteOnExit()
        val stdErr = File.createTempFile("$prefix-stderr", ".txt")
        stdErr.deleteOnExit()
        return ProcessBuilder(*cmdLine)
            .redirectOutput(ProcessBuilder.Redirect.to(stdOut))
            .redirectError(ProcessBuilder.Redirect.to(stdErr))
            .directory(workDir)
            .start()
    }

    @JvmStatic
    @BeforeAll
    fun setUpAll() {
        logger.trace("Tearing down containers before testing, if they are running...")
        // Stop and remove the container if it is running
        var process = startProcess(File(".."), "docker", "stop", "user-sql-db")
        process.waitFor()
        process = startProcess(File(".."), "docker", "rm", "user-sql-db")
        process.waitFor()
        startProcess(File(".."), "docker", "build", "-t", "user-sql-db", "-f", "Dockerfile", ".")
    }

    @BeforeEach
    fun setUp() {
        startProcess(
            File(".."),
            "docker", "run", "-d", "-p", "3306:3306", "--name", "user-sql-db", "user-sql-db"
        )
        Thread.sleep(7 * MINUTE)

        repository.connect(
            "user-sql-db",
            "3306",
            "user",
            "root",
            "password"
        )

        val vertx = Vertx.vertx()
        val latch = CountDownLatch(1)
        vertx.deployVerticle(api).onComplete {
            latch.countDown()
            if (it.succeeded()) {
                logger.info("User service started")
            } else {
                logger.error("Failed to start user API: '{}'", it.cause().message)
            }
        }
        latch.await()

        webClient = WebClient.create(vertx, WebClientOptions().setDefaultPort(8080).setDefaultHost("localhost"))
    }

    @AfterEach
    fun tearDown() {
        // Stop and remove the container
        var process = startProcess(File(".."), "docker", "stop", "user-sql-db")
        process.waitFor()
        process = startProcess(File(".."), "docker", "rm", "user-sql-db")
        process.waitFor()
    }

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
