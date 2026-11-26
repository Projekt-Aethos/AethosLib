package de.aethos.lib.xp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public final class PackBuilder {

    public static final int FRAMES = 6;
    public static final int SPRITES = 11;
    private static final long ENTRY_TIME = 315532800000L + 86_400_000L; // 1980-01-02

    private PackBuilder() {
    }


    public static byte[] build(BufferedImage source, int[] format, String description, double opacity) {
        if (source.getWidth() < 4 || source.getWidth() % 4 != 0 || source.getHeight() < source.getWidth() * 3 / 4) {
            throw new IllegalArgumentException("Kugel-Textur hat nicht den Vanilla-Aufbau (4×4 Raster): "
                    + source.getWidth() + "×" + source.getHeight());
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            put(zip, "pack.mcmeta", packMeta(format, description));
            for (XPType type : XPType.values()) {
                for (int sprite = 0; sprite < SPRITES; sprite++) {
                    String name = "xp/" + type.id() + "_" + sprite;
                    put(zip, "assets/aethos/textures/item/" + name + ".png",
                            png(frames(source, type, sprite, opacity)));
                    put(zip, "assets/aethos/textures/item/" + name + ".png.mcmeta",
                            "{ \"animation\": { \"frametime\": 2, \"interpolate\": true } }\n");
                    put(zip, "assets/aethos/models/item/" + name + ".json",
                            "{ \"parent\": \"minecraft:item/generated\", \"textures\": { \"layer0\": \"aethos:item/"
                                    + name + "\" } }\n");
                    put(zip, "assets/aethos/items/item/" + name + ".json",
                            "{ \"model\": { \"type\": \"minecraft:model\", \"model\": \"aethos:item/" + name + "\" } }\n");
                }
            }
            // Vanilla-Kugel unsichtbar: gleiche Größe, alles durchsichtig
            put(zip, "assets/minecraft/textures/entity/experience/experience_orb.png",
                    png(new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    /**
     * Ein Sprite einer Art als senkrechter Streifen aus {@link #FRAMES} Bildern. Die Farbe pulsiert
     * wie beim Client (dort: Rot pulsierend, Grün voll, Blau höchstens 20 %).
     */
    static BufferedImage frames(BufferedImage source, XPType type, int sprite, double opacity) {
        int cell = source.getWidth() / 4;
        BufferedImage out = new BufferedImage(cell, cell * FRAMES, BufferedImage.TYPE_INT_ARGB);
        for (int f = 0; f < FRAMES; f++) {
            double phi = 2 * Math.PI * f / FRAMES;
            double p = (Math.sin(phi) + 1) / 2, q = (Math.sin(phi + 4.1887903) + 1) / 2;
            double[] m = switch (type) {
                case VANILLA -> new double[]{p, 1, 0.2 * q};                  // grün ↔ gelb
                case FIGHTING -> new double[]{1, 0.3 * p, 0.2 * q};              // rot ↔ orangerot
                case CRAFTING -> new double[]{0.25 * p, 0.35 + 0.45 * p, 1};  // blau ↔ hellblau
            };
            for (int y = 0; y < cell; y++) {
                for (int x = 0; x < cell; x++) {
                    int c = source.getRGB((sprite % 4) * cell + x, (sprite / 4) * cell + y);
                    int al = Math.clamp((int) ((c >>> 24) * opacity), 0, 255);
                    int r = (int) (((c >> 16) & 255) * m[0]);
                    int g = (int) (((c >> 8) & 255) * m[1]);
                    int b = (int) ((c & 255) * m[2]);
                    out.setRGB(x, f * cell + y, al == 0 ? 0 : (al << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }
        return out;
    }

    static String packMeta(int[] format, String description) {
        String desc = description.replace("\\", "\\\\").replace("\"", "\\\"");
        // pack_format für ältere Clients, min/max_format für Clients ab 1.21.9
        return "{\n  \"pack\": {\n    \"description\": \"" + desc + "\",\n"
                + "    \"pack_format\": " + format[0] + ",\n"
                + "    \"min_format\": [" + format[0] + ", " + format[1] + "],\n"
                + "    \"max_format\": [" + format[0] + ", " + format[1] + "]\n  }\n}\n";
    }

    public static String sha1(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static void put(ZipOutputStream zip, String path, String text) throws IOException {
        put(zip, path, text.getBytes(StandardCharsets.UTF_8));
    }

    private static void put(ZipOutputStream zip, String path, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        entry.setTime(ENTRY_TIME);
        zip.putNextEntry(entry);
        zip.write(data);
        zip.closeEntry();
    }
}
