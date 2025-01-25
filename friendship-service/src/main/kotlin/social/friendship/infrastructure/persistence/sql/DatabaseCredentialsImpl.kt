package social.friendship.infrastructure.persistence.sql

import social.friendship.social.friendship.application.DatabaseCredentials

/**
 * Data class to represent database credentials.
 * @param host the database host
 * @param port the database port
 * @param dbName the database name
 * @param username the database username
 * @param password the database password
 */
data class DatabaseCredentialsImpl(
    override val host: String,
    override val port: String,
    override val dbName: String,
    override val username: String,
    override val password: String
) : DatabaseCredentials
