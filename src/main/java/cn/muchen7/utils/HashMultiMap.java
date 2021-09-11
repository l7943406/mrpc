package cn.muchen7.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author muchen
 */
public class HashMultiMap<K, V> {
    private final Map<K, Set<V>> map;

    public HashMultiMap() {
        map = new ConcurrentHashMap<>();
    }

    public void put(K key, Set<V> value) {
        map.putIfAbsent(key, new CopyOnWriteArraySet<>());
        map.get(key).addAll(value);
    }

    public Set<V> get(K key) {
        return map.get(key);
    }
}
