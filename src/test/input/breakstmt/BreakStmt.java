package breakstmt;

import java.util.Scanner;

public class BreakStmt {

    public int sum(Scanner sc) {
        int s = 0;
        while (true) {
            int m = sc.nextInt();
            if (m == 0) {
                break;
            }
            s += m;
        }
        return s;
    }
}
