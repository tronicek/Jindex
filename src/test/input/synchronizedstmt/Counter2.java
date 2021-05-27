package switchstmt;

public class Counter2 {

    private final Object lock = new Object();
    private int value;

    public void increment() {
        synchronized (lock) {
            value++;
        }
    }

    public void decrement() {
        synchronized (lock) {
            value--;
        }
    }
}
