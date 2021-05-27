package switchstmt;

public class Automaton {

    private int state;

    public void move(char input) {
        switch (input) {
            case '0':
            case '1':
                state = 2;
        }
    }
}
