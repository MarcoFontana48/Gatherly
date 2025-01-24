package test.user.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import social.user.domain.User

class UserTest {
    @Test
    fun testUserCreation() {
        assertDoesNotThrow { User.Companion.of("prova@gmail.com", "username") }
    }

    @Test
    fun testEquals() {
        val user1 = User.Companion.of("test@outlook.it", "social/user/test")
        val user2 = User.Companion.of("test@outlook.it", "other")
        assertTrue(user1 == user2)
    }

    @Test
    fun testHashCode() {
        val user1 = User.Companion.of("email@gmail.it", "username")
        val user2 = User.Companion.of("email@gmail.it", "username")
        assertTrue(user1.hashCode() == user2.hashCode())
    }

    @Test
    fun testFailingUserCreation() {
        assertThrows<IllegalArgumentException> { User.Companion.of("not.an.email", "username") }
    }
}
