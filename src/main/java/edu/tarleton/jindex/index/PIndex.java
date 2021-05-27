package edu.tarleton.jindex.index;

import edu.tarleton.jindex.Pos;
import java.util.List;

/**
 * The AST index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public interface PIndex {

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

    void print() throws Exception;
    
    List<Pos> find(List<String> tokens) throws Exception;
}
