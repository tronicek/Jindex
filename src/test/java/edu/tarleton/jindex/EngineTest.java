package edu.tarleton.jindex;

import edu.tarleton.jindex.index.plain.persistent.PEdge;
import edu.tarleton.jindex.index.plain.persistent.PNode;
import edu.tarleton.jindex.index.plain.persistent.PPos;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests.
 *
 * @author Zdenek Tronicek
 */
public class EngineTest {

    private final Random rand = new Random();
    private final Set<String> names = new HashSet<>();

    @AfterClass
    public static void tearDownAll() throws Exception {
        // GC must collect the mapped buffers
        Thread.sleep(500);
        System.gc();
        Thread.sleep(500);
    }

    private List<Pos> perform(String dir, String code) throws Exception {
        Engine plain = initializePlain(dir);
        List<Pos> pp = plain.find(code);
        Engine comp = initializeCompressed(dir);
        List<Pos> pp2 = comp.find(code);
        assertEquals(pp.size(), pp2.size());
        return pp;
    }

    private List<Pos> perform(Properties conf, String dir, String code) throws Exception {
        conf.setProperty("compressed", "false");
        Engine plain = initialize(conf, dir);
        List<Pos> pp = plain.find(code);
        conf.setProperty("compressed", "true");
        Engine comp = initialize(conf, dir);
        List<Pos> pp2 = comp.find(code);
        assertEquals(pp.size(), pp2.size());
        return pp;
    }

    private Engine initializePlain(String dir) throws Exception {
        Properties conf = new Properties();
        conf.setProperty("compressed", "false");
        return initialize(conf, dir);
    }

    private Engine initializeCompressed(String dir) throws Exception {
        Properties conf = new Properties();
        conf.setProperty("compressed", "true");
        return initialize(conf, dir);
    }

    private Engine initialize(Properties conf, String dir) throws Exception {
        conf.setProperty("project", "Test");
        conf.setProperty("sourceDir", "src/test/input/" + dir);
        String nodeFileName = generateFileName("data", "nodes");
        String edgeFileName = generateFileName("data", "edges");
        String posFileName = generateFileName("data", "positions");
        String projectFileName = generateFileName("data", "projects");
        String pathFileName = generateFileName("data", "paths");
        String labelFileName = generateFileName("data", "labels");
        String linearizationFileName = generateFileName("data", "linearizations");
        String nextStmtMapFileName = generateFileName("data", "nextstmt");
        conf.setProperty("nodeFile", nodeFileName);
        conf.setProperty("nodeFilePageSize", Integer.toString(PNode.LENGTH * 1024 * 64));
        conf.setProperty("edgeFile", edgeFileName);
        conf.setProperty("edgeFilePageSize", Integer.toString(PEdge.LENGTH * 1024 * 64));
        conf.setProperty("posFile", posFileName);
        conf.setProperty("posFilePageSize", Integer.toString(PPos.LENGTH * 1024 * 64));
        conf.setProperty("projectFile", projectFileName);
        conf.setProperty("pathFile", pathFileName);
        conf.setProperty("labelFile", labelFileName);
        conf.setProperty("linearizationFile", linearizationFileName);
        conf.setProperty("nextStmtMapFile", nextStmtMapFileName);
        //conf.setProperty("printTrie", "true");
        Engine eng = Engine.instance(conf);
        eng.perform();
        return eng;
    }

    private String generateFileName(String dir, String prefix) {
        String fn;
        do {
            fn = dir + "/" + prefix + randomString(8);
        } while (names.contains(fn));
        names.add(fn);
        File file = new File(fn);
        file.deleteOnExit();
        return fn;
    }

