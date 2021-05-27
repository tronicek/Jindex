package returnstmt;

public class ReturnStmt {

    public int abs(int a) {
        if (a >= 0) {
            return a;
        }
        return -a;
    }
    
    public void print(String s) {
        if (s == null) {
            return;
        }
        System.out.println(s);
    }
}
