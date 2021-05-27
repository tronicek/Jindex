package throwstmt;

import java.io.DataInputStream;
import java.io.IOException;

public class ThrowStmt {

    public int readInt(DataInputStream in) {
        try {
            return in.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long readLong(DataInputStream in) {
        try {
            return in.readLong();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
