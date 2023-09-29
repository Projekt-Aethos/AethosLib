package de.aethos.lib.cache;

import org.apache.commons.collections4.Transformer;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class ChunkCache<V> extends Cache<Chunk, V> implements Listener {


    public ChunkCache(int size, Transformer<? super Chunk, ? extends V> factory, Consumer<Entry<Chunk, V>> cleaner) {
        super(size, factory, cleaner);
    }

    public ChunkCache(int size, ExecutorService service, Transformer<? super Chunk, ? extends V> factory, Consumer<Entry<Chunk, V>> cleaner) {
        super(size, service, factory, cleaner);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        precache(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkLoadEvent event) {
        clear(event.getChunk());
    }

}
