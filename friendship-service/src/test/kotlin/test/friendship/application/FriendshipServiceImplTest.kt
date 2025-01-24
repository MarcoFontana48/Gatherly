package test.friendship.application

import io.vertx.core.Vertx
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import social.friendship.application.FriendshipServiceVerticle
import social.friendship.domain.Friendship
import social.friendship.domain.FriendshipRequest
import social.friendship.domain.Message
import social.friendship.domain.User
import social.friendship.infrastructure.controller.event.KafkaFriendshipProducerVerticle
import social.friendship.infrastructure.persistence.sql.FriendshipRequestSQLRepository
import social.friendship.infrastructure.persistence.sql.FriendshipSQLRepository
import social.friendship.infrastructure.persistence.sql.MessageSQLRepository
import social.friendship.infrastructure.persistence.sql.UserSQLRepository
import kotlin.test.assertEquals

class FriendshipServiceImplTest {
    private val user = User.Companion.of("id")
    private val nonExistingUser = User.Companion.of("nonExistingUserId")
    private val userTo = User.Companion.of("userToID")
    private val userFrom = User.Companion.of("userFromID")
    private val friendship = Friendship.Companion.of(userTo, userFrom)
    private val nonExistingUserTo = User.Companion.of("nonExistingUserToID")
    private val nonExistingUserFrom = User.Companion.of("nonExistingUserFromID")
    private val nonExistingFriendship = Friendship.Companion.of(nonExistingUserTo, nonExistingUserFrom)
    private val friendshipRequest = FriendshipRequest.Companion.of(userTo, userFrom)
    private val nonExistingFriendshipRequest = FriendshipRequest.Companion.of(nonExistingUserTo, nonExistingUserFrom)
    private val sender = userTo
    private val receiver = userFrom
    private val nonExistingSender = nonExistingUserTo
    private val nonExistingReceiver = nonExistingUserFrom
    private val message = Message.Companion.of(sender, receiver, "content")
    private val nonExistingMessage = Message.Companion.of(nonExistingSender, nonExistingReceiver, "content")
    private lateinit var closeable: AutoCloseable
    @Mock
    private val userRepository = UserSQLRepository()
    @Mock
    private val friendshipRepository = FriendshipSQLRepository()
    @Mock
    private val friendshipRequestRepository = FriendshipRequestSQLRepository()
    @Mock
    private val messageRepository = MessageSQLRepository()
    @Mock
    private val kafkaProducer = KafkaFriendshipProducerVerticle()
    private val friendshipService = FriendshipServiceVerticle(
        userRepository,
        friendshipRepository,
        friendshipRequestRepository,
        messageRepository,
        kafkaProducer,
        shouldConnectToDB = false,
    )

    @BeforeEach
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        friendshipService.apply {
            this::class.java.getDeclaredField("userRepository").apply {
                isAccessible = true
                set(friendshipService, userRepository)
            }
            this::class.java.getDeclaredField("friendshipRepository").apply {
                isAccessible = true
                set(friendshipService, friendshipRepository)
            }
            this::class.java.getDeclaredField("friendshipRequestRepository").apply {
                isAccessible = true
                set(friendshipService, friendshipRequestRepository)
            }
            this::class.java.getDeclaredField("messageRepository").apply {
                isAccessible = true
                set(friendshipService, messageRepository)
            }
            this::class.java.getDeclaredField("kafkaProducer").apply {
                isAccessible = true
                set(friendshipService, kafkaProducer)
            }
        }

        Mockito.`when`(friendshipRepository.findById(friendship.id)).thenReturn(friendship)
        Mockito.`when`(friendshipRepository.deleteById(friendship.id)).thenReturn(friendship)
        Mockito.`when`(friendshipRepository.findAllFriendsOf(user.id)).thenReturn(listOf(user))

        Mockito.`when`(friendshipRequestRepository.findById(friendshipRequest.id)).thenReturn(friendshipRequest)
        Mockito.`when`(friendshipRequestRepository.deleteById(friendshipRequest.id)).thenReturn(friendshipRequest)
        Mockito.`when`(friendshipRequestRepository.getAllFriendshipRequestsOf(user.id)).thenReturn(listOf(friendshipRequest))

        Mockito.`when`(messageRepository.findById(message.id)).thenReturn(message)
        Mockito.`when`(messageRepository.findAllMessagesReceivedBy(receiver.id)).thenReturn(listOf(message))
        Mockito.`when`(messageRepository.findAllMessagesExchangedBetween(sender.id, receiver.id)).thenReturn(listOf(message))

        Mockito.`when`(userRepository.findById(user.id)).thenReturn(user)

