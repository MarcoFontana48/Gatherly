import {Server, DefaultMiddlewares} from "./infrastructure/api"
import {getRouter} from "./infrastructure/router"
import {ContentServiceImpl} from "./application/service";
import {SqlFriendshipRepository, SqlPostRepository, SqlUserRepository} from "./infrastructure/persistence/sql/sql-repository";
import {FriendshipRepository, PostRepository, UserRepository} from "./application/repository";
import {KafkaConsumer} from "./infrastructure/kafka";
import {Kafka} from "kafkajs";

const userRepository: UserRepository = new SqlUserRepository();
const postRepository: PostRepository = new SqlPostRepository();
const friendshipRepository: FriendshipRepository = new SqlFriendshipRepository();
const service = new ContentServiceImpl(friendshipRepository, postRepository,userRepository);
service.init(3306).then(() => {
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
