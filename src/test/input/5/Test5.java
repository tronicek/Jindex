package test;

import java.util.ArrayList;
import java.util.List;

public class Test5 {

    List<Integer> primes = new ArrayList<>();

    public void add() {
        primes.add(2);
        primes.add(3);
        primes.add(5);
        primes.add(5);
    }
    
    public void remove() {
        primes.remove(2);
        primes.remove(3);
        primes.remove(5);
    }
}