        val vertx = Vertx.vertx()
        vertx.deployVerticle(friendshipService)
        vertx.deployVerticle(kafkaProducer)
    }

    @AfterEach
    fun tearDown() {
        closeable.close()
    }

    @Test
    fun addUser() {
        assertDoesNotThrow { friendshipService.addUser(user) }
    }

    @Test
    fun getUser() {
        val actual = friendshipService.getUser(user.id)
        assertEquals(user, actual)
    }

    @Test
    fun getNonExistentUser() {
        val actual = friendshipService.getUser(nonExistingUser.id)
        assertEquals(null, actual)
    }

    @Test
    fun addFriendship() {
        assertDoesNotThrow { friendshipService.addFriendship(friendship) }
    }

    @Test
    fun getFriendship() {
        val actual = friendshipService.getFriendship(friendship.id)
        assertEquals(friendship, actual)
    }

    @Test
    fun getNonExistentFriendship() {
        val actual = friendshipService.getFriendship(nonExistingFriendship.id)
        assertEquals(null, actual)
    }

    @Test
    fun deleteFriendship() {
        val actual = friendshipService.deleteFriendship(friendship.id)
        assertEquals(friendship, actual)
    }

    @Test
    fun deleteNonExistingFriendship() {
        assertThrows<IllegalArgumentException> { friendshipService.deleteFriendship(nonExistingFriendship.id) }
    }

    @Test
    fun getAllFriendsOfUser() {
        val actual = friendshipService.getAllFriendsByUserId(user.id)
        assertEquals(listOf(user), actual)
    }

    @Test
    fun getAllFriendsOfNonExistingUser() {
        val actual = friendshipService.getAllFriendsByUserId(nonExistingUser.id)
        assertEquals(emptyList(), actual)
    }

    @Test
    fun addFriendshipRequest() {
        assertDoesNotThrow { friendshipService.addFriendshipRequest(friendshipRequest) }
    }

    @Test
    fun getFriendshipRequest() {
        val actual = friendshipService.getFriendshipRequest(friendshipRequest.id)
        assertEquals(friendshipRequest, actual)
    }

    @Test
    fun getNonExistentFriendshipRequest() {
        val actual = friendshipService.getFriendshipRequest(nonExistingFriendshipRequest.id)
        assertEquals(null, actual)
    }

    @Test
    fun rejectFriendshipRequestOfUser() {
        val actual = friendshipService.rejectFriendshipRequest(friendshipRequest)
        assertEquals(friendshipRequest, actual)
    }

    @Test
    fun rejectNonExistentFriendshipRequest() {
        assertThrows<IllegalArgumentException> { friendshipService.rejectFriendshipRequest(nonExistingFriendshipRequest) }
    }

    @Test
    fun acceptFriendshipRequest() {
        assertDoesNotThrow { friendshipService.acceptFriendshipRequest(friendshipRequest) }
    }

    @Test
    fun acceptFriendshipRequestOfNonExistingUser() {
        assertThrows<IllegalArgumentException> { friendshipService.acceptFriendshipRequest(nonExistingFriendshipRequest) }
    }

    @Test
    fun getAllFriendshipRequestsByUserId() {
        val actual = friendshipService.getAllFriendshipRequestsByUserId(user.id)
        assertEquals(listOf(friendshipRequest), actual)
    }

    @Test
    fun getAllFriendshipRequestsByNonExistingUserId() {
        val actual = friendshipService.getAllFriendshipRequestsByUserId(nonExistingUser.id)
        assertEquals(emptyList(), actual)
    }

    @Test
    fun addMessage() {
        assertDoesNotThrow { friendshipService.addMessage(message) }
    }

    @Test
    fun getMessage() {
        val actual = friendshipService.getMessage(message.id)
        assertEquals(message, actual)
    }

    @Test
    fun getNonExistentMessage() {
        val actual = friendshipService.getMessage(nonExistingMessage.id)
        assertEquals(null, actual)
    }

    @Test
    fun receivedMessage() {
        assertDoesNotThrow { friendshipService.receivedMessage(message) }
    }

    @Test
    fun sentMessage() {
        assertDoesNotThrow { friendshipService.sentMessage(message) }
    }

    @Test
    fun getAllMessagesReceivedByUserId() {
        val actual = friendshipService.getAllMessagesReceivedByUserId(receiver.id)
        assertEquals(listOf(message), actual)
    }

    @Test
    fun getAllMessagesExchangedBetween() {
        val actual = friendshipService.getAllMessagesExchangedBetween(sender.id, receiver.id)
        assertEquals(listOf(message), actual)
    }
}
