package labeledstmt;

public class LabeledStmt {

    public void m(int a) {
        if (a > 0) {
            weird:
            a++;
        }
        System.out.println(a);
    }

    public void m2(int a) {
        if (a > 0) {
            weird:
            a--;
        }
        System.out.println(a);
    }
}
