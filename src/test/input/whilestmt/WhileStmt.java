package whilestmt;

import java.util.Scanner;

public class WhileStmt {

    public int log10(int m) {
        int c = 0;
        while (m > 0) {
            m /= 10;
            c++;
        }
        return c;
    }

    public void read(Scanner sc) {
        String s;
        while ((s = sc.nextLine()) != null) {
        }
        System.out.println(s);
    }
}
