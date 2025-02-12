import { Server, DefaultMiddlewares } from "./infrastructure/api/api";
import { getRouter } from "./infrastructure/api/rest/router";
import { ContentServiceImpl } from "./application/service";
import { FriendshipRepository, PostRepository, UserRepository } from "./application/repository";
import { KafkaConsumer } from "./infrastructure/api/kafka/kafka";
import { Kafka } from "kafkajs";
import {
    MongoFriendshipRepository,
    MongoPostRepository,
    MongoUserRepository
} from "./infrastructure/persistence/mongo/mongo-repository";
import axios from "axios";

const userRepository: UserRepository = new MongoUserRepository();
const postRepository: PostRepository = new MongoPostRepository();
const friendshipRepository: FriendshipRepository = new MongoFriendshipRepository();

const service = new ContentServiceImpl(friendshipRepository, postRepository, userRepository);

service.init(27017).then(() => {
    const kafka = new Kafka({
        clientId: "content-service",
        brokers: [`${process.env.KAFKA_HOST || 'localhost'}:${process.env.KAFKA_PORT || '9092'}`],
    });

    const consumer = new KafkaConsumer(kafka, { groupId: "content-group", retry: { retries: 50 } }, service);
    consumer.consume().then(() => {
        const server = new Server(8080, DefaultMiddlewares, getRouter(service));
        server.start().then(() => console.log("server up!"));
    });

    //! ONLY FOR TESTING PURPOSES: the following function creates a user and sends a friend request every 30 seconds
    //! from the newly created user to a fixed email address 'test@gmail.com'. This is to simulate a user sending friend
    //! requests.
    //! To see the events being received by the user in real time and accept / reject the friendship request, run the
    //! frontend of the app and login as 'test@gmail.com'.
    startSendingRequests();
});

/**
 * Sends an HTTP request every 30 seconds, logging every 5 seconds before sending.
 */
function startSendingRequests() {
    setInterval(async () => {
        let counter = 25; // Start countdown from 25 seconds

        const logInterval = setInterval(() => {
            if (counter === 0) {
                clearInterval(logInterval);
            } else {
                console.log(`Sending requests in ${counter}s...`);
                counter -= 5;
            }
        }, 5000); // Log every 5 seconds

        await new Promise((resolve) => setTimeout(resolve, 25000)); // Wait 25 seconds before sending the request

        // Generate a random email
        const email = `user${Math.floor(Math.random() * 10000)}@example.com`;

        console.log(`Sending request to create user with email: ${email}...`);
        try {
            const userResponse = await axios.post("http://user-service:8080/users", {
                email: email,
                username: "placeholder",
            });
            console.log("User creation request successful:", userResponse.data);
        } catch (error: any) {
            console.error("User creation request failed:", error.message);
        }

        console.log("Sending friend request...");
        try {
            const friendResponse = await axios.post("http://friendship-service:8081/friends/requests/send", {
                from: email,
                to: "test@gmail.com",
            });
            console.log("Friend request successful:", friendResponse.data);
        } catch (error: any) {
            console.error("Friend request failed:", error.message);
        }
    }, 30000); // Repeat every 30 seconds
}
