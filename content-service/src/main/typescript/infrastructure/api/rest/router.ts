import { Router } from "express";
import {ContentService} from "../../../application/service";
import {ContentServiceControllerImpl} from "./controller";

/**
 * Get the router for the content service
 * @param service the content service
 */
export function getRouter(service: ContentService): Router {
    const controller = new ContentServiceControllerImpl(service);

    const router = Router();

    router.get("/health", controller.getHealthCheckHandler);

    router.get("/contents/posts/:userID", controller.getPostFromUserId);

    router.post('/contents/posts', controller.addPostHandler);

    router.get('/contents/posts/feed/:userID', controller.getFeedFromUserId);

    router.delete('/contents/posts/:user/:post', controller.deletePostHandler);

    router.get('/notifications/:id', controller.sseHandler);

    return router;
}
