package edu.tarleton.jindex.index.plain.naive;

import edu.tarleton.jindex.rename.RenameStrategy;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * The implementation of stack.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class Stack implements Iterable<StackNode> {

    private final Deque<StackNode> nodes = new ArrayDeque<>();

    public void push() {
        RenameStrategy rs = new RenameStrategy();
        StackNode p = new StackNode(rs);
        nodes.addLast(p);
    }

    public StackNode peek() {
        return nodes.peekLast();
    }

    public StackNode pop() {
        return nodes.removeLast();
    }

    @Override
    public Iterator<StackNode> iterator() {
        return nodes.iterator();
    }
}
