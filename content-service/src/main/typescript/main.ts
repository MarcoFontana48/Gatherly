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
const storedEmails: string[] = [];  // only for testing purposes, stores the emails of the users created randomly

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
        let counter = 2 * 60;

        const logInterval = setInterval(() => {
            if (counter === 0) {
                clearInterval(logInterval);
            } else {
                console.log(`Sending requests in ${counter}s...`);
                counter -= 5;
            }
        }, 5000); // Log every 5 seconds

        await new Promise((resolve) => setTimeout(resolve, 2 * 60_000));

        // generate and store a random user
        const email = `user${Math.floor(Math.random() * 10000)}@example.com`;
        storedEmails.push(email);
        console.log(`Stored emails: ${storedEmails}`);

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

        // send a friend request to 'test@gmail.com'
        console.log("Sending friend request...");
        try {
            const friendResponse = await axios.post("http://friendship-service:8080/friends/requests/send", {
                from: email,
                to: "test@gmail.com",
            });
            console.log("Friend request successful:", friendResponse.data);
        } catch (error: any) {
            console.error("Friend request failed:", error.message);
        }

        // create a social-network post with a random string as content
        const randomContent = generateRandomString(10);
        const randomEmail = getRandomEmail();
        console.log("Sending post with random content...");
        try {
            const postResponse = await axios.post("http://content-service:8080/contents/posts", {
                user: {
                    email: randomEmail,
                    name: "placeholder",
                },
                content: randomContent,
            });
            console.log("Post successful:", postResponse.data);
        } catch (error: any) {
            console.error("Post request failed:", error.message);
        }
    }, 2 * 60_000); // Repeat every 2 minutes
}

const generateRandomString = (length: number) => {
    const characters = 'abcdefghijklmnopqrstuvwxyz';
    let result = '';
    const charactersLength = characters.length;
    for (let i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
};

const getRandomEmail = () => {
    if (storedEmails.length === 0) {
        console.log("No stored emails to choose from.");
        return null;
    }
    const randomIndex = Math.floor(Math.random() * storedEmails.length);

    console.log(`Random email chosen: ${storedEmails[randomIndex]}`);
    return storedEmails[randomIndex];
};
