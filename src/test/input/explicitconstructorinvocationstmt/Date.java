package explicitconstructorinvocationstmt;

public class Date {

    public Date(int year) {
        this(1, year);
    }

    public Date(int month, int year) {
        this(1, month, year);
    }

    public Date(int day, int month, int year) {
    }
}
