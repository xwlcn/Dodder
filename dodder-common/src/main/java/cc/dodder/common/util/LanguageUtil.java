package cc.dodder.common.util;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LanguageUtil {

    private static List<LanguageProfile> languageProfiles;
    private static LanguageDetector detector;

    static {
        try {
            languageProfiles = new LanguageProfileReader().readBuiltIn(Arrays.asList(LdLocale.fromString("ru"), LdLocale.fromString("en")));
            detector = LanguageDetectorBuilder.create(NgramExtractors.standard()).withProfiles(languageProfiles).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLanguage(String text) {
        Optional<LdLocale> locale = detector.detect(text);
        if (locale.isPresent())
            return locale.get().getLanguage();
        return null;
    }

}
