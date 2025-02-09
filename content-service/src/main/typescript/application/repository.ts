import {Feed, Friendship, FriendshipID, Pair, Post, User, UserID} from "../domain/domain";
import {social} from "../commons-lib";
import Entity = social.common.ddd.Entity;
import ID = social.common.ddd.ID;
import fs from "node:fs";
import {ConnectionOptions} from "mysql2/promise";

/**
 * Repository interface for CRUD operations
 */
export interface Repository<T, I extends ID<T>, E extends Entity<I>> {
    save(entity: E): Promise<void>
    findByID(id: I): Promise<E | undefined>
    deleteById(id: I): Promise<E | undefined>
    findAll(): Promise<E[]>
    update(entity: E): Promise<void>
}

/**
 * Connectable interface for connecting to a database
 */
export interface Connectable {
    connect(config: ConnectionOptions): Promise<void>;
}

/**
 * Post repository interface for post related operations
 */
export interface PostRepository extends Repository<string, ID<string>, Post>, Connectable {
    getFeed(user: User): Promise<Feed>;
    findAllPostsByUserID(id: UserID): Promise<Post[]>;
}

/**
 * User repository interface for user related operations
 */
export interface UserRepository extends Repository<string, ID<string>, User>, Connectable {}

/**
 * Friendship repository interface for friendship related operations
 */
export interface FriendshipRepository extends Repository<Pair<string, string>, FriendshipID, Friendship>, Connectable {}

/**
 * Get the configuration for the database connection
 * @param port the port to connect to
 */
export function getConfiguration(port: number) : ConnectionOptions {
    let password: string
    try {
        password = fs.readFileSync("../../run/secrets/db_password", "utf8")
    } catch (e: any) {
        password = fs.readFileSync("./db-password.txt", 'utf8')
    }

    return {
        host: process.env.DB_HOST || "127.0.0.1",
        port: port,
        database: "content",
        user: "user",
        password: password
    }
}