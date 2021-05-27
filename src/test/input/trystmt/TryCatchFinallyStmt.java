package trystmt;

public class TryCatchFinallyStmt {

    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        } finally {
        }
    }

    public void sleep2(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        }
    }
}
