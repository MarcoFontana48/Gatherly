import { Router } from "express";
import {ContentService} from "../../../application/service";
import {ContentServiceControllerImpl} from "./controller";

export function getRouter(service: ContentService): Router {
    const controller = new ContentServiceControllerImpl(service);

    const router = Router();

    router.get("/health", controller.getHealthCheckHandler);

    router.get("/contents/posts/:userID", controller.getPostFromUserId);

    router.post('/contents/posts', controller.addPostHandler);

    router.get('/contents/posts/feed/:userID', controller.getFeedFromUserId);

    router.delete('/contents/posts/:user/:post', controller.deletePostHandler);

    return router;
}
