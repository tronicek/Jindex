package trystmt;

public class TryFinallyStmt {

    public void sleep(int millis) throws Exception {
        try {
            Thread.sleep(millis);
        } finally {
        }
    }

    public void sleep2(int millis) throws Exception {
        try {
            Thread.sleep(millis);
        } finally {
            System.out.println();
        }
    }
}
