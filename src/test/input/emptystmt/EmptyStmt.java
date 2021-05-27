package emptystmt;

public class EmptyStmt {

    public void print(int a) {
        if (a > 0) ;
        else {
            a = -a;
        }
        System.out.println(a);
    }
}
