package expressionstmt;

import java.util.Scanner;

public class ExpressionStmt {

    public String read(Scanner sc) {
        sc.next();
        return sc.next();
    }

    public String hash(Object p) {
        int h = 0;
        h += 31 * p.hashCode();
        return Integer.toString(h);
    }

    public int max(int m) {
        if (m < Integer.MAX_VALUE) {
            m = Integer.MAX_VALUE;
        }
        return m - 1;
    }

    public int min(int m) {
        if (m > Integer.MIN_VALUE) {
            m = Integer.MIN_VALUE;
        }
        return m + 1;
    }

    public String convertObject(Integer i) {
        Object obj;
        obj = (i == null) ? new Object() : i;
        return obj.toString();
    }

    public String convertString(Integer i) {
        Object obj;
        obj = (i == null) ? new String() : i;
        return obj.toString();
    }
}
