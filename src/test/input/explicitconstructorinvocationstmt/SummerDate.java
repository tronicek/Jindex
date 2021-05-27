package explicitconstructorinvocationstmt;

public class SummerDate extends Date {

    public SummerDate(int year) {
        super(year);
    }

    public SummerDate(int month, int year) {
        super(month, year);
    }

    public SummerDate(int day, int month, int year) {
        super(day, month, year);
    }
}
