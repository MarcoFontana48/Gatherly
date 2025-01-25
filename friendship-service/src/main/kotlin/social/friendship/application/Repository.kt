package social.friendship.application

import social.common.ddd.Repository
import social.friendship.domain.Friendship
import social.friendship.domain.Friendship.FriendshipID
import social.friendship.domain.FriendshipRequest
import social.friendship.domain.FriendshipRequest.FriendshipRequestID
import social.friendship.domain.Message
import social.friendship.domain.Message.MessageID
import social.friendship.domain.User
import social.friendship.domain.User.UserID

/**
 * Interface to represent a repository that needs to connect to a database using the given parameters.
 */
interface ConnectableRepository {
    fun connect(host: String, port: String, dbName: String, username: String, password: String)
}

/**
 * Repository to manage users.
 */
interface UserRepository : Repository<UserID, User>, ConnectableRepository

/**
 * Repository to manage friendship requests.
 */
interface FriendshipRequestRepository : Repository<FriendshipRequestID, FriendshipRequest>, ConnectableRepository {
    fun getAllFriendshipRequestsOf(userId: UserID): Iterable<FriendshipRequest>
}

/**
 * Repository to manage friendships.
 */
interface FriendshipRepository : Repository<FriendshipID, Friendship>, ConnectableRepository {
    fun findAllFriendsOf(userID: UserID): Iterable<User>
}

/**
 * Repository to manage messages.
 */
interface MessageRepository : Repository<MessageID, Message>, ConnectableRepository {
    fun findAllMessagesReceivedBy(userID: UserID): Iterable<Message>
    fun findAllMessagesExchangedBetween(user1: UserID, user2: UserID): Iterable<Message>
}
