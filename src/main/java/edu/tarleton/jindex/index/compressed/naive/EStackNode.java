package edu.tarleton.jindex.index.compressed.naive;

import edu.tarleton.jindex.rename.RenameStrategy;
import java.util.ArrayList;
import java.util.List;

/**
 * The stack node.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class EStackNode {

    private final RenameStrategy renameStrategy;
    private final List<String> labels = new ArrayList<>();

    public EStackNode(RenameStrategy renameStrategy) {
        this.renameStrategy = renameStrategy;
    }

    public RenameStrategy getRenameStrategy() {
        return renameStrategy;
    }

    public List<String> getLabels() {
        return labels;
    }
}
