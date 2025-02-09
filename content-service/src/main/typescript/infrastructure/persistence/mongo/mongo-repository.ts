import {FriendshipRepository, PostRepository, UserRepository} from "../../../application/repository";
import {
    Feed,
    feedOf,
    Friendship,
    FriendshipID, friendshipOf,
    Post,
    postFrom,
    postOf,
    User,
    UserID,
    userOf
} from "../../../domain/domain";
import {social} from "../../../commons-lib";
import mongoose from 'mongoose';
import ID = social.common.ddd.ID;
import {ConnectionOptions} from "mysql2/promise";
import fs from 'fs/promises';

const userSchema = new mongoose.Schema({
    _id: { type: String, required: true }, // email as _id
    userName: { type: String, required: true },
});

const postSchema = new mongoose.Schema({
    _id: { type: String, required: true }, // UUID
    author: { type: String, ref: 'User', required: true }, // Reference to User (_id = email)
    content: { type: String, required: true },
});

const friendSchema = new mongoose.Schema({
    _id: { type: String, required: true },  // user1 + user2 emails
    user1: { type: String, ref: 'User', required: true }, // Reference to User
    user2: { type: String, ref: 'User', required: true }, // Reference to User
});

const PostModel = mongoose.model('Post', postSchema);
const UserModel = mongoose.model('User', userSchema);
const FriendModel = mongoose.model('Friend', friendSchema);

abstract class AbstractMongoRepository {
    protected uri: string | undefined;
    protected connection: Promise<mongoose.Mongoose> | undefined;

    async connectToMongoDB(config: ConnectionOptions): Promise<void> {
        try {
            // Read the password from the file
            // const password = (await fs.readFile('db-root-password.txt', 'utf-8')).trim();

            // MongoDB URI with authentication
            this.uri = `mongodb://root:${encodeURIComponent("111")}@content-mongo-repository:${config.port}/content?authSource=admin`;

            // Connect to the database
            this.connection = mongoose.connect(this.uri);
            await this.connection;

            console.log('Connected to MongoDB successfully.');
        } catch (error) {
            console.error('Failed to connect to MongoDB:', error);
        }
    }
}

export class MongoPostRepository extends AbstractMongoRepository implements PostRepository {
    async connect(config: ConnectionOptions): Promise<void> {
        await this.connectToMongoDB(config);
    }

