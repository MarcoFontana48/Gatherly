package social.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class UserTest {
    private val user = User(UserID("prova@gmail.com"), "username")

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

    @Test
    fun testUserCreation() {
        val actual = UserFactory.userOf("prova@gmail.com", "username")
        assertEquals(user, actual)
    }

    @Test
    fun testFailingUserCreation() {
        assertThrows<IllegalArgumentException> { UserFactory.userOf("not.an.email", "username") }
    }
}