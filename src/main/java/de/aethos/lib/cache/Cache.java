package de.aethos.lib.cache;


import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Cache<K, V> extends LazyMap<K, V> {

    private final ExecutorService service;
    private final Map<K, Future<V>> preCache = new ConcurrentHashMap<>();
    private final Consumer<Entry<K, V>> cleaner;

    public Cache(int size, Transformer<? super K, ? extends V> factory, Consumer<Entry<K, V>> cleaner) {
        super(new ConcurrentHashMap<>(size), factory);
        service = Executors.newSingleThreadExecutor();
        this.cleaner = cleaner;
    }

    public Cache(int size, ExecutorService service, Transformer<? super K, ? extends V> factory, Consumer<Entry<K, V>> cleaner) {
        super(new ConcurrentHashMap<>(size), factory);
        this.service = service;
        this.cleaner = cleaner;
    }

    public Future<V> precache(K k) {
        return service.submit(() -> factory.transform(k));
    }

    @Override
    public V remove(Object key) {
        preCache.remove(key);
        return super.remove(key);
    }

    public void shutdown() {
        clear();
        service.shutdown();
    }

    @Override
    public void clear() {
        for (Entry<K, V> entry : this.entrySet()) {
            cleaner.accept(entry);
        }
        preCache.clear();
        super.clear();
    }

    public void clear(K k) {
        preCache.remove(k);
        V v = remove(k);
        if (v != null || k != null) {
            cleaner.accept(new ImmutablePair<>(k, v));
        }
    }

    @Override
    public V get(Object key) {
        if (!preCache.containsKey(key)) {
            return get(key);
        }
        try {
            Future<V> future = preCache.remove(key);
            this.put((K) key, future.get());
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
