package edu.tarleton.jindex;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import edu.tarleton.jindex.index.compressed.persistent.CPEngine;
import edu.tarleton.jindex.index.plain.persistent.PlainPersistentEngine;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

/**
 * The class that builds the index and finds the clones.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public abstract class Engine {

    protected final Properties conf;
    protected final String sourceDir;
    protected final boolean printStatistics;
    protected final boolean printTrie;
    protected final boolean verbose;
    protected final boolean preprocessUnicodeEscapes;
    protected final String languageLevel;
    protected final String sourceEncoding;
    protected final ParserConfiguration parserConfiguration = new ParserConfiguration();
    protected final CountingVisitor countingVisitor;
    protected final Statistics statistics;
    protected int fileCount;

    protected Engine(Properties conf) {
        this.conf = conf;
        sourceDir = conf.getProperty("sourceDir");
        printStatistics = Boolean.parseBoolean(conf.getProperty("printStatistics"));
        printTrie = Boolean.parseBoolean(conf.getProperty("printTrie"));
        verbose = Boolean.parseBoolean(conf.getProperty("verbose"));
        preprocessUnicodeEscapes = Boolean.parseBoolean(conf.getProperty("preprocessUnicodeEscapes"));
        languageLevel = conf.getProperty("languageLevel", "JAVA_8");
        sourceEncoding = conf.getProperty("sourceEncoding", "UTF-8");
        prepareParserConfiguration();
        countingVisitor = printStatistics ? new CountingVisitor() : null;
        statistics = printStatistics ? new Statistics() : null;
    }

    private void prepareParserConfiguration() {
        LanguageLevel lang = LanguageLevel.valueOf(languageLevel);
        Charset cs = Charset.forName(sourceEncoding);
        parserConfiguration.setPreprocessUnicodeEscapes(preprocessUnicodeEscapes);
        parserConfiguration.setLanguageLevel(lang);
        parserConfiguration.setCharacterEncoding(cs);
    }

    public static Engine instance(Properties conf) {
        boolean compressed = Boolean.parseBoolean(conf.getProperty("compressed"));
        return compressed ? new CPEngine(conf) : new PlainPersistentEngine(conf);
    }

    public abstract void perform() throws Exception;

    public abstract List<Pos> find(String code) throws Exception;
}
