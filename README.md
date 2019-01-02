# HazelCast Quirks

Having had issues with hazelcast/payara-micro, I did some digging and found some quirks.

## Iterate over cache with exactly one element

This fails consistently when there's only one element in the cache

    for (Iterator<Cache.Entry> iterator = cache.iterator() ; iterator.hasNext() ;) {
        Cache.Entry next = iterator.next();


with: `java.util.NoSuchElementException` from `com.hazelcast.cache.impl.AbstractClusterWideIterator.next`

## Set component of cache key has been cached before (serialized?)

If a `Set<>` cache key (or a complex key with a `Set<>` component) has been stored
in a cache, it does not match even when `.hashCode()` returns the same value and
`.equals(...)` returns true.

    curl http://localhost:8080/hazelcast-quirks-1.0-SNAPSHOT/false

1st run:

 * Cache miss on cache-1, then generated a Set, and stores it.
 * Cache miss on cache-2 (newly generated set as key) generated a value and stores it
 * Dumping cache fails (due to the 1 element issue)

2nd run:

 * Cache hit on cache-1
 * Expected cache hit on cache-2, but gets cache-miss, generated a new value and stores it
 * Dumping cache now return 2 entries both with the values from 2nd run and same key

3rd run:

 * Cache hit on cache-1
 * Cache hit on cache-2
 * Still 2 (identical) entries in

If a copy is made of the set, and that is used for cache-key in cache-2

    curl http://localhost:8080/hazelcast-quirks-1.0-SNAPSHOT/true

1st run: exactly as 1st run before

2nd run:

 * Cache hit on cache-1
 * Cache hit on cache-2
 * Still one element in the cache, dump fails

## How to run service

    mvn clean package && java -jar payara-micro-5.183.jar --deploy target/hazelcast-quirks-1.0-SNAPSHOT.war
