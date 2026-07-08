package net.sakurain.mc.aeternumgenesis.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates template identifiers used for items, mobs, skills, sets and blocks.
 * Valid IDs are lowercase alphanumeric plus dots, dashes and underscores, max 64 chars.
 */
public final class TemplateIdUtil {

    private static final Pattern VALID_ID = Pattern.compile("^[a-z0-9._-]+$");
    private static final int MAX_LENGTH = 64;

    private TemplateIdUtil() {
    }

    public static boolean isValid(String id) {
        if (id == null || id.isBlank() || id.length() > MAX_LENGTH) {
            return false;
        }
        return VALID_ID.matcher(id).matches();
    }

    public static String normalize(String id) {
        return id == null ? null : id.toLowerCase(Locale.ROOT);
    }

    public static boolean isValidNormalized(String id) {
        return isValid(id);
    }
}
