package social.friendship.infrastructure.persistence.sql

import org.apache.logging.log4j.LogManager
import social.friendship.application.FriendshipRequestRepository
import social.friendship.domain.FriendshipRequest
import social.friendship.domain.FriendshipRequest.FriendshipRequestID
import social.friendship.domain.User
import java.sql.PreparedStatement

/**
 * SQL repository for friendship requests.
 */
class FriendshipRequestSQLRepository : FriendshipRequestRepository, AbstractSQLRepository() {
    val logger = LogManager.getLogger(this::class)

    /**
     * Find a friendship request by its ID.
     * @param id the ID of the friendship request
     * @return the friendship request if found, null otherwise
     */
    override fun findById(id: FriendshipRequestID): FriendshipRequest? {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Query.SELECT_FRIENDSHIP_REQUEST_BY_ID,
            id.to.value,
            id.from.value
        )
        val result = ps.executeQuery()
        return if (result.next()) {
            FriendshipRequest.of(User.of(result.getString(SQLColumns.FriendshipRequestTable.TO)), User.of(result.getString(SQLColumns.FriendshipRequestTable.FROM)))
        } else {
            null
        }
    }

    /**
     * Save a friendship request.
     * @param entity the friendship request to save
     */
    override fun save(entity: FriendshipRequest) {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Update.INSERT_FRIENDSHIP_REQUEST,
            entity.to.id.value,
            entity.from.id.value
        )
        ps.executeUpdate()
    }

    /**
     * Delete a friendship request by its ID.
     * @param id the ID of the friendship request
     * @return the deleted friendship request if found, null otherwise
     */
    override fun deleteById(id: FriendshipRequestID): FriendshipRequest? {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Update.DELETE_FRIENDSHIP_REQUEST_BY_ID,
            id.to.value,
            id.from.value
        )
        val result = ps.executeUpdate()
        return if (result > 0) {
            FriendshipRequest.of(User.of(id.to), User.of(id.from))
        } else {
            null
        }
    }

    /**
     * Find all friendship requests.
     * @return all friendship requests
     */
    override fun findAll(): Array<FriendshipRequest> {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Query.SELECT_ALL_FRIENDSHIP_REQUESTS
        )
        val result = ps.executeQuery()
        val friendshipRequests = mutableListOf<FriendshipRequest>()
        while (result.next()) {
            friendshipRequests.add(FriendshipRequest.of(User.of(result.getString(SQLColumns.FriendshipRequestTable.TO)), User.of(result.getString(SQLColumns.FriendshipRequestTable.FROM))))
        }
        return friendshipRequests.toTypedArray()
    }

    /**
     * Get all friendship requests of a user.
     * @param userId the ID of the user
     * @return all friendship requests of the user
     */
    override fun getAllFriendshipRequestsOf(userId: User.UserID): Iterable<FriendshipRequest> {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Query.SELECT_FRIENDSHIP_REQUESTS_OF_USER,
            userId.value,
            userId.value,
        )
        val result = ps.executeQuery()
        val friendshipRequests = mutableListOf<FriendshipRequest>()
        while (result.next()) {
            val userTo = User.of(result.getString(SQLColumns.FriendshipRequestTable.TO))
            val userFrom = User.of(result.getString(SQLColumns.FriendshipRequestTable.FROM))
            val friendshipRequest = FriendshipRequest.of(userTo, userFrom)
            friendshipRequests.add(friendshipRequest)
        }
        return friendshipRequests.toList()
    }

    /**
     * Update a friendship request.
     * @param entity the friendship request to update
     */
    override fun update(entity: FriendshipRequest) {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Update.UPDATE_FRIENDSHIP_REQUEST,
            entity.to.id.value,
            entity.from.id.value,
            entity.to.id.value,
            entity.from.id.value
        )
        ps.executeUpdate()
    }
}
