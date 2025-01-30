// Script to initialize MongoDB
const conn = new Mongo();
const db = conn.getDB("content-mongo-repository");

// Create "user" collection
db.createCollection("user");
db.user.createIndex({ email: 1 }, { unique: true });
db.user.createIndex({ userName: 1 });

// Create "post" collection
db.createCollection("post");
db.post.createIndex({ id: 1 }, { unique: true });
db.post.createIndex({ author: 1 });

// Create "friendship" collection
db.createCollection("friendship");
db.friendship.createIndex({ user1: 1, user2: 1 }, { unique: true });
db.friendship.createIndex({ user1: 1 });
db.friendship.createIndex({ user2: 1 });
