package edu.tarleton.jindex.search;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.Statement;
import edu.tarleton.jindex.NormalizingVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The parser that parses statements.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class StatementParser extends Parser {

    public StatementParser(Properties conf) {
        super(conf);
    }

    @Override
    public List<String> parseRename(String code, boolean normalize) {
        JavaParser parser = new JavaParser(parserConfiguration);
        Statement invoc = null;
        String thisOrSuper = constructorInvocation(code);
        if (!thisOrSuper.isEmpty()) {
            invoc = parseConstructorInvocation(parser, thisOrSuper);
            code = code.substring(thisOrSuper.length());
        }
        Statement stmt = parseBlock(parser, "{ " + code + " }");
        if (normalize) {
            NormalizingVisitor norm = new NormalizingVisitor(conf);
            norm.visitBlockStmt(stmt);
        }
        List<String> tokens = new ArrayList<>();
        if (invoc != null) {
            List<String> itt = tokenize(invoc);
            tokens.addAll(itt);
        }
        List<String> stt2 = tokenize(stmt);
        stt2 = stt2.subList(1, stt2.size() - 1);
        tokens.addAll(stt2);
        return tokens;
    }

    private String constructorInvocation(String code) {
        String str = code.trim();
        Pattern pat = Pattern.compile("super\\s*\\(");
        Matcher m = pat.matcher(str);
        if (m.find() && m.start() == 0) {
            int i = findCallEnd(str, m.end());
            return str.substring(0, i);
        }
        Pattern pat2 = Pattern.compile("this\\s*\\(");
        Matcher m2 = pat2.matcher(str);
        if (m2.find() && m2.start() == 0) {
            int i = findCallEnd(str, m2.end());
            return str.substring(0, i);
        }
        return "";
    }

    private int findCallEnd(String code, int i) {
        int c = 1;
        for (; c > 0; i++) {
            char ch = code.charAt(i);
            switch (ch) {
                case '(':
                    c++;
                    break;
                case ')':
                    c--;
            }
        }
        while (code.charAt(i) != ';') {
            i++;
        }
        return i + 1;
    }

    private Statement parseConstructorInvocation(JavaParser parser, String code) {
        ParseResult<ExplicitConstructorInvocationStmt> result = parser.parseExplicitConstructorInvocationStmt(code);
        if (!result.isSuccessful()) {
            System.err.println("parser error " + result.getProblems());
            throw new AssertionError();
        }
        return result.getResult().get();
    }

    private Statement parseBlock(JavaParser parser, String code) {
        ParseResult<BlockStmt> result = parser.parseBlock(code);
        if (!result.isSuccessful()) {
            System.err.println("parser error " + result.getProblems());
            throw new AssertionError();
        }
        return result.getResult().get();
    }

    private List<String> tokenize(Statement stmt) {
        Tokenizer tok = new Tokenizer(conf);
        stmt.accept(tok, null);
        return tok.getLabels();
    }
}
