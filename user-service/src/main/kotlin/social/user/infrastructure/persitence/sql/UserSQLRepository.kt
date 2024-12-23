package social.user.infrastructure.persitence.sql

import com.mysql.cj.jdbc.exceptions.CommunicationsException
import org.apache.logging.log4j.LogManager
import social.common.ddd.Repository
import social.user.domain.User
import social.user.domain.User.UserID
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLIntegrityConstraintViolationException

class UserSQLRepository : Repository<UserID, User> {
    private val logger = LogManager.getLogger(UserSQLRepository::class)
    private lateinit var connection: Connection

    fun connect(host: String, port: String, database: String, username: String, password: String) {
        logger.trace(
            "Connecting to database with credentials:\n" +
                "host={},\n" +
                "port={},\n" +
                "database={},\n" +
                "username={}",
            host, port, database, username
        )
        connection = SQLUtils.mySQLConnection(host, port, database, username, password)
    }

    override fun findById(id: UserID): User? {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.SELECT_USER_BY_ID,
            id.value
        )
        val rs = ps.executeQuery()
        return if (rs.next()) {
            User.of(rs.getString(SQLColumns.EMAIL), rs.getString(SQLColumns.USERNAME))
        } else {
            null
        }
    }

    override fun save(entity: User) {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.INSERT_USER,
            entity.email,
            entity.username
        )
        ps.executeUpdate()
    }

    override fun deleteById(id: UserID): User? {
        val userToDelete = findById(id) ?: return null
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.DELETE_USER_BY_ID,
            id.value
        )
        val result = ps.executeUpdate()
        return if (result > 0) {
            userToDelete
        } else {
            null
        }
    }

    override fun findAll(): Array<User> {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.SELECT_ALL_USERS
        )
        val rs = ps.executeQuery()
        val users = mutableListOf<User>()
        while (rs.next()) {
            users.add(User.of(rs.getString(SQLColumns.EMAIL), rs.getString(SQLColumns.USERNAME)))
        }
        return users.toTypedArray()
    }

    override fun update(entity: User) {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.UPDATE_USER,
            entity.username,
            entity.email
        )
        if (ps.executeUpdate() == 0) {
            throw SQLIntegrityConstraintViolationException("no rows affected")
        }
    }
}

object SQLUtils {
    private val logger = LogManager.getLogger(SQLUtils::class)

    fun prepareStatement(connection: Connection, sqlStatement: String, vararg params: Any): PreparedStatement {
        val ps = connection.prepareStatement(sqlStatement)
        params.forEachIndexed { index, param ->
            ps.setObject(index + 1, param)
        }
        return ps
    }

    fun mySQLConnection(
        host: String,
        port: String,
        database: String,
        username: String,
        password: String
    ): Connection {
        val url = "jdbc:mysql://$host:$port/$database"
        logger.trace("Attempting to connect to database with URL: {}", url)
        try {
            val conn: Connection = DriverManager.getConnection(url, username, password)
            logger.trace("Connection established successfully")
            return conn
        } catch (e: CommunicationsException) {
            logger.error("Failed to connect to database with URL: {}: {}", url, e)
            throw e
        }
    }
}
