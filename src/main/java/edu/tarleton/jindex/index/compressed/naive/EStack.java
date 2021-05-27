package edu.tarleton.jindex.index.compressed.naive;

import edu.tarleton.jindex.rename.RenameStrategy;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * The implementation of stack.
 * 
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class EStack implements Iterable<EStackNode> {

    private final Deque<EStackNode> nodes = new ArrayDeque<>();

    public void push() {
        RenameStrategy rs = new RenameStrategy();
        EStackNode p = new EStackNode(rs);
        nodes.addLast(p);
    }

    public EStackNode peek() {
        return nodes.peekLast();
    }

    public EStackNode pop() {
        return nodes.removeLast();
    }

    @Override
    public Iterator<EStackNode> iterator() {
        return nodes.iterator();
    }
}
