package social.friendship.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import social.common.ddd.AggregateRoot
import social.common.ddd.Factory
import social.common.ddd.ID
import social.friendship.domain.FriendshipRequest.FriendshipRequestID
import social.friendship.domain.User.UserID

/**
 * Class to represent a friendship request.
 */
class FriendshipRequest private constructor(
    @JsonProperty("to") val to: User,
    @JsonProperty("from") val from: User
) : AggregateRoot<FriendshipRequestID>(FriendshipRequestID(to.id, from.id)) {

    /**
     * Data class to represent the friendship request ID.
     */
    data class FriendshipRequestID @JsonCreator constructor(
        @JsonProperty("to") val to: UserID,
        @JsonProperty("from") val from: UserID
    ) : ID<Pair<UserID, UserID>>(Pair(to, from))

    /**
     * Factory object to create a friendship request.
     */
    companion object : Factory<FriendshipRequest> {
        /**
         * Creates a friendship request.
         *
         * @param to the user to whom the friendship request is sent
         * @param from the user who sends the friendship request
         * @return the friendship request
         * @throws IllegalArgumentException if the user sends a friendship request to itself
         */
        fun of(to: User, from: User): FriendshipRequest {
            if (to == from) {
                throw IllegalArgumentException("User cannot send a friendship request to itself")
            }
            return FriendshipRequest(to, from)
        }
    }
}
