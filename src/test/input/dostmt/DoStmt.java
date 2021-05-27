package dostmt;

public class DoStmt {

    public int pow(int p) {
        int m = 1;
        do {
            m *= 2;
        } while (m < p);
        return p;
    }
}
