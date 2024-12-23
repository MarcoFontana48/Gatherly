package social.user.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class UserTest {
    @Test
    fun testUserCreation() {
        assertDoesNotThrow { User.of("prova@gmail.com", "username") }
    }

    @Test
    fun testEquals() {
        val user1 = User.of("test@outlook.it", "test")
        val user2 = User.of("test@outlook.it", "other")
        assertTrue(user1 == user2)
    }

    @Test
    fun testHashCode() {
        val user1 = User.of("email@gmail.it", "username")
        val user2 = User.of("email@gmail.it", "username")
        assertTrue(user1.hashCode() == user2.hashCode())
    }

    @Test
    fun testFailingUserCreation() {
        assertThrows<IllegalArgumentException> { User.of("not.an.email", "username") }
    }
}
