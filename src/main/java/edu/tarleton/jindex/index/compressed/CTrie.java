package edu.tarleton.jindex.index.compressed;

import edu.tarleton.jindex.Pos;
import edu.tarleton.jindex.index.Index;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation of the compressed TRIE.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class CTrie implements Index, Serializable {

    private static final long serialVersionUID = 1L;
    private final CTrieNode root = new CTrieNode();
    private final List<String> buffer = new ArrayList<>();
    private final Map<Pos, Pos> nextStmtMap = new HashMap<>();

    public CTrieNode getRoot() {
        return root;
    }

    public Map<Pos, Pos> getNextStmtMap() {
        return nextStmtMap;
    }

    public List<String> getBuffer() {
        return buffer;
    }

    @Override
    public void nextStmt(Pos prev, Pos curr) {
        nextStmtMap.put(prev, curr);
    }

    @Override
    public void add(List<String> labels, Pos pos) {
        String first = labels.get(0);
        CTrieEdge edge = root.findEdge(first);
        if (edge == null) {
            int start = buffer.size();
            buffer.addAll(labels);
            edge = root.addEdge(buffer, start, buffer.size());
        } else {
            int cur = edge.getStart() + 1;
            for (int i = 1; i < labels.size(); i++) {
                String lab = labels.get(i);
                if (cur == edge.getEnd()) {
                    if (edge.getEnd() == buffer.size()) {
                        buffer.add(lab);
                        edge.setEnd(cur + 1);
                        cur++;
                    } else {
                        CTrieNode dest = edge.getDestination();
                        edge = dest.findEdge(lab);
                        if (edge == null) {
                            int start = buffer.size();
                            buffer.add(lab);
                            edge = dest.addEdge(buffer, start, start + 1);
                        }
                        cur = edge.getStart() + 1;
                    }
                } else {
                    String token = buffer.get(cur);
                    if (token.equals(lab)) {
                        cur++;
                    } else {
                        CTrieNode p = new CTrieNode();
                        CTrieEdge e2 = new CTrieEdge(buffer, cur, edge.getEnd(), edge.getDestination());
                        for (Pos pp : edge.removePositions()) {
                            e2.addPosition(pp);
                        }
                        p.addEdge(e2);
                        edge.setEnd(cur);
                        edge.setDestination(p);
                        CTrieNode r = new CTrieNode();
                        int start = buffer.size();
                        buffer.add(lab);
                        CTrieEdge e3 = new CTrieEdge(buffer, start, start + 1, r);
                        p.addEdge(e3);
                        edge = e3;
                        cur = start + 1;
                    }
                }
            }
        }
        if (pos != null) {
            edge.addPosition(pos);
        }
    }

    @Override
    public void print() {
        root.print();
    }
}
