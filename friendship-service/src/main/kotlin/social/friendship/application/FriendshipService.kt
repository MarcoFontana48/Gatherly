package social.friendship.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import org.apache.logging.log4j.LogManager
import social.common.ddd.Service
import social.common.events.FriendshipRemoved
import social.common.events.FriendshipRequestAccepted
import social.common.events.FriendshipRequestRejected
import social.common.events.FriendshipRequestSent
import social.common.events.MessageReceived
import social.common.events.MessageSent
import social.common.events.UserCreated
import social.friendship.domain.Friendship
import social.friendship.domain.Friendship.FriendshipID
import social.friendship.domain.FriendshipRequest
import social.friendship.domain.FriendshipRequest.FriendshipRequestID
import social.friendship.domain.Message
import social.friendship.domain.Message.MessageID
import social.friendship.domain.User
import social.friendship.social.friendship.application.DatabaseCredentials
import java.nio.file.Files
import java.nio.file.Paths

interface FriendshipService : FriendshipProcessor, FriendshipRequestProcessor, MessageProcessor, UserProcessor, Service {
    fun generateSseChannel(response: HttpServerResponse, userId: String)
    fun addWebSocket(webSocket: ServerWebSocket)

    val friendshipEvents: List<String>
        get() = listOf(
            FriendshipRequestAccepted.TOPIC,
            FriendshipRequestRejected.TOPIC,
            FriendshipRequestSent.TOPIC,
            FriendshipRemoved.TOPIC,
            MessageReceived.TOPIC,
            MessageSent.TOPIC,
        )
}

/**
 * FriendshipServiceVerticle is the main entry point for the Friendship Service.
 * It is responsible for handling all the business logic related to friendships, friendship requests, messages and users.
 * It also deploys the KafkaFriendshipProducerVerticle to publish events to the Kafka broker.
 * @param userRepository The repository to interact with the User entity.
 * @param friendshipRepository The repository to interact with the Friendship entity.
 * @param friendshipRequestRepository The repository to interact with the FriendshipRequest entity.
 * @param messageRepository The repository to interact with the Message entity.
 * @param credentials The database credentials to connect to the MySQL database.
 * @param shouldConnectToDB A flag to determine if the service should connect to the database, used for testing purposes.
 */
