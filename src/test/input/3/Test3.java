package test;

import java.util.Random;

public class Test3 {

    private final Random rand = new Random();

    public void nextInt() {
        int r = 1 + rand.nextInt(100);
        System.out.println(r);
    }

    public void nextInt2() {
        int r = 1 + rand.nextInt();
        System.out.println(r);
    }
    
    public void nextInt(int a, int b) {
        int r = a + rand.nextInt(b);
        System.out.println(r);
    }
}
