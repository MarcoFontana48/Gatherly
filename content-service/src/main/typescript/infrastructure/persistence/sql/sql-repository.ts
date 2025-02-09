import {ResultSetHeader, RowDataPacket} from 'mysql2/promise';
import {
    Feed,
    feedOf,
    Friendship,
    FriendshipID,
    friendshipOf,
    Post,
    postFrom,
    User, UserID,
    userOf
} from "../../../domain/domain";
import {
    INSERT_POST,
    INSERT_USER,
    FIND_POST_BY_ID,
    FIND_ALL_POST,
    GET_FEED,
    INSERT_FRIENDSHIP,
    FIND_USER_BY_ID,
    FIND_FRIENDSHIP_BY_ID,
    DELETE_USER_BY_ID,
    FIND_ALL_USERS,
    UPDATE_USER,
    DELETE_FRIENDSHIP_BY_ID,
    FIND_ALL_FRIENDSHIP, DELETE_POST_BY_ID, UPDATE_POST, FIND_ALL_POST_BY_AUTHOR
} from "./sql-operations";
import {FriendshipRepository, PostRepository, UserRepository} from "../../../application/repository";
import {social} from "../../../commons-lib";
import {SqlErrors} from "./sql-errors";
import ID = social.common.ddd.ID;

/**
 * SQL implementation of the post repository
 */
export class SqlPostRepository extends SqlErrors implements PostRepository {

    /**
     * Map the DTO to the domain object
     * @param array the array of DTOs
     * @return the array of Post
     */
    private mapToPost(array: PostDTO[]) {
        return array.map(dto => postFrom(dto.userName, dto.author, dto.content, dto.id));
    }