class FriendshipServiceVerticle(
    private val userRepository: UserRepository,
    private val friendshipRepository: FriendshipRepository,
    private val friendshipRequestRepository: FriendshipRequestRepository,
    private val messageRepository: MessageRepository,
    private val kafkaProducer: EventBrokerProducerVerticle,
    private val credentials: DatabaseCredentials? = null,
    shouldConnectToDB: Boolean? = true,
) : FriendshipService, AbstractVerticle() {
    private val logger = LogManager.getLogger(this::class)
    private val clients = mutableSetOf<ServerWebSocket>()
    private val responses = mutableMapOf<String, HttpServerResponse>()
    private val mapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }

    init {
        if (shouldConnectToDB == true) connectToDatabase()
    }

    /**
     * Connects to the MySQL database using the provided credentials or the default credentials given by the environment
     * variables.
     */
    fun connectToDatabase() {
        credentials?.let {
            connectToDatabaseWith(it)
        } ?: connectToDatabaseWithDefaultCredentials()
    }

    /**
     * Connects to the MySQL database using the provided credentials.
     * @param credentials The database credentials to connect to the MySQL database.
     */
    private fun connectToDatabaseWith(credentials: DatabaseCredentials) {
        listOf(userRepository, friendshipRepository, friendshipRequestRepository, messageRepository).forEach {
            it.connect(credentials.host, credentials.port, credentials.dbName, credentials.username, credentials.password)
        }
    }

    /**
     * Connects to the MySQL database using the provided credentials.
     * @param host The host of the MySQL database.
     * @param port The port of the MySQL database.
     * @param dbName The name of the MySQL database.
     * @param username The username to connect to the MySQL database.
     * @param password The password to connect to the MySQL database.
     */
    private fun connectToDatabaseWith(host: String, port: String, dbName: String, username: String, password: String) {
        listOf(userRepository, friendshipRepository, friendshipRequestRepository, messageRepository).forEach {
            it.connect(host, port, dbName, username, password)
        }
    }

    /**
     * Connects to the MySQL database using the default credentials given by the environment variables.
     */
    private fun connectToDatabaseWithDefaultCredentials() {
        val host = System.getenv("DB_HOST")
        val port = System.getenv("DB_PORT")
        val dbName = System.getenv("MYSQL_DATABASE")
        val username = System.getenv("MYSQL_USER")
        val password = Files.readString(Paths.get("/run/secrets/db_password")).trim()

        connectToDatabaseWith(host, port, dbName, username, password)
    }

    /**
     * Deploys the KafkaFriendshipProducerVerticle to publish events to the Kafka broker.
     */
    override fun start() {
        vertx.deployVerticle(kafkaProducer).onComplete { result ->
            if (result.succeeded()) {
                logger.trace("Kafka producer verticle deployed")
            } else {
                logger.error("Failed to deploy Kafka producer verticle")
            }
        }
    }

    /**
     * Stops the FriendshipServiceVerticle and closes the database connections.
     */
    override fun addFriendship(friendship: Friendship) = friendshipRepository.save(friendship)

    /**
     * Retrieves a friendship by its ID.
     * @param friendshipID The ID of the friendship to retrieve.
     * @return The friendship with the given ID, or null if it does not exist.
     */
    override fun getFriendship(friendshipID: FriendshipID): Friendship? = friendshipRepository.findById(friendshipID)

    /**
     * Deletes a friendship by its ID. Also publishes a FriendshipRemoved event to the Kafka broker and the Vert.x event
     * bus.
     * @param friendshipID The ID of the friendship to delete.
     * @return The deleted friendship, or null if it does not exist.
     */
    override fun deleteFriendship(friendshipID: FriendshipID): Friendship? {
        return friendshipRepository.deleteById(friendshipID)?.also {
            val event = FriendshipRemoved(it.user1.id.value, it.user2.id.value)
            kafkaProducer.publishEvent(event)
            vertx.eventBus().publish(FriendshipRemoved.TOPIC, mapper.writeValueAsString(it))
        } ?: throw IllegalArgumentException("Friendship not found")
    }

    /**
     * Retrieves all friendships.
     * @return An array containing all the friendships.
     */
    override fun getAllFriendships(): Array<Friendship> = friendshipRepository.findAll()

    /**
     * Retrieves all friendships of a user.
     * @param userID The ID of the user whose friendships to retrieve.
     * @return An iterable containing all the friendships of the user.
     */
    override fun getAllFriendsByUserId(userID: User.UserID): Iterable<User> = friendshipRepository.findAllFriendsOf(userID)

    /**
     * Adds a friendship request.
     * @param friendshipRequest The friendship request to add.
     */
    override fun addFriendshipRequest(friendshipRequest: FriendshipRequest) {
        friendshipRequestRepository.save(friendshipRequest).let {
            val event = FriendshipRequestSent(friendshipRequest.to.id.value, friendshipRequest.from.id.value)
            kafkaProducer.publishEvent(event)
            vertx.eventBus().publish(FriendshipRequestSent.TOPIC, mapper.writeValueAsString(friendshipRequest))
        }
    }

    /**
     * Retrieves a friendship request by its ID.
     * @param friendshipRequestID The ID of the friendship request to retrieve.
     * @return The friendship request with the given ID, or null if it does not exist.
     */
    override fun getFriendshipRequest(friendshipRequestID: FriendshipRequestID): FriendshipRequest? = friendshipRequestRepository.findById(friendshipRequestID)

    /**
     * Rejects a friendship request. Also publishes a FriendshipRequestRejected event to the Kafka broker and the Vert.x
     * event bus.
     * @param friendshipRequest The friendship request to reject.
     * @return The rejected friendship request, or null if it does not exist.
     */
    override fun rejectFriendshipRequest(friendshipRequest: FriendshipRequest): FriendshipRequest? {
        return friendshipRequestRepository.deleteById(friendshipRequest.id)?.also {
            val event = FriendshipRequestRejected(it.to.id.value, it.from.id.value)
            kafkaProducer.publishEvent(event)
            vertx.eventBus().publish(FriendshipRequestRejected.TOPIC, mapper.writeValueAsString(it))
        } ?: throw IllegalArgumentException("Friendship request not found")
    }

    /**
     * Retrieves all friendship requests in the repository.
     * @return An array containing all the friendship requests.
     */
    override fun getAllFriendshipRequests(): Array<FriendshipRequest> = friendshipRequestRepository.findAll()

    /**
     * Retrieves all friendship requests of a user.
     * @param userID The ID of the user whose friendship requests to retrieve.
     * @return An iterable containing all the friendship requests of the user.
     */
    override fun getAllFriendshipRequestsByUserId(userID: User.UserID): Iterable<FriendshipRequest> = friendshipRequestRepository.getAllFriendshipRequestsOf(userID)

    /**
     * Accepts a friendship request. Also publishes a FriendshipRequestAccepted event to the Kafka broker and the Vert.x
     * event bus.
     * @param request The friendship request to accept.
     */
    override fun acceptFriendshipRequest(request: FriendshipRequest) {
        friendshipRequestRepository.deleteById(request.id)?.let {
            friendshipRepository.save(Friendship.of(request))
            val event = FriendshipRequestAccepted(request.to.id.value, request.from.id.value)
            kafkaProducer.publishEvent(event)
            vertx.eventBus().publish(FriendshipRequestAccepted.TOPIC, mapper.writeValueAsString(it))
        } ?: throw IllegalArgumentException("Friendship request not found")
    }

    /**
     * Adds a message to the repository.
     * @param message The message to add.
     */
    override fun addMessage(message: Message) = messageRepository.save(message)

    /**
     * Handles a received message. Also publishes a MessageReceived event to the Kafka broker and the Vert.x event bus.
     * @param message The received message.
     */
    override fun receivedMessage(message: Message) {
        messageRepository.save(message)
        val event = MessageReceived(
            message.id.value.toString(),
            message.sender.id.value,
            message.receiver.id.value,
            message.content
        )
        kafkaProducer.publishEvent(event)
        vertx.eventBus().publish(MessageReceived.TOPIC, mapper.writeValueAsString(message))
        clients.filter {
            it.query().split("id=")[1] == message.sender.id.value.toString()
        }.forEach {
            it.writeTextMessage(mapper.writeValueAsString(message))
        }
    }

    /**
     * Handles a sent message. Also publishes a MessageSent event to the Kafka broker and the Vert.x event bus.
     * @param message The sent message.
     */
    override fun sentMessage(message: Message) {
        messageRepository.save(message)
        val event = MessageSent(
            message.id.value.toString(),
            message.sender.id.value,
            message.receiver.id.value,
            message.content
        )
        kafkaProducer.publishEvent(event)
        vertx.eventBus().publish(MessageSent.TOPIC, mapper.writeValueAsString(message))
        clients.filter {
            it.query().split("id=")[1] == message.receiver.id.value.toString()
        }.forEach {
            it.writeTextMessage(mapper.writeValueAsString(message))
        }
    }

    /**
     * Retrieves a message by its ID.
     * @param messageID The ID of the message to retrieve.
     * @return The message with the given ID, or null if it does not exist.
     */
    override fun getMessage(messageID: MessageID): Message? = messageRepository.findById(messageID)

    /**
     * Deletes a message by its ID.
     * @param messageID The ID of the message to delete.
     * @return The deleted message, or null if it does not exist.
     */
    override fun deleteMessage(messageID: MessageID): Message? = messageRepository.deleteById(messageID)

    /**
     * Retrieves all messages in the repository.
     * @return An array containing all the messages.
     */
    override fun getAllMessages(): Array<Message> = messageRepository.findAll()

    /**
     * Retrieves all messages sent by a user.
     * @param userID The ID of the user who sent the messages.
     * @return An iterable containing all the messages sent by the user.
     */
    override fun getAllMessagesReceivedByUserId(userID: User.UserID): Iterable<Message> = messageRepository.findAllMessagesReceivedBy(userID)

    /**
     * Retrieves all messages exchanged between 2 users.
     * @param user1Id The ID of the first user.
     * @param user2Id The ID of the second user.
     * @return An iterable containing all the messages received by the user.
     */
    override fun getAllMessagesExchangedBetween(user1Id: User.UserID, user2Id: User.UserID): Iterable<Message> = messageRepository.findAllMessagesExchangedBetween(user1Id, user2Id)

    /**
     * Adds a user to the repository.
     * @param user The user to add.
     */
    override fun addUser(user: User) {
        userRepository.save(user)
        vertx.eventBus().publish(UserCreated.TOPIC, mapper.writeValueAsString(User.of(user.id)))
    }

    /**
     * Retrieves a user by their ID.
     * @param userID The ID of the user to retrieve.
     * @return The user with the given ID, or null if it does not exist.
     */
    override fun getUser(userID: User.UserID): User? = userRepository.findById(userID)

    override fun generateSseChannel(response: HttpServerResponse, userId: String) {
        response.isChunked = true
        response.putHeader("Content-Type", "text/event-stream")
        response.putHeader("Cache-Control", "no-cache")
        response.putHeader("Connection", "keep-alive")
        response.putHeader("Access-Control-Allow-Origin", "*")
        response.putHeader("Access-Control-Allow-Methods", "GET, OPTIONS")
        response.putHeader("Access-Control-Allow-Headers", "Content-Type")

        responses[userId] = response

        logger.trace("SSE channel generated")
        prepareToSendSseEventsToClient(responses[userId], userId)
    }

    private fun prepareToSendSseEventsToClient(response: HttpServerResponse?, userId: String) {
        friendshipEvents.forEach { topic ->
            logger.trace("Subscribing to Vert.x topic: {}", topic)

            vertx.eventBus().consumer<String>(topic) { message ->
                logger.trace("Received event from topic '{}': {}", topic, message.body())
                val eventJson = JsonObject(message.body())

                when (topic) {
                    FriendshipRequestAccepted.TOPIC -> friendshipRequestAcceptedHandler(eventJson, userId, response, topic)
                    FriendshipRequestRejected.TOPIC -> friendshipRequestRejectedHandler(eventJson, userId, response, topic)
                    FriendshipRequestSent.TOPIC -> friendshipRequestSentHandler(eventJson, userId, response, topic)
                    MessageSent.TOPIC -> messageSentHandler(eventJson, userId, response, topic)
                }
            }
        }
    }

    private fun messageSentHandler(
        eventJson: JsonObject,
        userId: String,
        response: HttpServerResponse?,
        topic: String
    ) {
        if (eventJson.getString("receiver") == userId) {
            sendSseEvent(response, userId, topic, eventJson)
        }
    }

    private fun friendshipRequestSentHandler(
        eventJson: JsonObject,
        userId: String,
        response: HttpServerResponse?,
        topic: String
    ) {
        logger.trace("Friendship request sent handler called with arguments: {}, {}, {}, {}", eventJson, userId, response, topic)
        val receiver = eventJson.getJsonObject("to").getJsonObject("userId").getString("value")
        val sender = eventJson.getJsonObject("from").getJsonObject("userId").getString("value")
        val jsonResponse = JsonObject().put("sender", sender).put("receiver", receiver)
        if (receiver == userId) {
            logger.trace("receiver is equal to userId")
            sendSseEvent(response, userId, topic, jsonResponse)
        } else {
            logger.trace("receiver '{}' is not equal to userId '{}'", receiver, userId)
        }
    }

    private fun friendshipRequestRejectedHandler(
        eventJson: JsonObject,
        userId: String,
        response: HttpServerResponse?,
        topic: String
    ) {
        if (eventJson.getString("sender") == userId) {
            sendSseEvent(response, userId, topic, eventJson)
        }
    }

    private fun friendshipRequestAcceptedHandler(
        eventJson: JsonObject,
        userId: String,
        response: HttpServerResponse?,
        topic: String
    ) {
        if (eventJson.getString("sender") == userId) {
            sendSseEvent(response, userId, topic, eventJson)
        }
    }

    private fun sendSseEvent(response: HttpServerResponse?, userId: String, topic: String, eventJson: JsonObject) {
        logger.trace("Sending SSE Event '{}' to user '{}'", eventJson, userId)

        val eventValue = eventJson.put("topic", topic).toString()
        val formattedMessage = "data: $eventValue\n\n"

        response?.write(formattedMessage)?.onComplete {
            if (it.succeeded()) {
                logger.trace("SSE Event sent to user {}: {}", userId, formattedMessage)
            } else {
                logger.error("Failed to send SSE event to user {}", userId, it.cause())
            }
        } ?: logger.error("Response is null")
    }

    override fun addWebSocket(webSocket: ServerWebSocket) {
        clients.add(webSocket)
        webSocket.closeHandler {
            clients.remove(webSocket)
        }
    }
}