    async save(post: Post) {
        console.log(`Saving post: author: ${post.author.email}, content: ${post.content}`);
        if (!this.connection) {
            console.log('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        await this.connection;

        // Check if the author exists in the database
        console.log(`Checking if author exists: ${post.author.email}`);
        const authorExists = await UserModel.exists({_id: post.author.email});

        if (!authorExists) {
            console.error(`Author with ID ${post.author.email} does not exist.`);
            throw new Error(`Author with ID ${post.author.email} does not exist.`);
        }

        console.log(`Author exists: ${post.author.email}`);
        const document = new PostModel({
            _id: post.id.id,
            author: post.author.email,
            content: post.content,
        });

        console.log(`Saving post to database: author: ${post.author.email}, content: ${post.content}`);
        try {
            // Save the post
            const result = await document.save();
            console.log(`Post saved to database: author: ${result.author}, content: ${result.content}`);
        } catch (error) {
            console.error('Error saving post:', error);
            throw error;
        }
    }

    async deleteById(id: ID<string>): Promise<Post | undefined> {
        if (!this.connection) {
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(() => {
                return PostModel.findByIdAndDelete(id);
            })
            .then((result: any) => {
                console.log(`Post deleted from database: ${result}`);
                return result;
            })
            .catch((error: any) => {
                console.error(error);
            });
    }

    async findAll(): Promise<Post[]> {
        console.log('Finding all posts');
        if (!this.connection) {
            console.error('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(async () => {
                const retrievedPosts = await PostModel.find().populate<{
                    author: { userName: string; email: string }
                }>('author');
                console.log(`Posts found in database: ${retrievedPosts.length} posts`);

                return retrievedPosts.map((retrievedPost) => {
                    const {author, content, _id} = retrievedPost;
                    console.log(`Post found in database: author: ${author}, content: ${content}`);

                    if (!author) {
                        console.error(`Author not found for post with ID: ${_id}`);
                        throw new Error(`Author not found for post with ID: ${_id}`);
                    }

                    console.log(`Author found for post with ID: ${_id}`);
                    return postOf(userOf(author.userName, author.email), content, _id);
                });
            })
            .then((result: Post[]) => {
                console.log(`Posts found in database: ${result.length} posts`);
                return result;
            })
            .catch((error: any) => {
                console.error('Error fetching posts:', error);
                throw error;
            });
    }

    async findByID(id: ID<string>): Promise<Post | undefined> {
        console.log(`Finding post by ID: ${id.id}`);
        if (!this.connection) {
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(async () => {
                // Find the post by ID and populate the 'author' reference
                const retrievedPost = await PostModel.findById(id.id).populate<{
                    author: { userName: string; _id: string }
                }>('author');
                console.log(`Post found in database: ${retrievedPost}`);

                if (!retrievedPost) {
                    console.error(`Post not found in database: ${id.id}`);
                    return undefined;
                }

                const {author, content, _id} = retrievedPost;

                // Validate the populated author
                if (!author) {
                    console.error(`Author not found for post with ID: ${id.id}`);
                    throw new Error(`Author not found for post with ID: ${id.id}`);
                }

                // Map the MongoDB document to the Post domain model
                console.log(`Post found in database: author: ${author.userName}, content: ${content}`);
                return postOf(userOf(author.userName, author._id), content, _id);
            })
            .then((result: Post | undefined) => {
                if (result) {
                    console.log(`Post found in database: author: ${result.author.id.id}, content: ${result.content}`);
                }
                return result;
            })
            .catch((error: any) => {
                console.error(error);
                return undefined;
            });
    }

    async update(entity: Post): Promise<void> {
        console.log(`Updating post: author: ${entity.author.email}, content: ${entity.content}`);
        if (!this.connection) {
            console.error('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(() => {
                console.log(`Updating post in database: author: ${entity.author.email}, content: ${entity.content}`);
                return PostModel.findByIdAndUpdate(entity.id, entity);
            })
            .then((result: any) => {
                console.log(`Post updated in database: ${result}`);
            })
            .catch((error: any) => {
                console.error(error);
            });
    }

    async findAllPostsByUserID(id: UserID): Promise<Post[]> {
        console.log(`Finding all posts by user ID: ${id.id}`);
        if (!this.connection) {
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(async () => {
                const retrievedPosts = await PostModel.find({author: id.id}).populate<{
                    author: { userName: string; email: string }
                }>('author');

                console.log(`Posts found in database: ${retrievedPosts.length} posts`);
                return retrievedPosts.map((retrievedPost) => {
                    const {author, content, _id} = retrievedPost;

                    if (!author) {
                        console.error(`Author not found for post with ID: ${_id}`);
                        throw new Error(`Author not found for post with ID: ${_id}`);
                    }

                    console.log(`Author found for post with ID: ${_id}`);
                    return postOf(userOf(author.userName, author.email), content, _id);
                });
            })
            .then((result: Post[]) => {
                console.log(`Posts found in database: ${result.length} posts`);
                return result;
            })
            .catch((error: any) => {
                console.error('Error fetching posts:', error);
                throw error;
            });
    }

    async getFeed(user: User): Promise<Feed> {
        await this.connection;

        console.log(`Getting feed for user: ${user.email}`);

        const friends = await FriendModel.find({
            $or: [
                {user1: user.email},
                {user2: user.email}
            ]
        });

        console.log(`Found friends: ${friends}`);
        const friendEmails = friends.map((friend: { user1: string; user2: string }) =>
            friend.user1 === user.email ? friend.user2 : friend.user1
        );

        console.log(`Found friends emails: ${friendEmails}`);

        const posts = await PostModel.find({author: {$in: friendEmails}}).exec();

        console.log(`Found posts: ${posts}`);

        const domainPosts = posts.map((post: any) =>
            postOf(userOf(post.author.userName, post.author), post.content, post._id)
        );

        return feedOf(user, domainPosts);
    }
}

export class MongoUserRepository extends AbstractMongoRepository implements UserRepository {
    async connect(config: ConnectionOptions): Promise<void> {
        await this.connectToMongoDB(config);
    }

    async save (user: User) {
        console.log(`Saving user: email: ${user.email}, userName: ${user.userName}`);
        if (!this.connection) {
            throw new TypeError('No active MongoDB connection.');
        }

        await this.connection
            .then(() => {
                const document = new UserModel({
                    _id: user.email,
                    userName: user.userName,
                })
                console.log(`Saving user to database: email: ${user.email}, userName: ${user.userName}`);
                return document.save();
            })
            .then((result: { _id: any; userName: any; }) => {
                console.log(`User saved to database: email:${result._id}, userName:${result.userName}`);
            })
    }

    async deleteById(id: ID<string>): Promise<User | undefined> {
        if (!this.connection) {
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(async () => {
                const retrievedUser = await UserModel.findByIdAndDelete(id.id);
                console.log(`User found in database: ${retrievedUser}`);

                if (!retrievedUser) {
                    console.error(`User not found in database: ${id.id}`);
                    return undefined; // User not found
                }

                const deletedPosts = await PostModel.deleteMany({ author: retrievedUser._id });
                console.log(`${deletedPosts.deletedCount} post(s) deleted for user ${retrievedUser._id}`);

                const deletedFriendships = await FriendModel.deleteMany({
                    $or: [{ user1: retrievedUser._id }, { user2: retrievedUser._id }]
                });
                console.log(`${deletedFriendships.deletedCount} friendship(s) deleted for user ${retrievedUser._id}`);

                return userOf(retrievedUser.userName, retrievedUser._id);
            })
            .then((result: any) => {
                console.log(`User deleted from database: ${result}`);
                return result;
            })
            .catch((error: any) => {
                console.error('Error deleting user:', error);
                throw error;
            });
    }

    async findAll(): Promise<User[]> {
        if (!this.connection) {
            throw new TypeError('No active MongoDB connection.');
        }

        return this.connection
            .then(() => {
                console.log('Finding all users');
                return UserModel.find();
            })
            .then((result: any) => {
                console.log(`Users found in database: ${result}`);
                return result;
            })
            .catch((error: any) => {
                console.error(error);
            });
    }

    async findByID(id: ID<string>): Promise<User | undefined> {
        if (!this.connection) {
            throw new TypeError('No active MongoDB connection.');
        }

        return this.connection
            .then(async () => {
                const retrievedUser = await UserModel.findById(id.id);
                console.log(`User found in database: ${retrievedUser}`);

                if (!retrievedUser) {
                    console.error(`User not found in database: ${id.id}`);
                    return undefined;
                }

                return userOf(retrievedUser.userName, retrievedUser._id);
            })
            .then((result: any) => {
                console.log(`User found in database: ${result}`);
                return result;
            })
            .catch((error: any) => {
                console.error(error);
            });
    }

    async update(entity: User): Promise<void> {
        if (!this.connection) {
            console.error('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        return this.connection
            .then(() => {
                console.log(`Updating user: email: ${entity.email}, userName: ${entity.userName}`);
                return UserModel.findByIdAndUpdate(entity.id.id, entity);
        })
            .then((result: any) => {
                console.log(`User updated in database: ${result}`);
            })
            .catch((error: any) => {
                console.error(error);
            });
    }
}

export class MongoFriendshipRepository extends AbstractMongoRepository implements FriendshipRepository {
    async connect(config: ConnectionOptions): Promise<void> {
        await this.connectToMongoDB(config);
    }

    async deleteById(id: FriendshipID): Promise<Friendship | undefined> {
        if (!this.connection) {
            console.error('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(() => {
                console.log(`Deleting friendship: ${id.id}`);
                return FriendModel.findByIdAndDelete(id);
            })
            .then((result: any) => {
                console.log(`Friendship deleted from database: ${result}`);
                return result;
            })
            .catch((error: any) => {
                console.error(error);
            });
    }

    async findAll(): Promise<Friendship[]> {
        if (!this.connection) {
            console.error('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(() => {
                console.log('Finding all friendships');
                return FriendModel.find();
            })
            .then((result: any) => {
                console.log(`Friendships found in database: ${result}`);
                return result;
            })
            .catch((error: any) => {
                console.error(error);
            });
    }

    async findByID(id: FriendshipID): Promise<Friendship | undefined> {
        if (!this.connection) {
            console.error('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(async () => {
                const friendshipId = id.id.x + id.id.y;
                console.log(`Finding friendship by ID: ${friendshipId}`);

                const retrievedFriendship = await FriendModel.findById(friendshipId).populate<{ user1: { userName: string; _id: string }, user2: { userName: string; _id: string } }>('user1 user2');

                console.log(`Friendship found in database: ${retrievedFriendship}`);

                if (!retrievedFriendship) {
                    console.error(`Friendship not found in database: ${friendshipId}`);
                    return undefined;
                }

                console.log(`Friendship found in database: ${retrievedFriendship}`);

                const user1 = userOf(retrievedFriendship.user1.userName, retrievedFriendship.user1._id);
                const user2 = userOf(retrievedFriendship.user2.userName, retrievedFriendship.user2._id);

                console.log(`Friendship found in database: ${user1}, ${user2}`);

                return friendshipOf(user1, user2);
            })
            .then((result: Friendship | undefined) => {
                console.log(`Friendship found in database: ${result ? result.id.id : 'Not found'}`);
                return result;
            })
            .catch((error: any) => {
                console.error('Error fetching friendship:', error);
                throw error;
            });
    }

    async save(entity: Friendship): Promise<void> {
        if (!this.connection) {
            console.error('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(async () => {
                const document = new FriendModel({
                    _id: entity.id.id.x + entity.id.id.y,
                    user1: entity.user1.email,
                    user2: entity.user2.email,
                })

                console.log(`Saving friendship to database: user1:${entity.user1.email}, user2:${entity.user2.email}`);

                // Check if the user (author) exists
                const user1Exists = await UserModel.exists({_id: entity.user1.email});
                const user2Exists = await UserModel.exists({_id: entity.user2.email});
                if (!user1Exists || !user2Exists) {
                    console.error(`Cannot store friendship since at least one of the users does not exist.`);
                    throw new Error(`Cannot store friendship since at least one of the users does not exist.`);
                }

                console.log(`Users exist: ${entity.user1.email}, ${entity.user2.email}`);
                return document.save();
            })
            .then((result: { user1: any; user2: any; }) => {
                console.log(`Friendship saved to database: user1:${result.user1}, user2:${result.user2}`);
            })
    }

    async update(entity: Friendship): Promise<void> {
        if (!this.connection) {
            console.error('No active MongoDB connection.');
            throw new TypeError('No active MongoDB connection.');
        }

        return await this.connection
            .then(() => {
                console.log(`Updating friendship: user1:${entity.user1.email}, user2:${entity.user2.email}`);
                return FriendModel.findByIdAndUpdate(entity.id, entity);
        })
            .then((result: any) => {
                console.log(`Friendship updated in database: ${result}`);
            })
            .catch((error: any) => {
                console.error(error);
            });
    }
}

