package de.aethos.lib.xp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrbSprite {
    private static final int[] THRESHOLDS = {3, 7, 17, 37, 73, 149, 307, 617, 1237, 2477};

    public static final int MAX_MARKER = Short.MAX_VALUE;

    private OrbSprite() {
    }

    public static int index(int value) {
        int index = 0;
        for (int threshold : THRESHOLDS) {
            if (value >= threshold) {
                index++;
            }
        }
        return index;
    }

    public static boolean isVanillaSplitValue(int value) {
        if (value == 1) {
            return true;
        }
        for (int threshold : THRESHOLDS) {
            if (value == threshold) {
                return true;
            }
        }
        return false;
    }

    public static List<String> validate(Map<XPType, Integer> markers) {
        List<String> problems = new ArrayList<>();
        Map<Integer, XPType> spriteOwner = new HashMap<>();
        for (XPType type : XPType.values()) {
            Integer marker = markers.get(type);
            if (marker == null || marker < 1 || marker > MAX_MARKER) {
                problems.add("FEHLER: marker für " + type.id() + " muss zwischen 1 und " + MAX_MARKER + " liegen.");
                continue;
            }
            XPType other = spriteOwner.putIfAbsent(index(marker), type);
            if (other != null) {
                problems.add("FEHLER: " + type.id() + " und " + other.id() + " nutzen dasselbe Sprite "
                        + index(marker) + " – die Kugeln wären nicht unterscheidbar.");
            }
            if (isVanillaSplitValue(marker)) {
                problems.add("Warnung: marker " + marker + " (" + type.id() + ") ist ein Vanilla-Aufteilwert –"
                        + " frische Vanilla-XP kann ungezählt hineinverschmelzen.");
            }
        }
        return problems;
    }

}
