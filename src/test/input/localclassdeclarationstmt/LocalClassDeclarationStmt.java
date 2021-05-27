package localclassdeclarationstmt;

public class LocalClassDeclarationStmt {

    public static void main(String[] args) {
        class MyRun implements Runnable {

            @Override
            public void run() {
            }
        }
        new MyRun().run();
        class Empty {
        }
        System.out.println(new Empty());
    }
}
