package assertstmt;

public class AssertStmt {

    void m1(int a) {
        assert a > 0;
    }

    void m2(int a) {
        assert a < 0;
    }

    void m3(int a) {
        assert a > 0 : "not positive";
    }

    void m4(int a) {
        assert a < 0 : "not negative";
    }
}
