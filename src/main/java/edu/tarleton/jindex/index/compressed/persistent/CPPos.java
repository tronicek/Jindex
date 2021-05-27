package edu.tarleton.jindex.index.compressed.persistent;

import com.github.javaparser.Position;
import edu.tarleton.jindex.index.MappedFile;
import java.io.IOException;

/**
 * The position representation in the compressed persistent TRIE.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class CPPos {

    public static final int LENGTH = 44;
    private static final Position NO_POSITION = new Position(-1, -1);
    private static long count;
    private final int projectId;
    private final long fileId;
    private final Position begin;
    private final Position end;
    private final Position methodBegin;
    private final Position methodEnd;

    public static long getCount() {
        return count;
    }

    public CPPos(int projectId, long fileId, Position begin, Position end,
            Position methodBegin, Position methodEnd) {
        this.projectId = projectId;
        this.fileId = fileId;
        this.begin = (begin == null) ? NO_POSITION : begin;
        this.end = (end == null) ? NO_POSITION : end;
        this.methodBegin = methodBegin;
        this.methodEnd = methodEnd;
        count++;
    }

    public int getProjectId() {
        return projectId;
    }

    public long getFileId() {
        return fileId;
    }

    public Position getBegin() {
        return begin;
    }

    public Position getEnd() {
        return end;
    }

    public Position getMethodBegin() {
        return methodBegin;
    }

    public Position getMethodEnd() {
        return methodEnd;
    }

    public int getLines() {
        if (begin == NO_POSITION || end == NO_POSITION) {
            return 0;
        }
        return end.line - begin.line + 1;
    }

    public static CPPos readFrom(Storage storage) throws IOException {
        MappedFile posFile = storage.getPosFile();
        int projectId = posFile.readInt();
        long fileId = posFile.readLong();
        int bline = posFile.readInt();
        int bcol = posFile.readInt();
        int eline = posFile.readInt();
        int ecol = posFile.readInt();
        int mbline = posFile.readInt();
        int mbcol = posFile.readInt();
        int meline = posFile.readInt();
        int mecol = posFile.readInt();
        return new CPPos(projectId, fileId,
                position(bline, bcol), position(eline, ecol),
                position(mbline, mbcol), position(meline, mecol));
    }

    private static Position position(int begin, int end) {
        if (begin < 0 && end < 0) {
            return NO_POSITION;
        }
        return new Position(begin, end);
    }

    public void writeTo(Storage storage) throws IOException {
        MappedFile posFile = storage.getPosFile();
        posFile.writeInt(projectId);
        posFile.writeLong(fileId);
        posFile.writeInt(begin.line);
        posFile.writeInt(begin.column);
        posFile.writeInt(end.line);
        posFile.writeInt(end.column);
        posFile.writeInt(methodBegin.line);
        posFile.writeInt(methodBegin.column);
        posFile.writeInt(methodEnd.line);
        posFile.writeInt(methodEnd.column);
    }

    public void print() {
        System.out.printf("      %d,%d:%s,%s:%s,%s%n",
                projectId, fileId, begin, end,
                methodBegin, methodEnd);
    }
}
