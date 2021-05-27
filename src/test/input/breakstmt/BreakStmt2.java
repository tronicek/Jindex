package breakstmt;

public class BreakStmt2 {

    public void matrix(int[][] a) {
        outer:
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                if (a[i][j] == 0) {
                    break outer;
                }
                System.out.printf("%d ", a[i][j]);
            }
        }
    }
}
