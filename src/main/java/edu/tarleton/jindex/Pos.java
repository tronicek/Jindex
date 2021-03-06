package edu.tarleton.jindex;

import com.github.javaparser.Position;
import java.util.Objects;

/**
 * The representation of a position in the source code.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class Pos {

    private static int count;
    private final String project;
    private final String file;
    private final Position start;
    private final Position end;
    private final Position methodStart;
    private final Position methodEnd;

    public Pos(String project, String file, Position start, Position end,
            Position methodStart, Position methodEnd) {
        this.project = project;
        this.file = file;
        this.start = start;
        this.end = end;
        this.methodStart = methodStart;
        this.methodEnd = methodEnd;
        count++;
    }

    public static int getCount() {
        return count;
    }

    public String getProject() {
        return project;
    }

    public String getFile() {
        return file;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    public Position getMethodStart() {
        return methodStart;
    }

    public Position getMethodEnd() {
        return methodEnd;
    }

    public int getStartLine() {
        return start.line;
    }

    public int getStartColumn() {
        return start.column;
    }

    public int getEndLine() {
        return end.line;
    }

    public int getEndColumn() {
        return end.column;
    }

    public int getMethodStartLine() {
        return methodStart.line;
    }

    public int getMethodStartColumn() {
        return methodStart.column;
    }

    public int getMethodEndLine() {
        return methodEnd.line;
    }

    public int getMethodEndColumn() {
        return methodEnd.column;
    }

    public int getLines() {
        return end.line - start.line + 1;
    }

    public void print() {
        System.out.printf("    %s,%s:%s,%s%n", project, file, start, end);
    }

    public boolean subsetOf(Pos pos) {
        if (!file.equals(pos.file)) {
            return false;
        }
        return notBefore(start, pos.start) && notAfter(end, pos.end);
    }

    private boolean notBefore(Position p, Position p2) {
        return (p.line > p2.line) || (p.line == p2.line && p.column >= p2.column);
    }

    private boolean notAfter(Position p, Position p2) {
        return (p.line < p2.line) || (p.line == p2.line && p.column <= p2.column);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pos) {
            Pos that = (Pos) obj;
            if (start == null || end == null) {
                return false;
            }
            return project.equals(that.project)
                    && file.equals(that.file)
                    && start.equals(that.start)
                    && end.equals(that.end);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, file, start, end);
    }

    @Override
    public String toString() {
        return String.format(
                "%s, %s, start: %s, end: %s, method start: %s, method end: %s",
                project, file, start, end, methodStart, methodEnd);
    }
}
