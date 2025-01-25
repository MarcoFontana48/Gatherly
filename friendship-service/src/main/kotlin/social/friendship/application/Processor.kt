package social.friendship.application

import social.friendship.domain.Friendship
import social.friendship.domain.Friendship.FriendshipID
import social.friendship.domain.FriendshipRequest
import social.friendship.domain.FriendshipRequest.FriendshipRequestID
import social.friendship.domain.Message
import social.friendship.domain.Message.MessageID
import social.friendship.domain.User
import social.friendship.domain.User.UserID

/**
 * Interface to represent a class that is able to process data.
 */
interface Processor

/**
 * Interface to represent a friendship processor, that is able to process data about friendships.
 */
interface FriendshipProcessor : Processor {
    fun addFriendship(friendship: Friendship)
    fun getFriendship(friendshipID: FriendshipID): Friendship?
    fun deleteFriendship(friendshipID: FriendshipID): Friendship?
    fun getAllFriendships(): Array<Friendship>
    fun getAllFriendsByUserId(userID: UserID): Iterable<User>
}

/**
 * Interface to represent a friendship request processor, that is able to process data about friendship requests.
 */
interface FriendshipRequestProcessor : Processor {
    fun addFriendshipRequest(friendshipRequest: FriendshipRequest)
    fun getFriendshipRequest(friendshipRequestID: FriendshipRequestID): FriendshipRequest?
    fun rejectFriendshipRequest(friendshipRequest: FriendshipRequest): FriendshipRequest?
    fun getAllFriendshipRequests(): Array<FriendshipRequest>
    fun acceptFriendshipRequest(request: FriendshipRequest)
    fun getAllFriendshipRequestsByUserId(userID: UserID): Iterable<FriendshipRequest>
}

/**
 * Interface to represent a message processor, that is able to process data about messages.
 */
interface MessageProcessor : Processor {
    fun addMessage(message: Message)
    fun receivedMessage(message: Message)
    fun sentMessage(message: Message)
    fun getMessage(messageID: MessageID): Message?
    fun deleteMessage(messageID: MessageID): Message?
    fun getAllMessages(): Array<Message>
    fun getAllMessagesReceivedByUserId(userID: UserID): Iterable<Message>
    fun getAllMessagesExchangedBetween(user1Id: UserID, user2Id: UserID): Iterable<Message>
}

/**
 * Interface to represent a user processor, that is able to process data about users.
 */
interface UserProcessor : Processor {
    fun addUser(user: User)
    fun getUser(userID: UserID): User?
}
