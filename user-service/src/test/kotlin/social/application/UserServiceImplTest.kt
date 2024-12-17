package social.application

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import social.ddd.Repository
import social.domain.User
import social.domain.UserID
import social.infrastructure.persitence.UserSQLRepository
import kotlin.test.assertEquals

class UserServiceImplTest {
    private val repository: Repository<UserID, User> = mock(UserSQLRepository::class.java)
    private val service: UserService = UserServiceImpl(repository)
    private val userID = UserID("userID")
    private val user = User(userID, "username")
    private val nonExistingUserID = UserID("nonExistingUserID")

    init {
        `when`(repository.findById(userID)).thenReturn(user)
    }

    @Test
    fun addUser() {
        service.addUser(user)
        assertDoesNotThrow { repository.save(user) }
    }

    @Test
    fun getUser() {
        val actual = service.getUser(userID)
        assertEquals(user, actual)
    }

    @Test
    fun getNonExistingUser() {
        val actual = service.getUser(nonExistingUserID)
        assertEquals(null, actual)
    }

    @Test
    fun updateUser() {
        assertDoesNotThrow { service.updateUser(user) }
    }
}
