package edu.tarleton.jindex.index;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.tarleton.jindex.index.compressed.CTrie;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The visitor that builds compressed index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public abstract class CompressedIndexBuilder extends VoidVisitorAdapter<Void> {

    protected final String project;
    protected final Path srcDir;

    public CompressedIndexBuilder(Properties conf, Path srcDir) {
        project = conf.getProperty("project");
        this.srcDir = srcDir;
    }

    public abstract CTrie getTrie();

    public abstract void reset();
}
