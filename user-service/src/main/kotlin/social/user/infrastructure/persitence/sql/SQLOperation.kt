package social.user.infrastructure.persitence.sql

/**
 * Object to store the SQL operations.
 */
object SQLOperation {
    const val INSERT_USER =
        """
        INSERT INTO user (email, username)
        VALUES (?, ?)
        """

    const val DELETE_USER_BY_ID =
        """
        DELETE FROM user
        WHERE email = ?
        """

    const val UPDATE_USER =
        """
        UPDATE user
        SET username = ?
        WHERE email = ?
        """

    const val SELECT_USER_BY_ID =
        """
        SELECT * FROM user
        WHERE email = ?
        """

    const val SELECT_ALL_USERS =
        """
        SELECT * FROM user
        """
}
