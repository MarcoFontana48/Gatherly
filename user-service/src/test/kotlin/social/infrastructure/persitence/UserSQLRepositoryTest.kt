package social.infrastructure.persitence

import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import social.domain.User
import social.infrastructure.persitence.sql.UserSQLRepository
import java.io.File
import java.sql.SQLIntegrityConstraintViolationException

object UserSQLRepositoryTest {
    private val logger = LogManager.getLogger(UserSQLRepositoryTest::class)
    private var repository = UserSQLRepository()
    private const val MINUTE = 60_000L
    private val user1 = User.of("test1@gmail.com", "test1")
    private val user2 = User.of("test2@gmail.com", "test2")

    /**
     * Starts a process with the given command line and working directory.
     *
     * @param workDir the working directory of the process
     * @param cmdLine the command line of the process
     * @return the started process
     */
    private fun startProcess(workDir: File, vararg cmdLine: String): Process {
        logger.trace("Starting process on dir '{}', with command line: '{}'", workDir, cmdLine.contentToString())
        val prefix: String = UserSQLRepositoryTest::class.java.getName() + "-" + cmdLine.contentHashCode()
        val stdOut = File.createTempFile("$prefix-stdout", ".txt")
        stdOut.deleteOnExit()
        val stdErr = File.createTempFile("$prefix-stderr", ".txt")
        stdErr.deleteOnExit()
        return ProcessBuilder(*cmdLine)
            .redirectOutput(ProcessBuilder.Redirect.to(stdOut))
            .redirectError(ProcessBuilder.Redirect.to(stdErr))
            .directory(workDir)
            .start()
    }

    @JvmStatic
    @BeforeAll
    fun setUpAll() {
        logger.trace("Tearing down containers before testing, if they are running...")
        // Stop and remove the container if it is running
        var process = startProcess(File(".."), "docker", "stop", "user-sql-db")
        process.waitFor()
        process = startProcess(File(".."), "docker", "rm", "user-sql-db")
        process.waitFor()
        startProcess(File(".."), "docker", "build", "-t", "user-sql-db", "-f", "Dockerfile", ".")
    }

    @BeforeEach
    fun setUp() {
        startProcess(
            File(".."),
            "docker", "run", "-d", "-p", "3306:3306", "--name", "user-sql-db", "user-sql-db"
        )
        Thread.sleep(7 * MINUTE)
        repository.connect("user-sql-db", "3306", "user", "root", "password")
    }

    @AfterEach
    fun tearDown() {
        // Stop and remove the container
        var process = startProcess(File(".."), "docker", "stop", "user-sql-db")
        process.waitFor()
        process = startProcess(File(".."), "docker", "rm", "user-sql-db")
        process.waitFor()
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
