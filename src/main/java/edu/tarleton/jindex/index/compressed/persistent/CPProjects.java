package edu.tarleton.jindex.index.compressed.persistent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * The class that represents the persistent project names.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class CPProjects {

    private final Map<String, Integer> map;
    private int nextProjectId;

    private CPProjects(Map<String, Integer> map, int nextProjectId) {
        this.map = map;
        this.nextProjectId = nextProjectId;
    }

    public static void initialize(Storage storage) throws IOException {
        File pathFile = storage.getPathFile();
        try (FileChannel ch = FileChannel.open(pathFile.toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ch.truncate(0);
        }
    }

    public static CPProjects load(Storage storage) throws IOException {
        Map<String, Integer> map = new HashMap<>();
        int projectId = 0;
        File projFile = storage.getProjectFile();
        if (!projFile.exists()) {
            return new CPProjects(map, projectId);
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(projFile))) {
            try {
                while (true) {
                    String path = in.readUTF();
                    map.put(path, projectId);
                    projectId++;
                }
            } catch (EOFException e) {
                // okay
            }
        }
        return new CPProjects(map, projectId);
    }

    public void append(Storage storage, String path) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new FileOutputStream(storage.getPathFile(), true))) {
            out.writeUTF(path);
        }
    }

    public int toProjectId(Storage storage, String project) throws IOException {
        Integer pid = map.get(project);
        if (pid != null) {
            return pid;
        }
        append(storage, project);
        map.put(project, nextProjectId);
        nextProjectId++;
        return nextProjectId - 1;
    }

    public Map<Integer, String> getInverseMap() {
        Map<Integer, String> imap = new HashMap<>();
        for (String fn : map.keySet()) {
            Integer projId = map.get(fn);
            imap.put(projId, fn);
        }
        return imap;
    }

    public void print(Storage storage) throws IOException {
        int projId = 0;
        try (DataInputStream in = new DataInputStream(
                new FileInputStream(storage.getProjectFile()))) {
            try {
                while (true) {
                    String project = in.readUTF();
                    System.out.printf("%d %s%n", projId, project);
                    projId++;
                }
            } catch (EOFException e) {
                // okay
            }
        }
    }
}
