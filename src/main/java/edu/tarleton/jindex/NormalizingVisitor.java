package edu.tarleton.jindex;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ArrayType.Origin;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Properties;

/**
 * The visitor that implements normalization, such as adding curly braces.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class NormalizingVisitor extends VoidVisitorAdapter<Void> {

    private final boolean addBlocks;
    private final boolean concatenateStrings;
    private final boolean ignoreParentheses;
    private final boolean treatArrayDeclEqual;

    public NormalizingVisitor(Properties conf) {
        addBlocks = Boolean.parseBoolean(conf.getProperty("addBlocks", "false"));
        concatenateStrings = Boolean.parseBoolean(conf.getProperty("concatenateStrings", "false"));
        ignoreParentheses = Boolean.parseBoolean(conf.getProperty("ignoreParentheses", "false"));
        treatArrayDeclEqual = Boolean.parseBoolean(conf.getProperty("treatArrayDeclEqual", "false"));
    }

    public void visitBlockStmt(Statement stmt) {
        if (stmt.isBlockStmt()) {
            visit(stmt.asBlockStmt(), null);
            return;
        }
        throw new AssertionError("invalid statement: " + stmt);
    }

    @Override
    public void visit(ArrayType n, Void arg) {
        if (treatArrayDeclEqual && n.getOrigin() == Origin.NAME) {
            n.setOrigin(Origin.TYPE);
        }
    }

    @Override
    public void visit(BinaryExpr n, Void arg) {
        super.visit(n, arg);
        if (concatenateStrings) {
            concatenateStrings(n);
        }
    }

    private void concatenateStrings(BinaryExpr n) {
        if (n.getOperator() == BinaryExpr.Operator.PLUS) {
            Expression left = n.getLeft();
            Expression right = n.getRight();
            if (left.isStringLiteralExpr() && right.isStringLiteralExpr()) {
                StringLiteralExpr sle1 = left.asStringLiteralExpr();
                StringLiteralExpr sle2 = right.asStringLiteralExpr();
                StringLiteralExpr concat = new StringLiteralExpr(sle1.asString() + sle2.asString());
                Node parent = n.getParentNode().get();
                parent.replace(n, concat);
                return;
            }
            if (left.isBinaryExpr() && right.isStringLiteralExpr()) {
                BinaryExpr n2 = left.asBinaryExpr();
                Expression right2 = n2.getRight();
                if (n2.getOperator() == BinaryExpr.Operator.PLUS
                        && right2.isStringLiteralExpr()) {
                    StringLiteralExpr sle1 = right2.asStringLiteralExpr();
                    StringLiteralExpr sle2 = right.asStringLiteralExpr();
                    StringLiteralExpr concat = new StringLiteralExpr(sle1.asString() + sle2.asString());
                    n2.replace(right2, concat);
                    Node parent = n.getParentNode().get();
                    parent.replace(n, n2);
                }
            }
        }
    }

    @Override
    public void visit(EnclosedExpr n, Void arg) {
        if (ignoreParentheses) {
            removeParentheses(n);
        }
        super.visit(n, arg);
    }

    private void removeParentheses(EnclosedExpr n) {
        Node parent = n.getParentNode().get();
        parent.replace(n, n.getInner());
    }

    @Override
    public void visit(ForEachStmt n, Void arg) {
        Statement body = n.getBody();
        if (addBlocks && !body.isBlockStmt()) {
            BlockStmt block = new BlockStmt();
            block.addStatement(body);
            n.setBody(block);
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        Statement body = n.getBody();
        if (addBlocks && !body.isBlockStmt()) {
            BlockStmt block = new BlockStmt();
            block.addStatement(body);
            n.setBody(block);
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(IfStmt n, Void arg) {
        Statement thenStmt = n.getThenStmt();
        if (addBlocks && !thenStmt.isBlockStmt()) {
            BlockStmt block = new BlockStmt();
            block.addStatement(thenStmt);
            n.setThenStmt(block);
        }
        Statement elseStmt = n.getElseStmt().orElse(null);
        if (addBlocks && elseStmt != null && !elseStmt.isBlockStmt()) {
            BlockStmt block = new BlockStmt();
            block.addStatement(elseStmt);
            n.setElseStmt(block);
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        Statement body = n.getBody();
        if (addBlocks && !body.isBlockStmt()) {
            BlockStmt block = new BlockStmt();
            block.addStatement(body);
            n.setBody(block);
        }
        super.visit(n, arg);
    }
}
