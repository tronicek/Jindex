package forstmt;

public class ForStmt {

    public void print() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
        }
    }

    public void print2() {
        for (int i = 0; i < 10; i++) {
            System.out.print(i);
        }
    }

    public void print3() {
        for (int i = 0; i <= 10; i++) {
            System.out.println(i);
        }
    }

    public void print4() {
        for (int i = 0; i < 10; ++i) {
            System.out.println(i);
        }
    }
}
