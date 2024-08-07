package au.com.kahaara.wf.orchestration.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A generic cache that is used for caching the rules
 *
 * @param <K> Key The key name
 * @param <V> Value The value / object
 */
@Component
public class GenericCache<K, V> implements GenericCacheInterface<K, V> {

    public static final Logger log = LoggerFactory.getLogger(GenericCache.class);

    public static final Long DEFAULT_CACHE_TIMEOUT = 60000L;

    protected Map<K, CacheValue<V>> cacheMap;
    protected Long cacheTimeout;

    public GenericCache() {
        this(DEFAULT_CACHE_TIMEOUT);
    }

    public GenericCache(Long cacheTimeout) {
        log.debug("Creating cache for {}",this.getClass().getSuperclass());
        this.cacheTimeout = cacheTimeout;
        this.clear();
    }

    /**
     * Clear the whole cache
     */
    @Override
    public void clear() {
        log.debug("Clearing cache");
        this.cacheMap = new HashMap<>();
        //this.cacheMap.clear()
    }

    /**
     * Clean out any expired cache keys
     */
    @Override
    public void clean() {
        log.debug("Cleaning out expired entries from cache");
        for(K key: this.getExpiredKeys()) {
            this.remove(key);
        }
    }

    /**
     * Remove an entry from the cache
     *
     * @param key The key
     */
    @Override
    public void remove(K key) {
        log.debug("Removing {} from cache",key);
        this.cacheMap.remove(key);
    }

    /**
     * See if the cache entry exists
     *
     * @param key The key
     * @return true if it exists
     */
    @Override
    public boolean containsKey(K key) {
        log.debug("looking for {} in cache",key);
        return this.cacheMap.containsKey(key);
    }

    /**
     * Get any expired keys from the cache
     *
     * @return A set of keys
     */
    protected Set<K> getExpiredKeys() {
        log.debug("Getting expired keys from cache");
        return this.cacheMap.keySet().parallelStream()
                .filter(this::isExpired)
                .collect(Collectors.toSet());
    }

    /**
     * See if the key has expired
     *
     * @param key The key
     * @return true if the key has expired
     */
    protected boolean isExpired(K key) {
        LocalDateTime expirationDateTime = this.cacheMap.get(key).getCreatedAt().plus(this.cacheTimeout, ChronoUnit.MILLIS);
        return LocalDateTime.now().isAfter(expirationDateTime);
    }

    /**
     * Get the object out of the cache
     *
     * @param key The key
     * @return an optional object if it exists
     */
    @Override
    public Optional<V> get(K key) {
        this.clean();
        return Optional.ofNullable(this.cacheMap.get(key)).map(CacheValue::getValue);
    }

    /**
     * Put a cache entry into the map. This will override an existing one of the
     * same name
     * @param key The key
     * @param value The related object
     */
    @Override
    public void put(K key, V value) {
        this.cacheMap.put(key, this.createCacheValue(value));
    }

    /**
     * The cache value / object. This allows the object to be timestamped
     *
     * @param value V value of the object
     * @return The object cache value
     */
    protected CacheValue<V> createCacheValue(V value) {
        LocalDateTime now = LocalDateTime.now();
        return new CacheValue<V>() {
            @Override
            public V getValue() {
                return value;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return now;
            }
        };
    }

    /**
     * Allow the storage of the date time stamp along with the stored object
     * @param <V> The object type
     */
    protected interface CacheValue<V> {
        V getValue();

        LocalDateTime getCreatedAt();
    }
}
