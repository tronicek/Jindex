package edu.tarleton.jindex.index.plain.persistent;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import edu.tarleton.jindex.Engine;
import edu.tarleton.jindex.NormalizingVisitor;
import edu.tarleton.jindex.Pos;
import edu.tarleton.jindex.index.IndexBuilder;
import edu.tarleton.jindex.index.plain.naive.SimplifiedIndexStmtBuilderNaive;
import edu.tarleton.jindex.index.plain.Trie;
import edu.tarleton.jindex.index.plain.persistent.PTrie.Mode;
import edu.tarleton.jindex.search.Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * The class that builds the plain (not compressed) index and finds the clones.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class PlainPersistentEngine extends Engine {

    public static final int DEFAULT_NODE_FILE_PAGE_SIZE = PNode.LENGTH * 1024 * 1024 * 60;
    public static final int DEFAULT_EDGE_FILE_PAGE_SIZE = PEdgeBlock.LENGTH * 1024 * 1024 * 4;
    public static final int DEFAULT_POS_FILE_PAGE_SIZE = PPos.LENGTH * 1024 * 1024 * 40;
    private final String nodeFileName;
    private final int nodeFilePageSize;
    private final String edgeFileName;
    private final int edgeFilePageSize;
    private final String posFileName;
    private final int posFilePageSize;
    private final String projectFileName;
    private final String pathFileName;
    private final String labelFileName;
    private final String nextStmtMapFileName;
    private final int batchFileSize;

    public PlainPersistentEngine(Properties conf) {
        super(conf);
        String dataPath = conf.getProperty("dataPath", ".");
        if (!dataPath.endsWith("/")) {
            dataPath += "/";
        }
        nodeFileName = dataPath + conf.getProperty("nodeFile");
        nodeFilePageSize = getIntProperty(conf, "nodeFilePageSize", DEFAULT_NODE_FILE_PAGE_SIZE);
        edgeFileName = dataPath + conf.getProperty("edgeFile");
        edgeFilePageSize = getIntProperty(conf, "edgeFilePageSize", DEFAULT_EDGE_FILE_PAGE_SIZE);
        posFileName = dataPath + conf.getProperty("posFile");
        posFilePageSize = getIntProperty(conf, "posFilePageSize", DEFAULT_POS_FILE_PAGE_SIZE);
        projectFileName = dataPath + conf.getProperty("projectFile");
        pathFileName = dataPath + conf.getProperty("pathFile");
        labelFileName = dataPath + conf.getProperty("labelFile");
        nextStmtMapFileName = dataPath + conf.getProperty("nextStmtMapFile");
        batchFileSize = Integer.parseInt(conf.getProperty("batchFileSize", "1000"));
    }

    private int getIntProperty(Properties conf, String name, int defaultValue) {
        String s = conf.getProperty(name);
        if (s == null) {
            return defaultValue;
        }
        return Integer.parseInt(s);
    }

    @Override
    public void perform() throws Exception {
        IndexBuilder builder = createBuilder();
        try (PTrie trie = PTrie.initialize(nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                projectFileName, pathFileName,
                labelFileName, nextStmtMapFileName)) {
            processDir(builder, sourceDir, trie);
            if (fileCount > 0) {
                Trie t = (Trie) builder.getIndex();
                trie.addTrie(t);
                if (printStatistics) {
                    statistics.store(countingVisitor.getLines(),
                            countingVisitor.getNodes(), PNode.getCount(),
                            PEdge.getCount(), PPos.getCount());
                }
            }
            if (printStatistics) {
                statistics.print(true);
            }
            if (printTrie) {
                trie.print();
            }
        }
    }

    private IndexBuilder createBuilder() {
        Path dir = Paths.get(sourceDir).toAbsolutePath();
        return new SimplifiedIndexStmtBuilderNaive(conf, dir);
    }

    private void processDir(IndexBuilder builder, String srcDir, PTrie trie) throws IOException {
        Path path = Paths.get(srcDir);
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        processFile(builder, srcDir, p, trie);
                    });
        }
    }

    private void processFile(IndexBuilder builder, String srcDir, Path path, PTrie trie) {
        String fn = path.toString().substring(srcDir.length());
        if (fn.startsWith("/") || fn.startsWith("\\")) {
            fn = fn.substring(1);
        }
        if (verbose) {
            System.out.printf("processing %s...%n", fn);
        }
        NormalizingVisitor normVisitor = new NormalizingVisitor(conf);
        Path root = Paths.get(srcDir);
        SourceRoot sourceRoot = new SourceRoot(root, parserConfiguration);
        try {
            CompilationUnit cu = sourceRoot.parse("", fn);
            if (printStatistics) {
                cu.accept(countingVisitor, null);
            }
            cu.accept(normVisitor, null);
            cu.accept(builder, null);
            fileCount++;
            if (fileCount == batchFileSize) {
                Trie t = (Trie) builder.getIndex();
                trie.addTrie(t);
                if (printStatistics) {
                    statistics.store(countingVisitor.getLines(),
                            countingVisitor.getNodes(), PNode.getCount(),
                            PEdge.getCount(), PPos.getCount());
                }
                builder.reset();
                fileCount = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Pos> find(String code) throws Exception {
        try (PTrie trie = PTrie.fromFiles(Mode.READ_ONLY,
                nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                projectFileName, pathFileName,
                labelFileName, nextStmtMapFileName)) {
            Parser parser = Parser.instantiate(conf);
            List<String> tokens = parser.parseRename(code, true);
            return trie.find(tokens);
        }
    }
}
