type Nullable<T> = T | null | undefined
export declare namespace social.common.ddd {
    interface Service {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.ddd.Service": unique symbol;
        };
    }
    class ID<I> implements social.common.ddd.ValueObject {
        constructor(id: I);
        get id(): I;
        equals(other: Nullable<any>): boolean;
        hashCode(): number;
        toString(): string;
        readonly __doNotUseOrImplementIt: social.common.ddd.ValueObject["__doNotUseOrImplementIt"];
    }
    class Entity<I extends social.common.ddd.ID<any /*UnknownType **/>> {
        constructor(id: I);
        get id(): I;
        equals(other: Nullable<any>): boolean;
        hashCode(): number;
        toString(): string;
    }
    interface ValueObject {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.ddd.ValueObject": unique symbol;
        };
    }
    class AggregateRoot<I extends social.common.ddd.ID<any /*UnknownType **/>> extends social.common.ddd.Entity<I> {
        constructor(id: I);
    }
    interface Repository<I extends social.common.ddd.ID<any /*UnknownType **/>, E extends social.common.ddd.Entity<any /*UnknownType **/>> {
        findById(id: I): Nullable<E>;
        save(entity: E): void;
        deleteById(id: I): Nullable<E>;
        findAll(): Array<E>;
        update(entity: E): void;
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.ddd.Repository": unique symbol;
        };
    }
    interface Factory<E extends social.common.ddd.Entity<any /*UnknownType **/>> {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.ddd.Factory": unique symbol;
        };
    }
    interface DomainEvent {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.ddd.DomainEvent": unique symbol;
        };
    }
}
export declare namespace social.common.endpoint {
    const Endpoint: {
        get USER(): string;
    };
    const StatusCode: {
        get OK(): number;
        get CREATED(): number;
        get BAD_REQUEST(): number;
        get FORBIDDEN(): number;
        get NOT_FOUND(): number;
        get INTERNAL_SERVER_ERROR(): number;
    };
    const Port: {
        get HTTP(): number;
    };
}
export declare namespace social.common.events {
    interface ContentEvent extends social.common.ddd.DomainEvent {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.events.ContentEvent": unique symbol;
        } & social.common.ddd.DomainEvent["__doNotUseOrImplementIt"];
    }
    interface Alert extends social.common.ddd.DomainEvent {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.events.Alert": unique symbol;
        } & social.common.ddd.DomainEvent["__doNotUseOrImplementIt"];
    }
    class PostPublished implements social.common.events.ContentEvent {
        constructor(title: string, username: string, content: string);
        get title(): string;
        get username(): string;
        get content(): string;
        copy(title?: string, username?: string, content?: string): social.common.events.PostPublished;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.ContentEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class ContentAlert implements social.common.events.Alert {
        constructor(title: string, username: string, content: string);
        get title(): string;
        get username(): string;
        get content(): string;
        copy(title?: string, username?: string, content?: string): social.common.events.ContentAlert;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.Alert["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
}
export declare namespace social.common.events {
    interface FriendshipEvent extends social.common.ddd.DomainEvent {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.events.FriendshipEvent": unique symbol;
        } & social.common.ddd.DomainEvent["__doNotUseOrImplementIt"];
    }
    class FriendshipRequestSent implements social.common.events.FriendshipEvent {
        constructor(sender: string, receiver: string);
        get sender(): string;
        get receiver(): string;
        copy(sender?: string, receiver?: string): social.common.events.FriendshipRequestSent;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.FriendshipEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class FriendshipRequestAccepted implements social.common.events.FriendshipEvent {
        constructor(sender: string, receiver: string);
        get sender(): string;
        get receiver(): string;
        copy(sender?: string, receiver?: string): social.common.events.FriendshipRequestAccepted;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.FriendshipEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class FriendshipRequestRejected implements social.common.events.FriendshipEvent {
        constructor(sender: string, receiver: string);
        get sender(): string;
        get receiver(): string;
        copy(sender?: string, receiver?: string): social.common.events.FriendshipRequestRejected;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.FriendshipEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
}
export declare namespace social.common.events {
    interface MessageEvent extends social.common.ddd.DomainEvent {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.events.MessageEvent": unique symbol;
        } & social.common.ddd.DomainEvent["__doNotUseOrImplementIt"];
    }
    class MessageSent implements social.common.events.MessageEvent {
        constructor(sender: string, receiver: string, message: string);
        get sender(): string;
        get receiver(): string;
        get message(): string;
        copy(sender?: string, receiver?: string, message?: string): social.common.events.MessageSent;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.MessageEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class MessageReceived implements social.common.events.MessageEvent {
        constructor(sender: string, receiver: string, message: string);
        get sender(): string;
        get receiver(): string;
        get message(): string;
        copy(sender?: string, receiver?: string, message?: string): social.common.events.MessageReceived;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.MessageEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
}
export declare namespace social.common.events {
    interface UserEvent extends social.common.ddd.DomainEvent {
        readonly __doNotUseOrImplementIt: {
            readonly "social.common.events.UserEvent": unique symbol;
        } & social.common.ddd.DomainEvent["__doNotUseOrImplementIt"];
    }
    class UserCreated implements social.common.events.UserEvent {
        constructor(username: string, email: string);
        get username(): string;
        get email(): string;
        copy(username?: string, email?: string): social.common.events.UserCreated;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.UserEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class UserBlocked implements social.common.events.UserEvent {
        constructor(username: string);
        get username(): string;
        copy(username?: string): social.common.events.UserBlocked;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.UserEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class UserUnblocked implements social.common.events.UserEvent {
        constructor(username: string);
        get username(): string;
        copy(username?: string): social.common.events.UserUnblocked;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.UserEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class UserLoggedOut implements social.common.events.UserEvent {
        constructor(username: string);
        get username(): string;
        copy(username?: string): social.common.events.UserLoggedOut;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.UserEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class AdminLoggedOut implements social.common.events.UserEvent {
        constructor(username: string);
        get username(): string;
        copy(username?: string): social.common.events.AdminLoggedOut;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.UserEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class UserLoggedIn implements social.common.events.UserEvent {
        constructor(username: string);
        get username(): string;
        copy(username?: string): social.common.events.UserLoggedIn;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.UserEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
    class AdminLoggedIn implements social.common.events.UserEvent {
        constructor(username: string);
        get username(): string;
        copy(username?: string): social.common.events.AdminLoggedIn;
        toString(): string;
        hashCode(): number;
        equals(other: Nullable<any>): boolean;
        readonly __doNotUseOrImplementIt: social.common.events.UserEvent["__doNotUseOrImplementIt"];
        static get Companion(): {
            get TOPIC(): string;
        };
    }
}