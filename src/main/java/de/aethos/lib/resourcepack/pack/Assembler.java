package de.aethos.lib.resourcepack.pack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class Assembler {

    private static final String META = "pack.mcmeta";

    private static final String COMBINED_ID = "server-pack";

    private static final Gson GSON = new Gson();

    private static final Set<String> COMMENT_KEYS =
            Set.of("comment", "_comment", "__comment", "credit", "credits", "_credit");

    private static final long ENTRY_TIME = 315532800000L + 86_400_000L;

    private static final List<String> JUNK = List.of(
            ".ds_store", "thumbs.db", ".gitignore", ".gitattributes", "desktop.ini");

    private static final List<String> JUNK_EXTENSIONS = List.of(".md", ".psd", ".xcf", ".bak");

    private final boolean compression;

    private final Format format;

    private final String description;

    public Assembler(final boolean compression, final Format format, final String description) {
        this.compression = compression;
        this.format = format;
        this.description = description;
    }

    public static String human(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    public static void deleteRecursive(final Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    public static Artifact.Validation validate(final Path zip, final long maxBytes) {
        final List<String> errors = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        try {
            final long size = Files.size(zip);
            if (size == 0) {
                errors.add("Pack ist 0 Byte groß.");
            }
            if (maxBytes > 0 && size > maxBytes) {
                errors.add("Pack zu groß (" + human(size) + " > " + human(maxBytes) + ").");
            }
        } catch (final IOException e) {
            errors.add("Pack-Datei nicht lesbar: " + e.getMessage());
        }

        boolean hasEntries = false;
        boolean hasAssets = false;
        byte[] meta = null;
        int badJson = 0;
        String firstBadJson = null;
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                hasEntries = true;
                String path = entry.getName().replace('\\', '/');
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                if (path.startsWith("assets/")) {
                    hasAssets = true;
                }
                if (entry.isDirectory()) {
                    continue;
                }
                final String lower = path.toLowerCase(Locale.ROOT);
                if (path.equals(META)) {
                    meta = in.readAllBytes();
                } else if (lower.endsWith(".json") || lower.endsWith(".mcmeta")) {
                    try {
                        parseLenient(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                    } catch (final RuntimeException e) {
                        badJson++;
                        if (firstBadJson == null) {
                            firstBadJson = path;
                        }
                    }
                }
            }
        } catch (final IOException | RuntimeException e) {
            errors.add("ZIP nicht lesbar: " + e.getMessage());
        }

        if (!hasEntries) {
            errors.add("ZIP ist leer.");
        }
        if (meta == null) {
            errors.add("pack.mcmeta fehlt im Wurzelverzeichnis.");
        } else if (format(meta) <= 0) {
            errors.add("pack.mcmeta ohne gültiges pack_format oder min_format.");
        }
        if (!hasAssets) {
            warnings.add("Kein assets/-Ordner gefunden.");
        }
        if (badJson > 0) {
            warnings.add(badJson + " JSON-Datei(en) nicht lesbar, z. B. " + firstBadJson + ".");
        }
        return new Artifact.Validation(List.copyOf(errors), List.copyOf(warnings));
    }

    private static JsonElement parseLenient(final String json) {
        final JsonReader reader = new JsonReader(new StringReader(json));
        reader.setStrictness(Strictness.LENIENT);
        return JsonParser.parseReader(reader);
    }

    private static int format(final byte[] meta) {
        try {
            final JsonElement root = parseLenient(new String(meta, StandardCharsets.UTF_8));
            if (!root.isJsonObject()) {
                return 0;
            }
            final JsonElement pack = root.getAsJsonObject().get("pack");
            if (pack == null || !pack.isJsonObject()) {
                return 0;
            }
            final JsonObject object = pack.getAsJsonObject();
            if (object.has("pack_format")) {
                return object.get("pack_format").getAsInt();
            }
            final JsonElement min = object.get("min_format");
            if (min == null) {
                return 0;
            }
            return min.isJsonArray() ? min.getAsJsonArray().get(0).getAsInt() : min.getAsInt();
        } catch (final RuntimeException e) {
            return 0;
        }
    }

    private static void stripComments(final JsonElement element) {
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            for (final String key : Set.copyOf(object.keySet())) {
                if (COMMENT_KEYS.contains(key.toLowerCase(Locale.ROOT))) {
                    object.remove(key);
                } else {
                    stripComments(object.get(key));
                }
            }
        } else if (element.isJsonArray()) {
            for (final JsonElement child : element.getAsJsonArray()) {
                stripComments(child);
            }
        }
    }

    private static JsonElement mergeModel(final String existing, final String incoming) {
        final JsonElement base = JsonParser.parseString(existing);
        final JsonElement top = JsonParser.parseString(incoming);
        if (!base.isJsonObject() || !top.isJsonObject()) {
            return top;
        }
        final JsonObject baseObject = base.getAsJsonObject();
        final JsonObject topObject = top.getAsJsonObject();
        if (!baseObject.has("overrides") && !topObject.has("overrides")) {
            return top;
        }
        final JsonArray merged = new JsonArray();
        final Set<String> seen = new LinkedHashSet<>();
        for (final JsonElement source : List.of(baseObject, topObject)) {
            final JsonElement overrides = source.getAsJsonObject().get("overrides");
            if (overrides == null || !overrides.isJsonArray()) {
                continue;
            }
            for (final JsonElement override : overrides.getAsJsonArray()) {
                if (seen.add(GSON.toJson(override))) {
                    merged.add(override);
                }
            }
        }
        topObject.add("overrides", merged);
        return topObject;
    }

    public Artifact build(final List<Source> sources, final Path work, final Path output) {
        final List<String> log = new ArrayList<>();
        try {
            final Path merged = work.resolve("merged");
            final Assembled assembled = assemble(sources, merged, log);
            if (assembled.used() == 0) {
                return Artifact.fail(COMBINED_ID, description, "Keine Quellen vorhanden.", log);
            }
            Files.writeString(merged.resolve(META), format.mcmeta(description), StandardCharsets.UTF_8);
            final int entries = finish(merged, output, log);
            final long size = Files.size(output);
            log.add("Kombiniertes Pack gebaut: " + entries + " Einträge, " + human(size) + ".");
            return new Artifact(true, "ok", COMBINED_ID, description, output, size, entries, format.major(),
                    sha1(output), null, log);
        } catch (final IOException | RuntimeException e) {
            log.add("FEHLER: " + e);
            return Artifact.fail(COMBINED_ID, description, "Build fehlgeschlagen: " + e.getMessage(), log);
        }
    }

    public Artifact buildOne(final Source source, final String id, final Path work, final Path output) {
        final List<String> log = new ArrayList<>();
        try {
            if (!Files.exists(source.path())) {
                return Artifact.fail(id, source.name(), "Quelle fehlt: " + source.path(), log);
            }
            final Path dir = work.resolve("pack-" + id);
            final Assembled assembled = assemble(List.of(source), dir, log);
            final int packFormat = writeMeta(dir, assembled.meta(), source.name());
            final int entries = finish(dir, output, log);
            final long size = Files.size(output);
            log.add("Pack '" + source.name() + "' gebaut: " + entries + " Einträge, " + human(size)
                    + ", Format " + packFormat + ".");
            return new Artifact(true, "ok", id, source.name(), output, size, entries, packFormat, sha1(output),
                    null, log);
        } catch (final IOException | RuntimeException e) {
            log.add("FEHLER: " + e);
            return Artifact.fail(id, source.name(), "Build fehlgeschlagen: " + e.getMessage(), log);
        }
    }

    private Assembled assemble(final List<Source> sources, final Path dest, final List<String> log) throws IOException {
        deleteRecursive(dest);
        Files.createDirectories(dest);
        int used = 0;
        byte[] meta = null;
        for (final Source source : sources) {
            if (!Files.exists(source.path())) {
                log.add("Übersprungen (fehlt): " + source.name());
                continue;
            }
            final byte[] found = Files.isDirectory(source.path())
                    ? mergeFolder(source, dest, log)
                    : mergeZip(source, dest, log);
            if (found != null) {
                meta = found;
            }
            used++;
        }
        return new Assembled(used, meta);
    }

    private byte @Nullable [] mergeFolder(final Source source, final Path dest, final List<String> log) throws IOException {
        final Path root = locatePackRoot(source.path());
        final List<Path> files;
        try (Stream<Path> paths = Files.walk(root)) {
            files = paths.filter(Files::isRegularFile).toList();
        }
        byte[] meta = null;
        for (final Path file : files) {
            final String relative = root.relativize(file).toString().replace('\\', '/');
            final byte[] data = Files.readAllBytes(file);
            if (relative.equals(META)) {
                meta = data;
            } else {
                mergeFile(relative, data, dest);
            }
        }
        log.add("Quelle '" + source.name() + "': " + files.size() + " Dateien (Ordner).");
        return meta;
    }

    private byte @Nullable [] mergeZip(final Source source, final Path dest, final List<String> log) throws IOException {
        int count = 0;
        byte[] meta = null;
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(source.path()))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String relative = entry.getName().replace('\\', '/');
                if (relative.startsWith("/")) {
                    relative = relative.substring(1);
                }
                final byte[] data = in.readAllBytes();
                if (relative.equals(META)) {
                    meta = data;
                } else {
                    mergeFile(relative, data, dest);
                }
                count++;
            }
        }
        log.add("Quelle '" + source.name() + "': " + count + " Dateien (zip).");
        return meta;
    }

    private void mergeFile(final String relative, final byte[] data, final Path dest) throws IOException {
        if (relative.isBlank() || relative.contains("..")) {
            return;
        }
        final Path target;
        try {
            target = dest.resolve(relative).normalize();
        } catch (final InvalidPathException e) {
            return;
        }
        if (!target.startsWith(dest) || target.equals(dest)) {
            return;
        }
        Files.createDirectories(target.getParent());
        if (Files.exists(target) && relative.endsWith(".json") && relative.contains("/models/")) {
            try {
                final JsonElement merged = mergeModel(Files.readString(target, StandardCharsets.UTF_8),
                        new String(data, StandardCharsets.UTF_8));
                Files.writeString(target, GSON.toJson(merged), StandardCharsets.UTF_8);
                return;
            } catch (final RuntimeException ignored) {
            }
        }
        Files.write(target, data);
    }

    private Path locatePackRoot(final Path dir) throws IOException {
        if (Files.exists(dir.resolve(META)) || Files.isDirectory(dir.resolve("assets"))) {
            return dir;
        }
        final List<Path> candidates;
        try (Stream<Path> paths = Files.walk(dir, 3)) {
            candidates = paths.filter(path -> {
                final String name = path.getFileName().toString();
                return name.equals(META) || name.equals("assets") && Files.isDirectory(path);
            }).toList();
        }
        return candidates.stream().filter(path -> path.getFileName().toString().equals(META)).findFirst()
                .or(() -> candidates.stream().findFirst())
                .map(Path::getParent).orElse(dir);
    }

    private int writeMeta(final Path dir, final byte @Nullable [] meta, final String name) throws IOException {
        if (meta != null) {
            final int found = format(meta);
            if (found > 0) {
                Files.write(dir.resolve(META), meta);
                return found;
            }
        }
        Files.writeString(dir.resolve(META), format.mcmeta(name), StandardCharsets.UTF_8);
        return format.major();
    }

    private int finish(final Path dir, final Path output, final List<String> log) throws IOException {
        if (compression) {
            compress(dir, log);
        }
        return zip(dir, output);
    }

    private void compress(final Path root, final List<String> log) throws IOException {
        int minified = 0;
        int junk = 0;
        final List<Path> files;
        try (Stream<Path> paths = Files.walk(root)) {
            files = paths.filter(Files::isRegularFile).toList();
        }
        for (final Path file : files) {
            final String relative = root.relativize(file).toString().replace('\\', '/').toLowerCase(Locale.ROOT);
            final String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            if (relative.equals(META)) {
                continue;
            }
            final boolean credit = name.startsWith("credits") || name.startsWith("license") || name.startsWith("licence");
            if (!credit && (JUNK.contains(name) || JUNK_EXTENSIONS.stream().anyMatch(name::endsWith)
                    || name.endsWith(".txt") && !relative.startsWith("assets/"))) {
                Files.delete(file);
                junk++;
                continue;
            }
            if (name.endsWith(".json") || name.endsWith(".mcmeta")) {
                try {
                    final JsonElement element = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                    stripComments(element);
                    Files.writeString(file, GSON.toJson(element), StandardCharsets.UTF_8);
                    minified++;
                } catch (final RuntimeException ignored) {
                }
            }
        }
        log.add("Kompression: " + minified + " JSON minifiziert, " + junk + " Junk-Dateien entfernt.");
    }

    private int zip(final Path root, final Path output) throws IOException {
        Files.createDirectories(output.getParent());
        final List<Path> files;
        try (Stream<Path> paths = Files.walk(root)) {
            files = paths.filter(Files::isRegularFile).sorted(Comparator.comparing(Path::toString)).toList();
        }
        final Path tmp = output.resolveSibling(output.getFileName() + ".tmp");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(tmp))) {
            out.setLevel(compression ? 9 : 6);
            for (final Path file : files) {
                final ZipEntry entry = new ZipEntry(root.relativize(file).toString().replace('\\', '/'));
                entry.setTime(ENTRY_TIME);
                out.putNextEntry(entry);
                Files.copy(file, out);
                out.closeEntry();
            }
        }
        Files.move(tmp, output, StandardCopyOption.REPLACE_EXISTING);
        return files.size();
    }

    private String sha1(final Path file) throws IOException {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
                in.transferTo(java.io.OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public record Source(Path path, String name) {
    }

    private record Assembled(int used, byte @Nullable [] meta) {
    }
}
