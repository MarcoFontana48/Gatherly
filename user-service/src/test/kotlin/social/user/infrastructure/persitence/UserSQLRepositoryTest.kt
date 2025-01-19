package social.user.infrastructure.persitence

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import social.user.domain.User
import social.user.infrastructure.persitence.sql.UserSQLRepository
import social.utils.docker.DockerTest
import java.io.File
import java.sql.SQLIntegrityConstraintViolationException

class UserSQLRepositoryTest : DockerTest() {
    private var repository = UserSQLRepository()
    private val user1 = User.of("test1@gmail.com", "test1")
    private val user2 = User.of("test2@gmail.com", "test2")
    private val dockerComposePath = "/social/user/infrastructure/persistence/sql/docker-compose.yml"
    private lateinit var dockerComposeFile: File

    @BeforeEach
    fun setUp() {
        val dockerComposeResource = this::class.java.getResource(dockerComposePath) ?: throw Exception("Resource not found")
        dockerComposeFile = File(dockerComposeResource.toURI())

        executeDockerComposeCmd(dockerComposeFile, "up", "--wait")
        repository.connect("127.0.0.1", "3306", "user", "test_user", "password")
    }

    @AfterEach
    fun tearDown() {
        // stops and removes the container, also removes the volumes in order to start fresh each time
        executeDockerComposeCmd(dockerComposeFile, "down", "-v")
    }

    @Test
    fun save() {
        repository.save(user1)
        repository.findById(user1.id)?.let {
            assertTrue(it == user1)
        }
    }

    @Test
    fun doesNotSaveDoubles() {
        repository.save(user1)
        assertThrows<SQLIntegrityConstraintViolationException> {
            repository.save(user1)
        }
    }

    @Test
    fun doesNotSaveIfSameEmail() {
        repository.save(user1)
        assertThrows<SQLIntegrityConstraintViolationException> {
            repository.save(User.of(user1.email, "otherUsername"))
        }
    }

    @Test
    fun savesIfSameUsername() {
        repository.save(user1)
        val userWithSameUsername = User.of(user2.email, user1.username)
        repository.save(userWithSameUsername)
        val users = repository.findAll().toList()
        assertAll(
            { assertTrue(users.size == 2) },
            { assertTrue(users.contains(user1)) },
            { assertTrue(users.contains(userWithSameUsername)) }
        )
    }

    @Test
    fun deleteById() {
        repository.save(user1)
        val userAfterDeletion = repository.deleteById(user1.id)
        assertTrue(userAfterDeletion == user1)
    }

    @Test
    fun deleteByIdNotFound() {
        val userAfterDeletion = repository.deleteById(user1.id)
        assertTrue(userAfterDeletion == null)
    }

    @Test
    fun findAll() {
        repository.save(user1)
        repository.save(user2)
        val users = repository.findAll().toList()
        assertAll(
            { assertTrue(users.size == 2) },
            { assertTrue(users.contains(user1)) },
            { assertTrue(users.contains(user2)) }
        )
    }

    @Test
    fun update() {
        repository.save(user1)
        val updatedUsername = "newUsername"
        val updatedUser = User.of(user1.email, updatedUsername)
        repository.update(updatedUser)
        val userAfterUpdate = repository.findById(user1.id)
        assertAll(
            { assertTrue(userAfterUpdate?.email == user1.email) },
            { assertTrue(userAfterUpdate?.username == updatedUsername) },
            { assertTrue(userAfterUpdate?.username != user1.username) }
        )
    }
}
