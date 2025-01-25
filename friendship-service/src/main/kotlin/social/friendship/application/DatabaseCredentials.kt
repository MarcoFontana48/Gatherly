package social.friendship.social.friendship.application

/**
 * Database credentials
 * @property host the host of the database
 * @property port the port of the database
 * @property dbName the name of the database
 * @property username the username to connect to the database
 * @property password the password to connect to the database
 */
interface DatabaseCredentials {
    val host: String
    val port: String
    val dbName: String
    val username: String
    val password: String
}
