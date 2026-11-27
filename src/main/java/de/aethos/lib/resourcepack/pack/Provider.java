package de.aethos.lib.resourcepack.pack;

import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
public interface Provider {
    Path provide() throws IOException;
}
