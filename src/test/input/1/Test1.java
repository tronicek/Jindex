package test;

public class Test1 {

    public int min(int a, int b) {
        return Math.min(a, b);
    }

    public int min(int a) {
        return Math.min(a, a);
    }

    public int max(int a, int b) {
        return Math.max(a, b);
    }

    public int min2(int a, int b) {
        return Integer.min(a, b);
    }
}