    private String randomString(int len) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
        String str = "";
        for (; len > 0; len--) {
            int i = Math.abs(rand.nextInt() % chars.length());
            str += chars.charAt(i);
        }
        return str;
    }

    @Test
    public void test1() throws Exception {
        List<Pos> pp = perform("1", "return Math.min(x, y);");
        assertEquals(1, pp.size());
    }

    @Test
    public void test2() throws Exception {
        List<Pos> pp = perform("2", "return java.lang.Math.min(x, y);");
        assertEquals(1, pp.size());
    }

    @Test
    public void test3() throws Exception {
        List<Pos> pp = perform("3", "int m = 1 + gen.nextInt(100);");
        assertEquals(1, pp.size());
    }

    @Test
    public void test4() throws Exception {
        List<Pos> pp = perform("4", "return x + y;");
        assertEquals(1, pp.size());
    }

    @Test
    public void test5() throws Exception {
        List<Pos> pp = perform("5", "p.add(3);");
        assertEquals(1, pp.size());
        List<Pos> pp2 = perform("5", "p.add(5);");
        assertEquals(2, pp2.size());
    }

    @Test
    public void testAddBlocks1() throws Exception {
        List<Pos> pp = perform("addblocks", "if (x > 0) x++;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testAddBlocks2() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("addBlocks", "true");
        List<Pos> pp = perform(conf, "addblocks", "if (x > 0) { x++; }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testAddBlocks3() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("addBlocks", "true");
        List<Pos> pp = perform(conf, "addblocks", "if (x > 0) x++;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testConcatenateStrings1() throws Exception {
        List<Pos> pp = perform("concatenatestrings",
                "return \"a\" + \"b\" + \"c\";");
        assertEquals(1, pp.size());
    }

    @Test
    public void testConcatenateStrings2() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("concatenateStrings", "true");
        List<Pos> pp = perform(conf, "concatenatestrings", "return \"abc\";");
        assertEquals(1, pp.size());
        List<Pos> pp2 = perform(conf, "concatenatestrings",
                "return \"a\" + \"b\" + \"c\";");
        assertEquals(1, pp2.size());
    }

    @Test
    public void testConcatenateStrings3() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("concatenateStrings", "true");
        List<Pos> pp = perform(conf, "concatenatestrings",
                "return \"a\" + \"b\" + \"c\";");
        assertEquals(1, pp.size());
    }

    @Test
    public void testConcatenateStrings4() throws Exception {
        List<Pos> pp = perform("concatenatestrings",
                "return x + \"d\" + \"e\";");
        assertEquals(1, pp.size());
    }

    @Test
    public void testConcatenateStrings5() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("concatenateStrings", "true");
        List<Pos> pp = perform(conf, "concatenatestrings", "return x + \"de\";");
        assertEquals(1, pp.size());
    }

    @Test
    public void testConcatenateStrings6() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("concatenateStrings", "true");
        List<Pos> pp = perform(conf, "concatenatestrings",
                "return x + \"d\" + \"e\";");
        assertEquals(1, pp.size());
    }

    @Test
    public void testIgnoreParentheses1() throws Exception {
        List<Pos> pp = perform("ignoreparentheses", "return (x + y);");
        assertEquals(1, pp.size());
    }

    @Test
    public void testIgnoreParentheses2() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("ignoreParentheses", "true");
        List<Pos> pp = perform(conf, "ignoreparentheses", "return x + y;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testIgnoreParentheses3() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("ignoreParentheses", "true");
        List<Pos> pp = perform(conf, "ignoreparentheses", "return (x + y);");
        assertEquals(1, pp.size());
    }

    @Test
    public void testTreatArrayDeclEqual1() throws Exception {
        List<Pos> pp = perform("treatarraydeclequal", "int[] x = {1, 2};");
        assertEquals(1, pp.size());
    }

    @Test
    public void testTreatArrayDeclEqual2() throws Exception {
        List<Pos> pp = perform("treatarraydeclequal", "int x[] = {1, 2};");
        assertEquals(1, pp.size());
    }

    @Test
    public void testTreatArrayDeclEqual3() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("treatArrayDeclEqual", "true");
        List<Pos> pp = perform(conf, "treatarraydeclequal", "int[] x = {1, 2};");
        assertEquals(2, pp.size());
    }

    @Test
    public void testTreatArrayDeclEqual4() throws Exception {
        Properties conf = new Properties();
        conf.setProperty("treatArrayDeclEqual", "true");
        List<Pos> pp = perform(conf, "treatarraydeclequal", "int x[] = {1, 2};");
        assertEquals(2, pp.size());
    }

    @Test
    public void testAssertStmt1() throws Exception {
        List<Pos> pp = perform("assertstmt", "assert x > 0;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testAssertStmt2() throws Exception {
        List<Pos> pp = perform("assertstmt", "assert x > 0 : \"not positive\";");
        assertEquals(1, pp.size());
    }

    @Test
    public void testBlockStmt1() throws Exception {
        List<Pos> pp = perform("blockstmt", "{ System.out.println(\"Easy\"); return 1; }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testBlockStmt2() throws Exception {
        List<Pos> pp = perform("blockstmt", "{ }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testBreakStmt1() throws Exception {
        List<Pos> pp = perform("breakstmt", "break;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testBreakStmt2() throws Exception {
        List<Pos> pp = perform("breakstmt", "break lab;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testContinueStmt() throws Exception {
        List<Pos> pp = perform("continuestmt", "continue;");
        assertEquals(1, pp.size());
        List<Pos> pp2 = perform("continuestmt", "continue lab;");
        assertEquals(1, pp2.size());
    }

    @Test
    public void testDoStmt() throws Exception {
        List<Pos> pp = perform("dostmt", "do { x *= 2; } while (x < y);");
        assertEquals(1, pp.size());
    }

    @Test
    public void testEmptyStmt() throws Exception {
        List<Pos> pp = perform("emptystmt", ";");
        assertEquals(1, pp.size());
    }

    @Test
    public void testExplicitConstructorInvocationStmt() throws Exception {
        List<Pos> pp = perform("explicitconstructorinvocationstmt", "this(1, x, y);");
        assertEquals(1, pp.size());
    }

    @Test
    public void testExplicitConstructorInvocationStmt2() throws Exception {
        List<Pos> pp = perform("explicitconstructorinvocationstmt", "super(x, y, z);");
        assertEquals(1, pp.size());
    }

    @Test
    public void testExpressionStmt1() throws Exception {
        List<Pos> pp = perform("expressionstmt", "scanner.next();");
        assertEquals(1, pp.size());
    }

    @Test
    public void testExpressionStmt2() throws Exception {
        List<Pos> pp = perform("expressionstmt", "x += 31 * obj.hashCode();");
        assertEquals(1, pp.size());
    }

    @Test
    public void testExpressionStmt3() throws Exception {
        List<Pos> pp = perform("expressionstmt", "x = Integer.MAX_VALUE;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testExpressionStmt4() throws Exception {
        List<Pos> pp = perform("expressionstmt", "x = (y == null) ? new Object() : y;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testForEachStmt() throws Exception {
        List<Pos> pp = perform("foreachstmt", "for (String s : ss) { System.out.println(s); }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testForStmt() throws Exception {
        List<Pos> pp = perform("forstmt", "for (int j = 0; j < 10; j++) { System.out.println(j); }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testIfStmt1() throws Exception {
        List<Pos> pp = perform("ifstmt", "if (x >= 0) { return x; }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testIfStmt2() throws Exception {
        List<Pos> pp = perform("ifstmt", "if (x >= 0) { return x; } else { return -x; }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testLabeledStmt() throws Exception {
        List<Pos> pp = perform("labeledstmt", "lab: x++;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testLocalClassDeclarationStmt1() throws Exception {
        List<Pos> pp = perform("localclassdeclarationstmt",
                "class C implements Runnable { @Override public void run() { } }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testLocalClassDeclarationStmt2() throws Exception {
        List<Pos> pp = perform("localclassdeclarationstmt", "class C { }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testReturnStmt1() throws Exception {
        List<Pos> pp = perform("returnstmt", "return -x;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testReturnStmt2() throws Exception {
        List<Pos> pp = perform("returnstmt", "return;");
        assertEquals(1, pp.size());
    }

    @Test
    public void testSwitchStmt() throws Exception {
        List<Pos> pp = perform("switchstmt",
                "switch (x) { case '0': case '1': p = 2; }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testSynchronizedStmt1() throws Exception {
        List<Pos> pp = perform("synchronizedstmt",
                "synchronized (this) { c++; }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testSynchronizedStmt2() throws Exception {
        List<Pos> pp = perform("synchronizedstmt",
                "synchronized (x) { c++; }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testThrowStmt() throws Exception {
        List<Pos> pp = perform("throwstmt",
                "throw new RuntimeException(exception);");
        assertEquals(1, pp.size());
    }

    @Test
    public void testTryStmt1() throws Exception {
        List<Pos> pp = perform("trystmt",
                "try { Thread.sleep(time); } catch (InterruptedException ie) { }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testTryStmt2() throws Exception {
        List<Pos> pp = perform("trystmt",
                "try { Thread.sleep(time); } finally { }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testTryStmt3() throws Exception {
        List<Pos> pp = perform("trystmt",
                "try { Thread.sleep(time); } catch (InterruptedException ie) { } finally { }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testWhileStmt1() throws Exception {
        List<Pos> pp = perform("whilestmt", "while (x > 0) { x /= 10; y++; }");
        assertEquals(1, pp.size());
    }

    @Test
    public void testWhileStmt2() throws Exception {
        List<Pos> pp = perform("whilestmt",
                "while ((line = scanner.nextLine()) != null) { }");
        assertEquals(1, pp.size());
    }
}
