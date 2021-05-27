package edu.tarleton.jindex.index.plain;

import edu.tarleton.jindex.Pos;
import edu.tarleton.jindex.index.Index;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class that represents the TRIE.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class Trie implements Index, Serializable {

    private static final long serialVersionUID = 1L;
    private final TrieNode root = new TrieNode();
    private final Map<Pos, Pos> nextStmtMap = new HashMap<>();

    public TrieNode getRoot() {
        return root;
    }

    public Map<Pos, Pos> getNextStmtMap() {
        return nextStmtMap;
    }

    @Override
    public void nextStmt(Pos prev, Pos curr) {
        nextStmtMap.put(prev, curr);
    }

    @Override
    public void add(List<String> labels, Pos pos) {
        TrieNode p = root;
        int lastIndex = labels.size() - 1;
        for (int i = 0; i < lastIndex; i++) {
            String lab = labels.get(i);
            p = p.addChild(lab, null);
        }
        p.addChild(labels.get(lastIndex), pos);
    }

    @Override
    public void print() {
        root.print();
    }
}
