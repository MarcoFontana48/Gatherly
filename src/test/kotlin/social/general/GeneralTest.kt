package social.general

import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import social.common.endpoint.Endpoint
import social.common.endpoint.StatusCode
import social.utils.docker.DockerTest
import social.utils.http.TestRequestUtils.sendGetRequest
import social.utils.http.TestRequestUtils.sendPostRequest
import social.utils.http.TestRequestUtils.sendPutRequest
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch

class GeneralTest : DockerTest() {
    private val userJson1 = JsonObject()
        .put("username", "test")
        .put("email", "test@gmail.com")
    private val userJsonWithDifferentUsername = JsonObject()
        .put("username", "newUsername")
        .put("email", "test@gmail.com")
    private val userJson2 = JsonObject()
        .put("username", "test")
        .put("email", "test2@gmail.com")
    private val dockerComposePath = "docker-compose.yml"
    private lateinit var dockerComposeFile: File
    private lateinit var vertx: Vertx

    @BeforeEach
    fun setUp() {
        dockerComposeFile = File(dockerComposePath)
        executeDockerComposeCmd(dockerComposeFile, "up", "--wait")

        vertx = Vertx.vertx()
    }

    private fun createTestWebClient(vertx: Vertx, port: Int, host: String): WebClient {
        return WebClient.create(vertx, WebClientOptions().setDefaultPort(port).setDefaultHost(host))
    }

    @AfterEach
    fun tearDown() {
        executeDockerComposeCmd(dockerComposeFile, "down", "-v")
    }

    @Test
    fun simulateClientSendingPostUserRequest() {
        val latch = CountDownLatch(2)

        val webClient = createTestWebClient(vertx, 8080, "localhost")
        val postUserResponse = sendPostRequest(userJson1, latch, Endpoint.USER, webClient)

        val emailParamName = "email"
        val getUserResponse = sendGetRequest(emailParamName, userJson1.getString(emailParamName), latch, Endpoint.USER, webClient)
        val user = JsonObject(getUserResponse.body())

        latch.await()
        assertAll(
            { assertEquals(StatusCode.CREATED, postUserResponse.statusCode()) },
            { assertEquals(StatusCode.OK, getUserResponse.statusCode()) },
            { assertEquals(userJson1.getString("email"), user.getString("email")) },
            { assertEquals(userJson1.getString("username"), user.getString("username")) },
        )
    }

    @Test
    fun simulateClientSendingPutUserRequest() {
        val latch = CountDownLatch(3)

        val webClient = createTestWebClient(vertx, 8080, "localhost")
        val postUserResponse = sendPostRequest(userJson1, latch, Endpoint.USER, webClient)

        val putUserResponse = sendPutRequest(userJsonWithDifferentUsername, latch, Endpoint.USER, webClient)

        val emailParamName = "email"
        val getUserResponse = sendGetRequest(emailParamName, userJson1.getString(emailParamName), latch, Endpoint.USER, webClient)
        val userRetrieved = JsonObject(getUserResponse.body())

        latch.await()
        assertAll(
            { assertEquals(StatusCode.CREATED, postUserResponse.statusCode()) },
            { assertEquals(StatusCode.OK, putUserResponse.statusCode()) },
            { assertEquals(StatusCode.OK, getUserResponse.statusCode()) },
            { assertEquals(userJsonWithDifferentUsername.getString("email"), userRetrieved.getString("email")) },
            { assertEquals(userJsonWithDifferentUsername.getString("username"), userRetrieved.getString("username")) },
        )
    }

    @Test
    fun simulateClientSendingFriendshipRequest() {
        val latch = CountDownLatch(8)

        // Creates two users and checks if they are correctly created
        val webClient = createTestWebClient(vertx, 8080, "localhost")
        val postUserResponse = sendPostRequest(userJson1, latch, Endpoint.USER, webClient)
        val postUser2Response = sendPostRequest(userJson2, latch, Endpoint.USER, webClient)

        val emailParamName = "email"
        val getUserResponse = sendGetRequest(emailParamName, userJson1.getString(emailParamName), latch, Endpoint.USER, webClient)
        val userRetrieved = JsonObject(getUserResponse.body())

        val getUser2Response = sendGetRequest(emailParamName, userJson2.getString(emailParamName), latch, Endpoint.USER, webClient)
        val user2Retrieved = JsonObject(getUser2Response.body())

        // Send friendship request.
        // NOTE: in order to this request to be successful, the user has to be stored in the DB of the
        // friendship-service, this is possible only if the previous HTTP request was received by the user-service and
        // it published an event (using kafka) that the friendship-service consumed. Otherwise an error will be shown,
        // because the user is not registered. This test also demonstrates micro-services communication through kafka
        // and the event-driven architecture.
        val webClient2 = createTestWebClient(vertx, 8081, "localhost")
        val friendshipRequestJson = JsonObject()
            .put("to", userJson1.getString("email"))
            .put("from", userJson2.getString("email"))

        val postFriendshipRequest = sendPostRequest(friendshipRequestJson, latch, Endpoint.FRIENDSHIP_REQUEST_SEND, webClient2)
        val getFriendshipRequest = sendGetRequest("id", userJson1.getString(emailParamName), latch, Endpoint.FRIENDSHIP_REQUEST, webClient2)
        val friendshipRequestRetrieved = JsonArray(getFriendshipRequest.body()).getJsonObject(0)

        // Accept friendship request
        val putFriendshipRequestAccept = sendPutRequest(friendshipRequestJson, latch, Endpoint.FRIENDSHIP_REQUEST_ACCEPT, webClient2)

        // Check if friendship is correctly created
        val getFriendship = sendGetRequest("id", userJson1.getString(emailParamName), latch, Endpoint.FRIENDSHIP, webClient2)
        val friendshipRetrieved = JsonArray(getFriendship.body()).getJsonObject(0)

        latch.await()
        assertAll(
            { assertEquals(StatusCode.CREATED, postFriendshipRequest.statusCode()) },
            { assertEquals(StatusCode.OK, getFriendshipRequest.statusCode()) },
            { assertEquals(userJson1.getString("email"), friendshipRequestRetrieved.getJsonObject("to").getJsonObject("id").getString("value")) },
            { assertEquals(userJson2.getString("email"), friendshipRequestRetrieved.getJsonObject("from").getJsonObject("id").getString("value")) },
            { assertEquals(StatusCode.OK, putFriendshipRequestAccept.statusCode()) },
            { assertEquals(StatusCode.OK, getFriendship.statusCode()) },
            { assertEquals(userJson2.getString("email"), friendshipRetrieved.getJsonObject("id").getString("value")) },
        )
    }

