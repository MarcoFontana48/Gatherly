package social.common.ddd

/**
 * Marker interface to easily identify a domain object as service.
 */
interface Service

/**
 * class to identify an object as ID
 */
open class ID<I> (open val id: I) : ValueObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ID<*>) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return id.toString()
    }
}

/**
 * class to identify a DDD object as Entity
 */
open class Entity<I : ID<*>>(open val id: I) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity<*>) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return id.toString()
    }
}

/**
 * Marker interface to easily identify a domain object as value object.
 */
interface ValueObject

/**
 * Marker interface to easily identify a domain object as aggregate root.
 */
open class AggregateRoot<I : ID<*>> (id: I) : Entity<I>(id)

/**
 * Marker interface to easily identify a domain object as repository.
 */
interface Repository<I : ID<*>, E : Entity<*>> {
    fun findById(id: I): E?
    fun save(entity: E)
    fun deleteById(id: I): E?
    fun findAll(): Iterable<E>
    fun update(entity: E)
}

/**
 * Marker interface to easily identify a domain object as factory.
 */
interface Factory<E : Entity<*>>

/**
 * Marker interface to easily identify a domain object as domain event
 */
interface DomainEvent
