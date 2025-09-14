package priv.liten.hbg5_utils;

import org.jetbrains.annotations.Nullable;

public class HBG5StringUtils {
    public static String notNull(@Nullable String value) {
        return value == null ? "" : value;
    }
    public static int notNullCompare(@Nullable String value1, @Nullable String value2) {
        return notNull(value1).compareTo(notNull(value2));
    }
}
