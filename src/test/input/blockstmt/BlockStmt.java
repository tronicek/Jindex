package blockstmt;

public class BlockStmt {

    public int fib(int n) {
        if (n == 0 || n == 1) {
            System.out.println("Easy");
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }

    public void print(int a) {
        if (a > 0) {
        } else {
            System.out.println(a);
        }
    }
}
