package edu.tarleton.jindex.index.compressed.persistent;

import edu.tarleton.jindex.index.MappedFile;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * The class that represents the persistent storage for the compressed
 * persistent TRIE.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class Storage {

    private final MappedFile nodeFile;
    private final MappedFile edgeFile;
    private final MappedFile posFile;
    private final File projectFile;
    private final File pathFile;
    private final File labelFile;
    private final File linearizationFile;
    private final File nextStmtMapFile;

    private Storage(MappedFile nodeFile, MappedFile edgeFile, MappedFile posFile,
            File projectFile, File pathFile, File labelFile,
            File linearizationFile, File nextStmtMapFile) throws IOException {
        this.nodeFile = nodeFile;
        this.edgeFile = edgeFile;
        this.posFile = posFile;
        this.projectFile = projectFile;
        this.pathFile = pathFile;
        this.labelFile = labelFile;
        this.linearizationFile = linearizationFile;
        this.nextStmtMapFile = nextStmtMapFile;
    }

    public static Storage initialize(String nodeFileName, int nodeFilePageSize,
            String edgeFileName, int edgeFilePageSize,
            String posFileName, int posFilePageSize,
            String projectFileName, String pathFileName,
            String labelFileName, String linearizationFileName,
            String nextStmtMapFileName) throws IOException {
        MappedFile nodeFile = MappedFile.initialize(nodeFileName, FileChannel.MapMode.READ_WRITE, nodeFilePageSize);
        MappedFile edgeFile = MappedFile.initialize(edgeFileName, FileChannel.MapMode.READ_WRITE, edgeFilePageSize);
        MappedFile posFile = MappedFile.initialize(posFileName, FileChannel.MapMode.READ_WRITE, posFilePageSize);
        File projectFile = new File(projectFileName);
        File pathFile = new File(pathFileName);
        File labelFile = new File(labelFileName);
        File linearizationFile = new File(linearizationFileName);
        File nextStmtMapFile = (nextStmtMapFileName == null) ? null : new File(nextStmtMapFileName);
        return new Storage(nodeFile, edgeFile, posFile, projectFile,
                pathFile, labelFile, linearizationFile, nextStmtMapFile);
    }

    public static Storage open(MapMode mapMode,
            String nodeFileName, int nodeFilePageSize,
            String edgeFileName, int edgeFilePageSize,
            String posFileName, int posFilePageSize,
            String projectFileName, String pathFileName,
            String labelFileName, String linearizationFileName,
            String nextStmtMapFileName) throws IOException {
        MappedFile nodeFile = MappedFile.open(nodeFileName, mapMode, nodeFilePageSize);
        MappedFile edgeFile = MappedFile.open(edgeFileName, mapMode, edgeFilePageSize);
        MappedFile posFile = MappedFile.open(posFileName, mapMode, posFilePageSize);
        File projectFile = new File(projectFileName);
        File pathFile = new File(pathFileName);
        File labelFile = new File(labelFileName);
        File linearizationFile = new File(linearizationFileName);
        File nextStmtFile = (nextStmtMapFileName == null) ? null : new File(nextStmtMapFileName);
        return new Storage(nodeFile, edgeFile, posFile, projectFile,
                pathFile, labelFile, linearizationFile, nextStmtFile);
    }

    public void close() throws IOException {
        nodeFile.close();
        edgeFile.close();
        posFile.close();
    }

    public MappedFile getNodeFile() {
        return nodeFile;
    }

    public MappedFile getEdgeFile() {
        return edgeFile;
    }

    public MappedFile getPosFile() {
        return posFile;
    }

    public File getProjectFile() {
        return projectFile;
    }

    public File getPathFile() {
        return pathFile;
    }

    public File getLabelFile() {
        return labelFile;
    }

    public File getLinearizationFile() {
        return linearizationFile;
    }

    public File getNextStmtMapFile() {
        return nextStmtMapFile;
    }
}
