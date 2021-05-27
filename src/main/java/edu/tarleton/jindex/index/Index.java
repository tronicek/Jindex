package edu.tarleton.jindex.index;

import edu.tarleton.jindex.Pos;
import java.util.List;

/**
 * The AST index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public interface Index {

    final String[] STATEMENTS = {
        "AssertStmt",
        "BreakStmt",
        "ContinueStmt",
        "DoStmt",
        "EmptyStmt",
        "ExplicitConstructorInvocationStmt",
        "ExpressionStmt",
        "ForEachStmt",
        "ForStmt",
        "IfStmt",
        "LabeledStmt",
        "ReturnStmt",
        "SwitchStmt",
        "SynchronizedStmt",
        "ThrowStmt",
        "TryStmt",
        "WhileStmt"
    };

    void add(List<String> labels, Pos pos);

    void nextStmt(Pos prev, Pos curr);

    void print() throws Exception;
}
