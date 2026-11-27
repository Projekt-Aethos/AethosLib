package de.aethos.lib.resourcepack.pack;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class BuiltEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Path packFile;

    private final String sha1;

    private final long size;

    private final int layerCount;

    public BuiltEvent(final Path packFile, final String sha1, final long size, final int layerCount) {
        this.packFile = packFile;
        this.sha1 = sha1;
        this.size = size;
        this.layerCount = layerCount;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Path getPackFile() {
        return packFile;
    }

    public String getSha1() {
        return sha1;
    }

    public long getSize() {
        return size;
    }

    public int getLayerCount() {
        return layerCount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
