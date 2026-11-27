package de.aethos.lib.resourcepack.pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Server;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public record Format(int major, int minor) {

    @Nullable
    public static Format parse(final String value) {
        try {
            final String[] parts = value.trim().split("\\.", 2);
            return new Format(Integer.parseInt(parts[0]), parts.length > 1 ? Integer.parseInt(parts[1]) : 0);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    public static Format ofServer(final Server server) {
        try (InputStream in = server.getClass().getClassLoader().getResourceAsStream("version.json")) {
            if (in == null) {
                return null;
            }
            final JsonObject pack = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("pack_version");
            if (pack.has("resource_major")) {
                return new Format(pack.get("resource_major").getAsInt(),
                        pack.has("resource_minor") ? pack.get("resource_minor").getAsInt() : 0);
            }
            if (pack.has("resource")) {
                return new Format(pack.get("resource").getAsInt(), 0);
            }
            return null;
        } catch (final IOException | RuntimeException e) {
            return null;
        }
    }

    public String mcmeta(final String description) {
        final String escaped = description.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\n  \"pack\": {\n    \"description\": \"" + escaped + "\",\n"
                + "    \"pack_format\": " + major + ",\n"
                + "    \"min_format\": [" + major + ", " + minor + "],\n"
                + "    \"max_format\": [" + major + ", " + minor + "]\n  }\n}\n";
    }
}
