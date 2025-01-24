package social.friendship.infrastructure.persistence.sql

import social.friendship.social.friendship.application.DatabaseCredentials

data class DatabaseCredentialsImpl(
    override val host: String,
    override val port: String,
    override val dbName: String,
    override val username: String,
    override val password: String
) : DatabaseCredentials
