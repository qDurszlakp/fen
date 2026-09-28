package com.sandbox.server.playground;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class SelfCache<K, V> {

    private static final int MAX_SIZE = 1000;

    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final Queue<K> orderQueue = new ConcurrentLinkedQueue<>();
    private final Object writeLock = new Object();

    public Optional<V> get(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return Optional.ofNullable(cache.get(key)); 
    }

    public V get(K key, Function<K, V> loader) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(loader, "Loader cannot be null");

        V existingVal = cache.get(key);
        if (existingVal != null) {
            return existingVal;
        }

        synchronized (writeLock) {
            existingVal = cache.get(key);
            if (existingVal != null) {
                return existingVal;
            }

            V computedVal = loader.apply(key);
            if (computedVal != null) {
                this.set(key, computedVal);
            }
            return computedVal;
        }
    }

    public void set(K key, V val) {
        if (key == null || val == null) {
            throw new RuntimeException("Wrong input");
        }

        synchronized (writeLock) {
            if (!cache.containsKey(key)) {
                while (cache.size() >= MAX_SIZE) {
                    K oldestKey = orderQueue.poll();
                    if (oldestKey != null) {
                        cache.remove(oldestKey);
                    } else {
                        break;
                    }
                }
                orderQueue.offer(key);
            }
            cache.put(key, val);
        }
    }

    public void evict(K key) {
        synchronized (writeLock) {
            cache.remove(key);
            orderQueue.remove(key);
        }
    }

    public void clear() {
        synchronized (writeLock) {
            cache.clear();
            orderQueue.clear();
        }
    }
}

