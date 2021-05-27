package edu.tarleton.jindex.search;

import com.github.javaparser.ParserConfiguration;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

/**
 * The common parent of parsers.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public abstract class Parser {

    protected final Properties conf;
    protected final ParserConfiguration parserConfiguration = new ParserConfiguration();

    public static Parser instantiate(Properties conf) {
        return new StatementParser(conf);
    }

    protected Parser(Properties conf) {
        this.conf = conf;
        prepareParserConfiguration();
    }

    private void prepareParserConfiguration() {
        boolean preprocessUnicodeEscapes = Boolean.parseBoolean(conf.getProperty("preprocessUnicodeEscapes"));
        String languageLevel = conf.getProperty("languageLevel", "JAVA_8");
        String sourceEncoding = conf.getProperty("sourceEncoding", "UTF-8");
        ParserConfiguration.LanguageLevel lang = ParserConfiguration.LanguageLevel.valueOf(languageLevel);
        parserConfiguration.setPreprocessUnicodeEscapes(preprocessUnicodeEscapes);
        parserConfiguration.setLanguageLevel(lang);
        Charset cs = Charset.forName(sourceEncoding);
        parserConfiguration.setCharacterEncoding(cs);
    }

    public abstract List<String> parseRename(String code, boolean normalize);
}
