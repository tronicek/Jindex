package test;

public class Test2 {

    public int min(int a, int b) {
        return java.lang.Math.min(a, b);
    }

    public int min2(int a, int b) {
        return java.util.Math.min(a, b);
    }

    public int min3(int a, int b) {
        return java.lang.Integer.min(a, b);
    }

    public int min(int a) {
        return java.lang.Math.min(a, a);
    }

    public int max(int a, int b) {
        return java.lang.Math.max(a, b);
    }
}
