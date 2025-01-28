# Testing
A test-driven like approach was followed during development, fully automating the functionality verification process.
The adopted methodology includes:

- Architectural tests
- Unit tests
- Integration tests
- Component tests
- End-to-end tests

In the initial phase, a sufficiently high coverage was achieved for individual microservices to ensure the correct
functioning of each node. 
Subsequently, extensive end-to-end tests for the entire system were produced, 
simulating a web client using dedicated libraries.
It is also specified that it's made extensive use of containerization and orchestration through Docker
to demonstrate the correct functioning of communication between system nodes or specific portions of it.
Below are some example of each test type to better illustrate the style in which they were written.

## Architecture Test
The test ensures that the dependency constraints are respected.
```kotlin
internal class DependenciesTest {
    @Test
    fun layerDependenciesAreRespected() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Infrastructure").definedBy("social.friendship.infrastructure..")
            .layer("Application").definedBy("social.friendship.application..")
            .layer("Domain").definedBy("social.friendship.domain..")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Domain").mayNotAccessAnyLayer()
            .check(ClassFileImporter().importPackages("social.friendship"))
    }
}
```

## Unit Test
These tests verify that the `contains` and `filteredBy` methods of `Post` and `Feed` works properly.
```typescript
test("post contains keywords", () => {
    const user = userOf(username, email);
    const post = postOf(user, content);
    expect(post.contains("test")).toBe(true);
});

test("feed can be filtered by keywords", () => {
    const user = userOf(username, email);
    const post1 = postOf(user, content);
    const post2 = postOf(user, "without keyword");
    const feed = feedOf(user, [post1, post2]);
    const filtered = feedOf(user, [post1]);
    expect(feed.filterBy("test")).toStrictEqual(filtered);
});
```

## Integration Test
In this integration test, a container with the database is instantiated and then queried using the repository.
```kotlin
@BeforeEach
fun setUp() {
    val dockerComposeResource = this::class.java.getResource(dockerComposePath) ?: throw Exception("Resource not found")
    dockerComposeFile = File(dockerComposeResource.toURI())
    executeDockerComposeCmd(dockerComposeFile, "up", "--wait")
    setUpDatabase()
}

@AfterEach
fun tearDown() {
    executeDockerComposeCmd(dockerComposeFile, "down", "-v")
}

@Timeout(5 * 60)
@Test
fun findAllFriendsOfUser() {
    friendshipRepository.save(Friendship.of(userTo, userFrom))
    friendshipRepository.save(Friendship.of(userTo, userFrom2))
    val friendsOfUserTo = friendshipRepository.findAllFriendsOf(userTo.id).toList()
    val friendsOfUserFrom = friendshipRepository.findAllFriendsOf(userFrom.id).toList()
    val friendsOfUserFrom2 = friendshipRepository.findAllFriendsOf(userFrom2.id).toList()
    assertAll(
        { assertTrue(friendsOfUserTo.size == 2) },
        { assertTrue(friendsOfUserTo.containsAll(listOf(userFrom, userFrom2))) },
        { assertTrue(friendsOfUserFrom.size == 1) },
        { assertTrue(friendsOfUserFrom.contains(userTo)) },
        { assertTrue(friendsOfUserFrom2.size == 1) },
        { assertTrue(friendsOfUserFrom2.contains(userTo)) },
    )
}
```

## Component Test
Similar to the previous example, this test instantiates the database. Additionally, a local REST server is set up. 
Finally, HTTP calls are made to the server to verify the overall behavior of the service.
```kotlin
@BeforeEach
fun setUp() {
    val dockerComposeResource = this::class.java.getResource(dockerComposePath) ?: throw Exception("Resource not found")
    dockerComposeFile = File(dockerComposeResource.toURI())
    executeDockerComposeCmd(dockerComposeFile, "up", "--wait")

    vertx = Vertx.vertx()
    service = FriendshipServiceVerticle(DatabaseCredentials(localhostIP, port, database, user, password))
    deployVerticle(vertx, service)
    api = RESTFriendshipAPIVerticle(service)
    deployVerticle(vertx, api)
    createTestWebClient(vertx)
}

@AfterEach
fun tearDown() {
    executeDockerComposeCmd(dockerComposeFile, "down", "-v")
    closeVertxInstance()
}

@Timeout(5 * 60)
@Test
fun addFriendshipWithoutUsersParam() {
    val latch = CountDownLatch(2)

    val friendshipJsonString = mapper.writeValueAsString(friendship1)
    val friendshipJson = JsonObject(friendshipJsonString)

    val friendshipWithoutUserToJson = friendshipJson.copy()
    friendshipWithoutUserToJson.remove("user1")
    val response1 = sendPostRequest(friendshipWithoutUserToJson, latch, Endpoint.FRIENDSHIP, webClient)

    val friendshipWithoutUserFromJson = friendshipJson.copy()
    friendshipWithoutUserFromJson.remove("user2")
    val response2 = sendPostRequest(friendshipWithoutUserFromJson, latch, Endpoint.FRIENDSHIP, webClient)

    latch.await()
    assertAll(
        { assertEquals(StatusCode.BAD_REQUEST, response1.statusCode()) },
        { assertEquals(StatusCode.BAD_REQUEST, response2.statusCode()) }
    )
}
```

[« Back to Index](../docs.md) | [« Previous](./architecture.md) | [Next »](./multiplatform.md)