    /**
     * Save a post
     * @param post the post to save
     */
    async save(post: Post) {
        try {
            await this.connection!.execute<ResultSetHeader>(INSERT_POST, [post.author.email, post.content, post.id.id]);
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Find a post by its ID
     * @param id the ID of the post
     * @return the post if found, undefined otherwise
     */
    async findByID(id: ID<string>): Promise<Post | undefined>{
        try {
            const result = await this.connection!.execute<PostDTO[]>(FIND_POST_BY_ID, [id.id]);
            const posts = this.mapToPost(result[0])
            return posts.length === 1 ? posts[0] : undefined
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Delete a post by its ID
     * @param id the ID of the post
     * @return the post if found, undefined otherwise
     */
    async deleteById(id: ID<string>): Promise<Post | undefined> {
        try {
            await this.connection!.beginTransaction();
            const post = await this.findByID(id);
            if(post){
                await this.connection!.execute(DELETE_POST_BY_ID, [id.id]);
            }
            await this.connection!.commit();
            return post;
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Find all posts
     * @return the array of posts
     */
    async findAll(): Promise<Post[]> {
        try {
            const result = await this.connection!.execute<PostDTO[]>(FIND_ALL_POST);
            return this.mapToPost(result[0]);
        } catch (error) {
            this.throwErrorFor(error);
        }
        return [];
    }

    /**
     * Update a post
     * @param post the post to update
     * @return the updated post
     */
    async update(post: Post): Promise<void> {
        try {
            await this.connection!.execute(UPDATE_POST, [post.author.email, post.content, post.id.id]);
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Get the feed for a user
     * @param user the user to get the feed for
     * @return the feed
     */
    async getFeed(user: User): Promise<Feed> {
        try {
            const result = await this.connection!.execute<PostDTO[]>(GET_FEED, [user.email, user.email]);
            return feedOf(user, this.mapToPost(result[0]));
        } catch (error) {
            this.throwErrorFor(error);
        }
        return feedOf(user, []);
    }

    /**
     * Find all posts by a user ID
     * @param id the ID of the user
     * @return the array of posts
     */
    async findAllPostsByUserID(id: UserID): Promise<Post[]> {
        try {
            const result = await this.connection!.execute<PostDTO[]>(FIND_ALL_POST_BY_AUTHOR, [id.id]);
            return this.mapToPost(result[0]);
        } catch (error) {
            this.throwErrorFor(error);
        }
        return [];
    }
}

/**
 * SQL implementation of the user repository
 */
export class SqlUserRepository extends SqlErrors implements UserRepository {

    /**
     * Map the DTO to the domain object
     * @param array the array of DTOs
     * @private the array of User
     */
    private mapToUser(array: UserDTO[]): User[] {
        return array.map(dto => userOf(dto.userName, dto.email))
    }

    /**
     * Save a user
     * @param user the user to save
     */
    async save(user: User) {
        try {
            await this.connection!.execute<ResultSetHeader>(INSERT_USER, [user.userName, user.email]);
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Find a user by its ID
     * @param id the ID of the user
     * @return the user if found, undefined otherwise
     */
    async findByID(id: ID<string>): Promise<User | undefined> {
        try {
            const result = await this.connection!.execute<UserDTO[]>(FIND_USER_BY_ID, [id.id]);
            const users = this.mapToUser(result[0]);
            return users.length === 1 ? users[0] : undefined;
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Delete a user by its ID
     * @param id the ID of the user
     * @return the user if found, undefined otherwise
     */
    async deleteById(id: ID<string>): Promise<User | undefined> {
        try {
            await this.connection!.beginTransaction();
            const user = await this.findByID(id);
            if(user){
                await this.connection!.execute(DELETE_USER_BY_ID, [id.id]);
            }
            await this.connection!.commit();
            return user;
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Find all users
     * @return the array of users
     */
    async findAll(): Promise<User[]> {
        try {
            const result = await this.connection!.execute<UserDTO[]>(FIND_ALL_USERS);
            return this.mapToUser(result[0]);
        } catch (error) {
            this.throwErrorFor(error);
        }
        return [];
    }

    /**
     * Update a user
     * @param user the user to update
     */
    async update(user: User): Promise<void> {
        try {
            await this.connection!.execute(UPDATE_USER, [user.userName, user.email]);
        } catch (error) {
            this.throwErrorFor(error);
        }
    }
}

/**
 * SQL implementation of the friendship repository
 */
export class SqlFriendshipRepository extends SqlErrors implements FriendshipRepository {
    /**
     * Map the DTO to the domain object
     * @param array the array of DTOs
     * @private the array of Friendship
     */
    private mapToFriendship(array: FriendshipDTO[]): Friendship[] {
        return array
            .map(dto => friendshipOf(userOf(dto.userName1, dto.user1), userOf(dto.userName2, dto.user2)))
    }

    /**
     * Delete a friendship by its ID
     * @param id the ID of the friendship
     * @return the friendship if found, undefined otherwise
     */
    async deleteById(id: FriendshipID): Promise<Friendship | undefined> {
        try {
            await this.connection!.beginTransaction();
            const friendship = await this.findByID(id);
            if(friendship) {
                await this.connection!.execute(DELETE_FRIENDSHIP_BY_ID, [id.user1, id.user2]);
            }
            await this.connection!.commit();
            return friendship;
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Find all friendships
     * @return the array of friendships
     */
    async findAll(): Promise<Friendship[]> {
        try {
            const result = await this.connection!.execute<FriendshipDTO[]>(FIND_ALL_FRIENDSHIP);
            return this.mapToFriendship(result[0]);
        } catch (error) {
            this.throwErrorFor(error);
        }
        return [];
    }

    /**
     * Find a friendship by its ID
     * @param id the ID of the friendship
     * @return the friendship if found, undefined otherwise
     */
    async findByID(id: FriendshipID): Promise<Friendship | undefined> {
        try {
            const result =
                await this.connection!.execute<FriendshipDTO[]>(FIND_FRIENDSHIP_BY_ID, [id.user1, id.user2]);
            const friendships = this.mapToFriendship(result[0]);
            return friendships.length === 1 ? friendships[0] : undefined;
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Save a friendship
     * @param friendship the friendship to save
     */
    async save(friendship: Friendship): Promise<void> {
        try {
            await this.connection?.execute<ResultSetHeader>(INSERT_FRIENDSHIP, [friendship.user1.email, friendship.user2.email]);
        } catch (error: any) {
            this.throwErrorFor(error);
        }
    }

    /**
     * Update a friendship
     * @param friendship the friendship to update
     */
    async update(friendship: Friendship): Promise<void> {
        try {
            await this.save(friendship);
        } catch (error) {
            this.throwErrorFor(error);
        }
    }

}

interface UserDTO extends RowDataPacket {
    readonly email: string;
    readonly userName: string;
}

interface PostDTO extends RowDataPacket {
    readonly id: string;
    readonly userName: string;
    readonly author: string;
    readonly content: string;
}

interface FriendshipDTO extends RowDataPacket {
    readonly user1: string;
    readonly userName1: string;
    readonly user2: string;
    readonly userName2: string;
}