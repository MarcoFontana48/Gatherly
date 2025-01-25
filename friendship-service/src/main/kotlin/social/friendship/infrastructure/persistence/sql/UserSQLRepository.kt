package social.friendship.infrastructure.persistence.sql

import social.friendship.application.UserRepository
import social.friendship.domain.User
import social.friendship.domain.User.UserID
import java.sql.PreparedStatement

/**
 * SQL repository for users.
 */
class UserSQLRepository : UserRepository, AbstractSQLRepository() {
    /**
     * Find a user by ID.
     * @param id the ID of the user
     * @return the user if found, null otherwise
     */
    override fun findById(id: UserID): User? {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Query.SELECT_USER_BY_ID,
            id.value
        )
        val result = ps.executeQuery()
        return if (result.next()) {
            User.of(result.getString(SQLColumns.UserTable.ID))
        } else {
            null
        }
    }

    /**
     * Save a user.
     * @param entity the user to save
     */
    override fun save(entity: User) {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Update.INSERT_USER,
            entity.id.value
        )
        ps.executeUpdate()
    }

    /**
     * Delete a user by ID.
     * @param id the ID of the user
     * @return the user if deleted, null otherwise
     */
    override fun deleteById(id: UserID): User? {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Update.DELETE_USER_BY_ID,
            id.value
        )
        val result = ps.executeUpdate()
        return if (result > 0) {
            User.of(id)
        } else {
            null
        }
    }

    /**
     * Find all users.
     * @return the list of all users
     */
    override fun findAll(): Array<User> {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Query.SELECT_ALL_USERS
        )
        val result = ps.executeQuery()
        val users = mutableListOf<User>()
        while (result.next()) {
            users.add(User.of(result.getString(SQLColumns.UserTable.ID)))
        }
        return users.toTypedArray()
    }

    /**
     * Update a user.
     * @param entity the user to update
     */
    override fun update(entity: User) {
        val ps: PreparedStatement = SQLUtils.prepareStatement(
            connection,
            SQLOperation.Update.UPDATE_USER,
            entity.id.value,
            entity.id.value
        )
        ps.executeUpdate()
    }
}
