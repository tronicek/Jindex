package edu.tarleton.jindex.index.plain.persistent;

import edu.tarleton.jindex.Pos;
import edu.tarleton.jindex.index.PIndex;
import edu.tarleton.jindex.index.plain.Trie;
import edu.tarleton.jindex.index.plain.TrieEdge;
import edu.tarleton.jindex.index.plain.TrieNode;
import java.io.IOException;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * The class that represents the plain (not compressed) persistent TRIE.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class PTrie implements AutoCloseable, PIndex {

    private final Mode mode;
    private final Storage storage;
    private final PProjects projects;
    private final PFilePaths filePaths;
    private final PLabels labels;
    private final PNextStmtMap nextStmtMap;

    public enum Mode {
        READ_WRITE, READ_ONLY
    }

    private PTrie(Mode mode, Storage storage) throws IOException {
        this.mode = mode;
        this.storage = storage;
        projects = PProjects.load(storage);
        filePaths = PFilePaths.load(storage);
        labels = PLabels.load(storage);
        nextStmtMap = PNextStmtMap.load(storage);
    }

    public static PTrie initialize(String nodeFileName, int nodeFilePageSize,
            String edgeFileName, int edgeFilePageSize,
            String posFileName, int posFilePageSize,
            String projectFileName, String pathFileName,
            String labelFileName, String nextStmtMapFileName) throws IOException {
        Storage st = Storage.initialize(nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                projectFileName, pathFileName,
                labelFileName, nextStmtMapFileName);
        PNode.reset();
        PEdgeBlock.reset();
        PPosBlock.reset();
        PNode root = new PNode();
        root.writeTo(st);
        PProjects.initialize(st);
        PFilePaths.initialize(st);
        PLabels.initialize(st);
        PNextStmtMap.initialize(st);
        return new PTrie(Mode.READ_WRITE, st);
    }

    public static PTrie fromFiles(Mode mode, String nodeFileName, int nodeFilePageSize,
            String edgeFileName, int edgeFilePageSize,
            String posFileName, int posFilePageSize,
            String projectFileName,
            String pathFileName, String labelFileName,
            String nextStmtMapFileName) throws IOException {
        MapMode mapMode = (mode == Mode.READ_WRITE) ? MapMode.READ_WRITE : MapMode.READ_ONLY;
        Storage st = Storage.open(mapMode, nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                projectFileName, pathFileName,
                labelFileName, nextStmtMapFileName);
        return new PTrie(mode, st);
    }

    @Override
    public void close() throws Exception {
        if (mode == Mode.READ_WRITE) {
            labels.store(storage);
            if (nextStmtMap != null) {
                nextStmtMap.store(storage);
            }
        }
        storage.close();
    }

    public void addTrie(Trie trie) throws IOException {
        TrieNode root = trie.getRoot();
        root.setPersistentId(0L);
        Deque<TrieNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TrieNode n = queue.remove();
            PNode p = new PNode(n.getPersistentId());
            p.readFrom(storage);
            PEdgeBlock eb = p.getEdgeBlock();
            for (TrieEdge e : n.getEdges()) {
                short labelId = (short) labels.toLabelId(e.getLabel());
                PEdge pe = eb.findEdge(labelId);
                if (pe == null) {
                    PNode dest = new PNode();
                    dest.writeTo(storage);
                    pe = new PEdge(labelId, dest.getId());
                    eb.addEdge(pe);
                }
                for (Pos pos : e.getPositions()) {
                    int projId = projects.toProjectId(storage, pos.getProject());
                    long fileId = filePaths.toFileId(storage, pos.getFile());
                    PPos pp = new PPos(projId, fileId, pos.getStart(), pos.getEnd(),
                            pos.getMethodStart(), pos.getMethodEnd());
                    pe.addPos(pp);
                }
                TrieNode nn = e.getDestination();
                nn.setPersistentId(pe.getDestId());
                queue.add(nn);
            }
            p.writeTo(storage);
        }
        if (nextStmtMap != null) {
            nextStmtMap.addNextStmtMap(storage, trie.getNextStmtMap());
        }
    }

    @Override
    public void print() throws IOException {
        PNode n = new PNode(0L);
        n.readFrom(storage);
        Deque<PNode> queue = new ArrayDeque<>();
        queue.add(n);
        while (!queue.isEmpty()) {
            PNode p = queue.remove();
            p.print(labels);
            PEdgeBlock eb = p.getEdgeBlock();
            while (eb != null) {
                PEdge[] ee = eb.getEdges();
                for (int i = 0; i < eb.getEdgeCount(); i++) {
                    PEdge e = ee[i];
                    PNode q = new PNode(e.getDestId());
                    q.readFrom(storage);
                    queue.add(q);
                }
                eb = eb.getNext();
            }
        }
        System.out.println("---------------------");
        filePaths.print(storage);
        System.out.println("---------------------");
        if (nextStmtMap != null) {
            nextStmtMap.print(storage);
            System.out.println("---------------------");
        }
    }

    @Override
    public List<Pos> find(List<String> tokens) throws Exception {
        List<Pos> found = new ArrayList<>();
        PNode p = new PNode(0L);
        p.readFrom(storage);
        PEdge edge = null;
        for (String token : tokens) {
            short labId = (short) labels.toExistingLabelId(token);
            edge = p.getEdgeBlock().findEdge(labId);
            if (edge == null) {
                return found;
            }
            long destId = edge.getDestId();
            p = new PNode(destId);
            p.readFrom(storage);
        }
        if (edge == null) {
            return found;
        }
        PPosBlock pb = edge.getPosBlock();
        Map<Integer, String> pmap = projects.getInverseMap();
        Map<Long, String> fmap = filePaths.getInverseMap();
        for (PPos pp : pb.getPositions()) {
            String project = pmap.get(pp.getProjectId());
            String file = fmap.get(pp.getFileId());
            Pos pos = new Pos(project, file,
                    pp.getBegin(), pp.getEnd(),
                    pp.getMethodBegin(), pp.getMethodEnd());
            found.add(pos);
        }
        return found;
    }
}
