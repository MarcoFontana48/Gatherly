package test.friendship.infrastructure

import social.utils.docker.DockerTest

abstract class DockerSQLTest : DockerTest() {
    internal val friendshipSqlRepositoryHost = "friendship-sql-repository"
    internal val localhostIP = "127.0.0.1"
    internal val localhost = "localhost"
    internal val database = "friendship"
    internal val user = "test_user"
    internal val password = "password"
    internal val port = "3306"
}
