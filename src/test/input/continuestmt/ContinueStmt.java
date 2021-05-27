package continuestmt;

import java.util.Scanner;

public class ContinueStmt {

    public int read(Scanner sc) {
        while (true) {
            int m = sc.nextInt();
            if (m == 0) {
                continue;
            }
            return m;
        }
    }
}
