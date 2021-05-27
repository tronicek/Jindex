package edu.tarleton.jindex.index.plain;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import edu.tarleton.jindex.Engine;
import edu.tarleton.jindex.NormalizingVisitor;
import edu.tarleton.jindex.Pos;
import edu.tarleton.jindex.index.IndexBuilder;
import edu.tarleton.jindex.index.plain.naive.SimplifiedIndexStmtBuilderNaive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * The class that builds the index and finds the clones.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class MemoryPlainEngine extends Engine {

    public MemoryPlainEngine(Properties conf) {
        super(conf);
    }

    @Override
    public void perform() throws Exception {
        IndexBuilder builder = createBuilder();
        processDir(builder, sourceDir);
        if (printStatistics) {
            statistics.print(false);
        }
        Trie trie = (Trie) builder.getIndex();
        if (printTrie) {
            trie.print();
        }
    }

    private IndexBuilder createBuilder() {
        Path dir = Paths.get(sourceDir).toAbsolutePath();
        return new SimplifiedIndexStmtBuilderNaive(conf, dir);
    }

    private void processDir(IndexBuilder builder, String srcDir) throws IOException {
        Path path = Paths.get(srcDir);
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        processFile(builder, srcDir, p);
                    });
        }
    }

    private void processFile(IndexBuilder builder, String srcDir, Path path) {
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
            if (printStatistics) {
                statistics.store(countingVisitor.getLines(),
                        countingVisitor.getNodes(), TrieNode.getCount(),
                        TrieEdge.getCount(), Pos.getCount());
            }
            fileCount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Pos> find(String code) throws Exception {
        throw new AssertionError();
    }
}
