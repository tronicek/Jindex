package foreachstmt;

import java.util.List;

public class ForEachStmt {

    public void print(List<String> names) {
        for (String name : names) {
            System.out.println(name);
        }
    }
    
    public void print(Integer[] primes) {
        for (Integer prime : primes) {
            System.out.println(prime);
        }
    }
}
