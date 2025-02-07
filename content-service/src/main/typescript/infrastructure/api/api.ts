import express from "express";
import http from "http";

export class Server {
    private readonly port: number;
    private readonly middlewares: express.RequestHandler[];
    private readonly router: express.Router;
    public server?: http.Server;

    constructor(port: number, middlewares: express.RequestHandler[], router: express.Router) {
        this.port = port;
        this.middlewares = middlewares;
        this.router = router;
    }

    async start(onStarted: () => void = () => {}) {
        return new Promise<void>((resolve) => {
            const app = express();

            app.use((_req: any, res: any, next: any) => {
                res.header("Access-Control-Allow-Origin", "http://localhost:5173");
                res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");

                // if (req.method === "OPTIONS") {
                //     return res.sendStatus(204);
                // }
                next();
            });

            app.use(...this.middlewares);
            app.use("/", this.router);
            this.server = http.createServer(app);
            this.server.listen(this.port, "0.0.0.0", () => {
                console.log(`Server is running on port ${this.port}`);
                onStarted();
                resolve();
            });
        });
    }
}


export const EmptyRouter = express.Router();
export const DefaultMiddlewares: express.RequestHandler[] = [
    express.json(),
];