    @Test
    fun simulateClientSendingFriendshipRequestAndMessage() {
        val latch = CountDownLatch(10)

        // Creates two users and checks if they are correctly created
        val webClient = createTestWebClient(vertx, 8080, "localhost")
        val postUserResponse = sendPostRequest(userJson1, latch, Endpoint.USER, webClient)
        val postUser2Response = sendPostRequest(userJson2, latch, Endpoint.USER, webClient)

        val emailParamName = "email"
        val getUserResponse = sendGetRequest(emailParamName, userJson1.getString(emailParamName), latch, Endpoint.USER, webClient)
        val userRetrieved = JsonObject(getUserResponse.body())

        val getUser2Response = sendGetRequest(emailParamName, userJson2.getString(emailParamName), latch, Endpoint.USER, webClient)
        val user2Retrieved = JsonObject(getUser2Response.body())

        // Send friendship request.
        val webClient2 = createTestWebClient(vertx, 8081, "localhost")
        val friendshipRequestJson = JsonObject()
            .put("to", userJson1.getString("email"))
            .put("from", userJson2.getString("email"))

        val postFriendshipRequest = sendPostRequest(friendshipRequestJson, latch, Endpoint.FRIENDSHIP_REQUEST_SEND, webClient2)
        val getFriendshipRequest = sendGetRequest("id", userJson1.getString(emailParamName), latch, Endpoint.FRIENDSHIP_REQUEST, webClient2)
        val friendshipRequestRetrieved = JsonArray(getFriendshipRequest.body()).getJsonObject(0)

        // Accept friendship request
        val putFriendshipRequestAccept = sendPutRequest(friendshipRequestJson, latch, Endpoint.FRIENDSHIP_REQUEST_ACCEPT, webClient2)

        // Check if friendship is correctly created
        val getFriendship = sendGetRequest("id", userJson1.getString(emailParamName), latch, Endpoint.FRIENDSHIP, webClient2)
        val friendshipRetrieved = JsonArray(getFriendship.body()).getJsonObject(0)

        // Send a message
        val messageJson = JsonObject()
            .put("messageId", UUID.randomUUID().toString())
            .put("sender", userJson1.getString("email"))
            .put("receiver", userJson2.getString("email"))
            .put("content", "Hello, how are you?")
        val postMessage = sendPostRequest(messageJson, latch, Endpoint.MESSAGE_SEND, webClient2)
        val getMessage = sendGetRequest("id", userJson2.getString(emailParamName), latch, Endpoint.MESSAGE_RECEIVE, webClient2)
        val messageRetrieved = JsonArray(getMessage.body()).getJsonObject(0)

        val messageJson2 = JsonObject()
            .put("messageId", UUID.randomUUID().toString())
            .put("sender", userJson2.getString("email"))
            .put("receiver", userJson1.getString("email"))
            .put("content", "Fine, thanks!")
        val postMessage2 = sendPostRequest(messageJson2, latch, Endpoint.MESSAGE_SEND, webClient2)
        val getMessage2 = sendGetRequest("id", userJson1.getString(emailParamName), latch, Endpoint.MESSAGE_RECEIVE, webClient2)
        val messageRetrieved2 = JsonArray(getMessage2.body()).getJsonObject(0)

        latch.await()
        assertAll(
            { assertEquals(StatusCode.CREATED, postMessage.statusCode()) },
            { assertEquals(StatusCode.OK, getMessage.statusCode()) },
            { assertEquals(messageJson.getString("messageId"), messageRetrieved.getString("messageId")) },
            { assertEquals(messageJson.getString("sender"), messageRetrieved.getJsonObject("sender").getJsonObject("id").getString("value")) },
            { assertEquals(messageJson.getString("receiver"), messageRetrieved.getJsonObject("receiver").getJsonObject("id").getString("value")) },
            { assertEquals(messageJson.getString("content"), messageRetrieved.getString("content")) },
            { assertEquals(StatusCode.CREATED, postMessage2.statusCode()) },
            { assertEquals(StatusCode.OK, getMessage2.statusCode()) },
            { assertEquals(messageJson2.getString("messageId"), messageRetrieved2.getString("messageId")) },
            { assertEquals(messageJson2.getString("sender"), messageRetrieved2.getJsonObject("sender").getJsonObject("id").getString("value")) },
            { assertEquals(messageJson2.getString("receiver"), messageRetrieved2.getJsonObject("receiver").getJsonObject("id").getString("value")) },
            { assertEquals(messageJson2.getString("content"), messageRetrieved2.getString("content")) },
        )
    }
}
