import {social} from "../commons-lib";
import Service = social.common.ddd.Service;
import {
    FriendshipRepository,
    getConfiguration,
    PostRepository,
    UserRepository
} from "./repository";
import {Feed, Friendship, FriendshipID, Post, PostID, User, UserID} from "../domain/domain";
import {UnableToDelete} from "./service-errors";
import {EventEmitter} from "events";

/**
 * Content service interface
 */
export interface ContentService extends Service {
    addPost(post: Post): Promise<void>;
    addUser(user: User): Promise<void>;
    addFriendship(friendship: Friendship): Promise<void>;
    getPost(id: PostID): Promise<Post | undefined>;
    getFeed(userID: UserID, keyword?: string): Promise<Feed>;
    deletePost(postID: PostID, userID: UserID): Promise<Post | undefined>;
    deleteUser(id: UserID): Promise<User | undefined>;
    deleteFriendship(id: FriendshipID): Promise<Friendship | undefined>;
    getPostByAuthor(id: UserID): Promise<Post[]>;
    init(port: number): Promise<void>;
    getPostAddedEmitter(): any;
}

/**
 * Content service implementation
 */
export class ContentServiceImpl implements ContentService {
    private eventEmitter = new EventEmitter();
    private friendshipRepository: FriendshipRepository;
    private postRepository: PostRepository;
    private userRepository: UserRepository;

    constructor(
        friendshipRepository: FriendshipRepository,
        postRepository: PostRepository,
        userRepository: UserRepository
    ) {
        this.friendshipRepository = friendshipRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Initialize the service
     * @param port the port to connect to
     */
    async init(port: number) {
        const config = getConfiguration(port);
        await this.friendshipRepository.connect(config);
        await this.postRepository.connect(config);
        await this.userRepository.connect(config);
    }

    /**
     * Add a friendship
     * @param friendship
     * @returns the added friendship
     */
    addFriendship(friendship: Friendship) {
        return this.friendshipRepository.save(friendship);
    }

    /**
     * Add a post
     * @param post
     */
    async addPost(post: Post) {
        console.log("adding post: '{}'", post);
        await this.postRepository.save(post);
        console.log("post added, about to emit postAdded event: post:'{}'", post);
        this.eventEmitter.emit("postAdded", post);
    }

    /**
     * Add a user
     * @param user
     */
    addUser(user: User) {
        return this.userRepository.save(user);
    }

    /**
     * Add a friendship
     * @param id
     * @returns the deleted friendship
     */
    deleteFriendship(id: FriendshipID): Promise<Friendship | undefined> {
        return this.friendshipRepository.deleteById(id);
    }

    /**
     * Delete a post
     * @param postID
     * @param userID
     * @returns the deleted post
     */
    async deletePost(postID: PostID, userID: UserID): Promise<Post | undefined> {
        const post = await this.postRepository.findByID(postID);
        if(post) {
            if(userID.equals(post.author.id)){
                return await this.postRepository.deleteById(postID);
            } else {
                throw new UnableToDelete("only authors can delete posts");
            }
        }
        return undefined;
    }

    /**
     * Delete a user
     * @param id
     * @returns the deleted user
     */
    deleteUser(id: UserID): Promise<User | undefined> {
        return this.userRepository.deleteById(id);
    }

    /**
     * Get the feed for a user
     * @param userID
     * @param keyword
     * @returns the feed
     */
    async getFeed(userID: UserID, keyword?: string): Promise<Feed> {
        const user = await this.userRepository.findByID(userID);
        const feed = await this.postRepository.getFeed(user!);
        console.log("retrieved feed: ", feed);

        if(keyword) {
                return feed.filterBy(keyword);
        }

        console.log("returning feed: ", feed);
        return feed;
    }

    /**
     * Get a post by id
     * @param id the id of the post
     * @returns the post
     */
    getPost(id: PostID): Promise<Post | undefined> {
        return this.postRepository.findByID(id);
    }

    /**
     * Get all posts by an author
     * @param id the id of the author
     * @returns the posts
     */
    getPostByAuthor(id: UserID): Promise<Post[]> {
        return this.postRepository.findAllPostsByUserID(id);
    }

    /**
     * Get the post added event emitter
     * @returns the event emitter
     */
    getPostAddedEmitter() {
        console.log("returning postAdded emitter");
        return this.eventEmitter;
    }
}