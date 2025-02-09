import {Request, Response} from "express";
import {social} from "../../../commons-lib";
import StatusCode = social.common.endpoint.StatusCode;
import {Feed, Post, postFrom, PostID, User, UserID} from "../../../domain/domain";
import {ContentService} from "../../../application/service";
import {NoReferencedRowError} from "../../persistence/sql/sql-errors";
import {UnableToDelete} from "../../../application/service-errors";

/**
 * Controller implementation for the content service
 */
export class ContentServiceControllerImpl {
    private service: ContentService;

    /**
     * Constructor
     * @param service content service
     */
    constructor(service: ContentService) {
        this.service = service;
    }

    /**
     * Handler for the SSE endpoint
     */
    sseHandler = (req: Request, res: Response) => {
        // Set the necessary headers for SSE
        res.setHeader("Content-Type", "text/event-stream");
        res.setHeader("Cache-Control", "no-cache");
        res.setHeader("Connection", "keep-alive");
        res.flushHeaders();

        const postAddedListener = (post: Post) => {
            console.log("received postAdded event, about to send it to client: '{}'", post);
            res.write(`data: ${JSON.stringify(post)}\n\n`);
        };

        this.service.getPostAddedEmitter().on("postAdded", postAddedListener);

        req.on("close", () => {
            this.service.getPostAddedEmitter().removeListener("postAdded", postAddedListener);
        });
    };

    /**
     * Handler for the health check endpoint
     */
    getHealthCheckHandler = (_req: Request, res: Response) => {
        console.log("received health check request")
        res.status(StatusCode.OK).json("OK");
    }

    /**
     * Handler for the get post from user ID endpoint
     */
    getPostFromUserId = async (req: Request, res: Response) => {
        try {
            const posts = await this.service.getPostByAuthor(new UserID(req.params.userID));
            res.status(StatusCode.OK).json(posts.map(p => this.postToJson(p)));
        } catch (error) {
            res.status(StatusCode.INTERNAL_SERVER_ERROR).end();
        }
    }

    /**
     * Handler for the add post endpoint
     */
    addPostHandler = async (req: Request, res: Response) => {
        const body = req.body;
        if (this.isPost(body)) {
            try {
                await this.service.addPost(postFrom(body.user.name, body.user.email, body.content));
                res.status(StatusCode.CREATED).end();
            } catch (error) {
                if (error instanceof NoReferencedRowError) {
                    res.status(StatusCode.FORBIDDEN).json(error.message);
                } else {
                    res.status(StatusCode.INTERNAL_SERVER_ERROR).end();
                }
            }
        } else {
            res.status(StatusCode.BAD_REQUEST).end();
        }
    }

    /**
     * Handler for the get feed from user ID endpoint
     */
    getFeedFromUserId = async (req: Request, res: Response) => {
        try {
            if(req.query.keyword) {
                if(typeof req.query.keyword === "string") {
                    const feed = await this.service.getFeed(new UserID(req.params.userID), req.query.keyword);
                    res.status(StatusCode.OK).json(this.feedToJson(feed));
                } else {
                    res.status(StatusCode.BAD_REQUEST).json('keyword parameter is not a string');
                }
            } else {
                const feed = await this.service.getFeed(new UserID(req.params.userID));
                res.status(StatusCode.OK).json(this.feedToJson(feed));
            }
        } catch (error) {
            res.status(StatusCode.INTERNAL_SERVER_ERROR).end();
        }
    }

    /**
     * Handler for the delete post endpoint
     */
    deletePostHandler = async (req: Request, res: Response)=> {
        try {
            const post = await this.service.deletePost(new PostID(req.params.post), new UserID(req.params.user));
            res.status(StatusCode.OK).json(post ? this.postToJson(post) : post);
        } catch (error) {
            if(error instanceof UnableToDelete){
                res.status(StatusCode.FORBIDDEN).json(error.message);
            } else {
                res.status(StatusCode.INTERNAL_SERVER_ERROR).end();
            }
        }
    }

    /**
     * Check if an object is a user
     * @param obj
     */
    isUser(obj: any): boolean {
        return "name" in obj && "email" in obj;
    }

    /**
     * Check if an object is a post
     * @param obj
     */
    isPost(obj: any): boolean {
        return "user" in obj && this.isUser(obj.user) && "content" in obj;
    }

    userToJson(user: User) {
        return {
            name: user.userName,
            email: user.email,
        }
    }

    postToJson(post: Post) {
        return {
            id: post.id.id,
            author: this.userToJson(post.author),
            content: post.content,
        }
    }

    feedToJson(feed: Feed) {
        return {
            owner: this.userToJson(feed.owner),
            posts: feed.posts.map(p => this.postToJson(p)),
        }
    }
}