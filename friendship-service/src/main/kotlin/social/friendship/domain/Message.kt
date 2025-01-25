package social.friendship.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import social.common.ddd.AggregateRoot
import social.common.ddd.Factory
import social.common.ddd.ID
import social.friendship.domain.Message.MessageID
import java.util.UUID

/**
 * Class to represent a message.
 */
class Message private constructor(
    @JsonProperty("messageId") val messageId: UUID,
    @JsonProperty("sender") val sender: User,
    @JsonProperty("receiver") val receiver: User,
    @JsonProperty("content") val content: String
) : AggregateRoot<MessageID>(MessageID(messageId)) {

    /**
     * Data class to represent the message ID.
     */
    data class MessageID @JsonCreator constructor(
        @JsonProperty("value") val value: UUID
    ) : ID<UUID>(value)

    /**
     * Factory to create a message.
     */
    companion object : Factory<Message> {
        /**
         * Creates a message.
         * @param sender the sender of the message
         * @param receiver the receiver of the message
         * @param content the content of the message
         * @return the message
         */
        fun of(sender: User, receiver: User, content: String): Message {
            return createMessage(UUID.randomUUID(), sender, receiver, content)
        }

        /**
         * Creates a message.
         * @param messageId the message ID
         * @param sender the sender of the message
         * @param receiver the receiver of the message
         * @param content the content of the message
         * @return the message
         */
        fun of(messageId: UUID, sender: User, receiver: User, content: String): Message {
            return createMessage(messageId, sender, receiver, content)
        }

        /**
         * Creates a message checking if the arguments are valid.
         * @param messageId the message ID
         * @param sender the sender of the message
         * @param receiver the receiver of the message
         * @param content the content of the message
         * @return the message
         */
        private fun createMessage(messageId: UUID, sender: User, receiver: User, content: String): Message {
            checkValidMessage(sender, receiver, content)
            return Message(messageId, sender, receiver, content)
        }

        /**
         * Checks if the message is valid.
         * @param sender the sender of the message
         * @param receiver the receiver of the message
         * @param content the content of the message
         * @throws IllegalArgumentException if the message content is blank or the sender is the same as the receiver
         */
        private fun checkValidMessage(
            sender: User,
            receiver: User,
            content: String
        ) {
            if (sender == receiver) {
                throw IllegalArgumentException("User cannot send a message to itself")
            }
            if (content.isBlank()) {
                throw IllegalArgumentException("Message content cannot be blank")
            }
        }
    }
}
