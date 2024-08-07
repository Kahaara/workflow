package au.com.kahaara.wf.orchestration.cache;


import java.util.Optional;

/**
 * The cache interface used by the configuration bean
 */
public interface GenericCacheInterface<K, V>  {

    void clean();

    void clear();

    boolean containsKey(K key);

    Optional<V> get(K key);

    void put(K key, V value);

    void remove(K key);

}
