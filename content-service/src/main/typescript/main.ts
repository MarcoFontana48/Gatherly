import {Server, DefaultMiddlewares} from "./infrastructure/api/api"
import {getRouter} from "./infrastructure/api/rest/router"
import {ContentServiceImpl} from "./application/service";
import {FriendshipRepository, PostRepository, UserRepository} from "./application/repository";
import {KafkaConsumer} from "./infrastructure/api/kafka/kafka";
import {Kafka} from "kafkajs";
import {
    MongoFriendshipRepository,
    MongoPostRepository,
    MongoUserRepository
} from "./infrastructure/persistence/mongo/mongo-repository";

const userRepository: UserRepository = new MongoUserRepository();
const postRepository: PostRepository = new MongoPostRepository();
const friendshipRepository: FriendshipRepository = new MongoFriendshipRepository();

const service = new ContentServiceImpl(friendshipRepository, postRepository,userRepository);

service.init(27017).then(() => {
    const kafka = new Kafka({
        clientId: "content-service",
        brokers: [`${process.env.KAFKA_HOST || 'localhost'}:${process.env.KAFKA_PORT || '9092'}`],
    });
    const consumer = new KafkaConsumer(kafka, {groupId: "content-group", retry: {retries: 50}}, service);
    consumer.consume().then(() => {
        const server = new Server(8080, DefaultMiddlewares, getRouter(service));
        server.start().then(() => console.log("server up!"));
    })
});
