package edu.tarleton.jindex.index.compressed;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import edu.tarleton.jindex.Engine;
import edu.tarleton.jindex.NormalizingVisitor;
import edu.tarleton.jindex.Pos;
import edu.tarleton.jindex.index.CompressedIndexBuilder;
import edu.tarleton.jindex.index.compressed.naive.SimplifiedCompressedIndexStmtBuilderNaive;
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
public class MemoryCompressedEngine extends Engine {

    public MemoryCompressedEngine(Properties conf) {
        super(conf);
    }

    @Override
    public void perform() throws Exception {
        CompressedIndexBuilder builder = createBuilder();
        processDir(builder, sourceDir);
        if (printStatistics) {
            statistics.print(false);
        }
        CTrie trie = builder.getTrie();
        if (printTrie) {
            trie.print();
        }
    }

    private CompressedIndexBuilder createBuilder() {
        Path dir = Paths.get(sourceDir).toAbsolutePath();
        return new SimplifiedCompressedIndexStmtBuilderNaive(conf, dir);
    }

    private void processDir(CompressedIndexBuilder builder, String srcDir) throws IOException {
        Path path = Paths.get(srcDir);
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        processFile(builder, srcDir, p);
                    });
        }
    }

    private void processFile(CompressedIndexBuilder builder, String srcDir, Path path) {
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
                        countingVisitor.getNodes(),
                        CTrieNode.getCount(),
                        CTrieEdge.getCount(),
                        Pos.getCount());
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
