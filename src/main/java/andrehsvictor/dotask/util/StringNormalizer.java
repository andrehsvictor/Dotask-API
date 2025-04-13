package andrehsvictor.dotask.util;

public class StringNormalizer {

    public static String normalize(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return str.trim()
                .strip()
                .toLowerCase();
    }
}
