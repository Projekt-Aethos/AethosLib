package de.aethos.lib.resourcepack.pack;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public record Artifact(boolean ok, String message, String id, String name, @Nullable Path file, long sizeBytes,
                    int entryCount, int packFormat, String sha1Hex, @Nullable Validation validation,
                    List<String> log) {

    public static Artifact fail(final String id, final String name, final String message, final List<String> log) {
        return new Artifact(false, message, id, name, null, 0, 0, 0, "-", null, log);
    }

    public Artifact withValidation(final Validation result) {
        return new Artifact(ok, message, id, name, file, sizeBytes, entryCount, packFormat, sha1Hex, result, log);
    }

    public UUID uuid() {
        return UUID.nameUUIDFromBytes(("aethosrp:" + id).getBytes(StandardCharsets.UTF_8));
    }

    public boolean sendable() {
        return ok && validation != null && validation.ok();
    }

    public String sizeHuman() {
        return Assembler.human(sizeBytes);
    }

    public String sha1Short() {
        return sha1Hex.length() >= 12 ? sha1Hex.substring(0, 12) : sha1Hex;
    }

    public record Validation(List<String> errors, List<String> warnings) {

        public boolean ok() {
            return errors.isEmpty();
        }

        public String summary() {
            if (errors.isEmpty() && warnings.isEmpty()) {
                return "OK";
            }
            final StringBuilder summary = new StringBuilder(ok() ? "OK" : "FEHLER");
            if (!errors.isEmpty()) {
                summary.append(" – ").append(String.join("; ", errors));
            }
            if (!warnings.isEmpty()) {
                summary.append(ok() ? " – Hinweise: " : " | Hinweise: ").append(String.join("; ", warnings));
            }
            return summary.toString();
        }
    }
}
