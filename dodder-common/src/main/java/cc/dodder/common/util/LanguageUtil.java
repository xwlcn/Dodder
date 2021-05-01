package cc.dodder.common.util;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;

public class LanguageUtil {

    private static LanguageDetector detector = LanguageDetectorBuilder.fromLanguages(Language.CHINESE, Language.RUSSIAN).build();

    public static Language getLanguage(String text) {
        return detector.detectLanguageOf(text);
    }
}
