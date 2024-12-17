package social.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun testEquals() {
        val user1 = User(UserID("userID"), "username")
        val user2 = User(UserID("userID"), "username4")
        assertTrue(user1 == user2)
    }

    @Test
    fun testHashCode() {
        val user1 = User(UserID("userID"), "username")
        val user2 = User(UserID("userID"), "username")
        assertTrue(user1.hashCode() == user2.hashCode())
    }
}