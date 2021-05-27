package edu.tarleton.jindex.index;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The visitor that builds plain (not compressed) index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public abstract class IndexBuilder extends VoidVisitorAdapter<Void> {

    protected final String project;
    protected final Path srcDir;

    protected IndexBuilder(Properties conf, Path srcDir) {
        project = conf.getProperty("project");
        this.srcDir = srcDir;
    }

    public abstract Index getIndex();

    public abstract void reset();
}
