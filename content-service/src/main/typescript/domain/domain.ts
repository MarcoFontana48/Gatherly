import {social} from "../commons-lib";
import {v4 as uuidv4} from "uuid"
import AggregateRoot = social.common.ddd.AggregateRoot;
import ID = social.common.ddd.ID;
import Entity = social.common.ddd.Entity;

/**
 * User ID class
 * @extends ID
 */
export class UserID extends ID<string> {}

/**
 * User interface
 * @extends Entity
 */
export interface User extends Entity<UserID> {
    readonly userName: string;
    readonly email: string;
}

/**
 * User implementation class
 * @extends Entity
 * @implements User
 */
class UserImpl extends Entity<UserID> implements User {
    readonly userName: string;
    readonly email: string;

    /**
     * Constructor
     * @param userName name of the user
     * @param email email of the user
     */
    constructor(userName: string, email: string) {
        super(new UserID(email));
        this.userName = userName;
        this.email = email;
    }
}

/**
 * Post ID class
 * @extends ID
 */
export class PostID extends ID<string> {}

/**
 * Post interface
 * @extends AggregateRoot
 */
export interface Post extends AggregateRoot<PostID> {
    readonly author: User;
    readonly content: string;
    contains(keyword: string): boolean;
}

/**
 * Post implementation class
 * @extends AggregateRoot
 * @implements Post
 */
class PostImpl extends AggregateRoot<PostID> implements Post {
    readonly author: User;
    readonly content: string;

    /**
     * Constructor
     * @param author author of the post
     * @param content content of the post
     * @param id id of the post
     */
    constructor(author: User, content: string, id: string) {
        super(new PostID(id));
        this.author = author;
        this.content = content;
    }

    /**
     * Check if the post contains a keyword
     * @param keyword keyword to check
     * @returns true if the post contains the keyword, false otherwise
     */
    contains(keyword: string): boolean {
        return this.content.includes(keyword);
    }
}

/**
 * Feed ID class
 * @extends ID
 */
export class FeedID extends ID<string> {}

/**
 * Feed interface
 */
export interface Feed {
    readonly posts: Post[];
    readonly owner: User;
    filterBy(keyword: string): Feed;
    copy(owner: User, posts: Post[]): Feed;
}

/**
 * Feed implementation class
 * @extends AggregateRoot
 * @implements Feed
 */
class FeedImpl extends AggregateRoot<FeedID> implements Feed {
    readonly posts: Post[];
    readonly owner: User;

    /**
     * Constructor
     * @param owner owner of the feed
     * @param posts posts in the feed
     */
    constructor(owner: User, posts: Post[]) {
        super(new FeedID(owner.email));
        this.owner = owner;
        this.posts = posts;
    }

    /**
     * Filter the feed by a keyword
     * @param keyword keyword to filter by
     * @returns the filtered feed
     */
    filterBy(keyword: string): Feed {
        return this.copy(undefined, this.posts.filter(post => post.contains(keyword)));
    }

    /**
     * Copy the feed
     * @param owner owner of the feed
     * @param posts posts in the feed
     * @returns the copied feed
     */
    copy(owner: User = this.owner, posts: Post[] = this.posts): Feed {
        return new FeedImpl(owner, posts);
    }
}

/**
 * Friendship ID class
 */
export interface Pair<X, Y> {
    x: X;
    y: Y;
}

/**
 * Friendship ID class
 * @extends ID
 */
export class FriendshipID extends ID<Pair<string, string>> {
    readonly user1: string;
    readonly user2: string;

    /**
     * Constructor
     * @param user1 first user
     * @param user2 second user
     */
    constructor(user1: string, user2: string) {
        const pair: Pair<string, string> = {x: user1, y: user2}
        super(pair);
        this.user1 = user1;
        this.user2 = user2;
    }
}

/**
 * Friendship interface
 * @extends Entity
 */
export interface Friendship extends Entity<FriendshipID> {
    readonly user1: User;
    readonly user2: User;
    readonly id: FriendshipID;
}

/**
 * Friendship implementation class
 * @extends Entity
 * @implements Friendship
 */
class FriendshipImpl extends Entity<FriendshipID> implements Friendship {
    readonly user1: User;
    readonly user2: User;

    /**
     * Constructor
     * @param user1 first user
     * @param user2 second user
     */
    constructor(user1: User, user2: User) {
        super(new FriendshipID(user1.email, user2.email));
        this.user1 = user1;
        this.user2 = user2;
    }
}

/* Factory methods */

/**
 * Create a post from an author
 * @param authorName name of the author
 * @param authorEmail email of the author
 * @param content content of the post
 * @param uuid optional uuid
 * @returns the created post
 */
export function postFrom(authorName: string, authorEmail: string, content: string, uuid?: string): Post {
    return postOf(userOf(authorName, authorEmail), content, uuid);
}

/**
 * Create a post from a user
 * @param user author of the post
 * @param content content of the post
 * @param uuid optional uuid
 * @returns the created post
 */
export function postOf(user: User, content: string, uuid?: string): Post {
    return new PostImpl(user, content, uuid ? uuid : uuidv4());
}

/**
 * Create a feed from an owner
 * @param ownerName name of the owner
 * @param ownerEmail email of the owner
 * @param posts posts in the feed
 * @returns the created feed
 */
export function feedFrom(ownerName: string, ownerEmail: string, posts: Post[]): Feed {
    return feedOf(userOf(ownerName, ownerEmail), posts);
}

/**
 * Create a feed from a user
 * @param owner owner of the feed
 * @param posts posts in the feed
 * @returns the created feed
 */
export function feedOf(owner: User, posts: Post[]): Feed {
    return new FeedImpl(owner, posts);
}

/**
 * Create a user
 * @param name name of the user
 * @param email email of the user
 * @returns the created user
 */
export function userOf(name: string, email: string): User {
    return new UserImpl(name, email);
}

/**
 * Create a friendship from two users
 * @param user1 first user
 * @param user2 second user
 * @returns the created friendship
 */
export function friendshipOf(user1: User, user2: User): Friendship {
    return new FriendshipImpl(user1, user2);
}
