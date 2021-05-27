package edu.tarleton.jindex.index.compressed.persistent;

import edu.tarleton.jindex.Pos;
import edu.tarleton.jindex.index.PIndex;
import edu.tarleton.jindex.index.compressed.CTrie;
import edu.tarleton.jindex.index.compressed.CTrieEdge;
import edu.tarleton.jindex.index.compressed.CTrieNode;
import java.io.IOException;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * The representation of the compressed persistent TRIE.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class CPTrie implements AutoCloseable, PIndex {

    private final Mode mode;
    private final Storage storage;
    private final CPProjects projects;
    private final CPFilePaths filePaths;
    private final CPLinearizations linearizations;
    private final CPNextStmtMap nextStmtMap;

    public enum Mode {
        READ_WRITE, READ_ONLY
    }

    private CPTrie(Mode mode, Storage storage) throws IOException {
        this.mode = mode;
        this.storage = storage;
        projects = CPProjects.load(storage);
        filePaths = CPFilePaths.load(storage);
        linearizations = CPLinearizations.load(storage);
        nextStmtMap = CPNextStmtMap.load(storage);
    }

    public static CPTrie initialize(String nodeFileName, int nodeFilePageSize,
            String edgeFileName, int edgeFilePageSize,
            String posFileName, int posFilePageSize,
            String projectFileName, String pathFileName,
            String labelFileName, String linearizationFileName,
            String nextStmtMapFileName) throws IOException {
        Storage st = Storage.initialize(nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                projectFileName, pathFileName,
                labelFileName, linearizationFileName,
                nextStmtMapFileName);
        CPNode.reset();
        CPEdgeBlock.reset();
        CPPosBlock.reset();
        CPNode root = new CPNode();
        root.writeTo(st);
        CPProjects.initialize(st);
        CPFilePaths.initialize(st);
        CPLinearizations.initialize(st);
        return new CPTrie(Mode.READ_WRITE, st);
    }

    public static CPTrie fromFiles(Mode mode, String nodeFileName, int nodeFilePageSize,
            String edgeFileName, int edgeFilePageSize,
            String posFileName, int posFilePageSize,
            String projectFileName, String pathFileName,
            String labelFileName, String linearizationFileName,
            String nextStmtMapFileName) throws IOException {
        MapMode mapMode = (mode == Mode.READ_WRITE) ? MapMode.READ_WRITE : MapMode.READ_ONLY;
        Storage st = Storage.open(mapMode, nodeFileName, nodeFilePageSize,
                edgeFileName, edgeFilePageSize,
                posFileName, posFilePageSize,
                projectFileName, pathFileName, labelFileName,
                linearizationFileName, nextStmtMapFileName);
        return new CPTrie(mode, st);
    }

    @Override
    public void close() throws Exception {
        if (mode == Mode.READ_WRITE) {
            linearizations.store(storage);
            if (nextStmtMap != null) {
                nextStmtMap.store(storage);
            }
        }
        storage.close();
    }

    public void addTrie(CTrie trie) throws IOException {
        int shift = linearizations.getBufferSize();
        linearizations.extendBuffer(trie.getBuffer());
        CTrieNode root = trie.getRoot();
        Deque<CTrieNode> queue = new ArrayDeque<>();
        queue.add(root);
        Deque<Long> queue2 = new ArrayDeque<>();
        queue2.add(0L);
        while (!queue.isEmpty()) {
            CTrieNode n = queue.remove();
            Long nodeId = queue2.remove();
            CPNode p = new CPNode(nodeId);
            p.readFrom(storage);
            CPEdgeBlock eb = p.getEdgeBlock();
            for (CTrieEdge e : n.getEdges()) {
                List<Integer> plin = linearizations.getBuffer();
                Integer first = plin.get(e.getStart() + shift);
                CPEdge pe = eb.findEdge(first, linearizations.getBuffer());
                if (pe == null) {
                    CPNode dest = new CPNode();
                    dest.writeTo(storage);
                    pe = new CPEdge(e.getStart() + shift, e.getEnd() + shift, dest.getId());
                    eb.addEdge(pe);
                    queue.add(e.getDestination());
                    queue2.add(dest.getId());
                } else {
                    List<Integer> curLab = linearizations.getBuffer(pe.getStart(), pe.getEnd());
                    List<Integer> newLab = plin.subList(e.getStart() + shift, e.getEnd() + shift);
                    int pref = commonPrefix(curLab, newLab);
                    CPNode dest;
                    if (pref == curLab.size()) {
                        dest = new CPNode(pe.getDestId());
                        dest.readFrom(storage);
                    } else {
                        CPEdge pe2 = pe.makeClone();
                        pe2.setStart(pe.getStart() + pref);
                        dest = new CPNode();
                        CPEdgeBlock eb2 = dest.getEdgeBlock();
                        eb2.addEdge(pe2);
                        pe.setEnd(pe.getStart() + pref);
                        pe.setDestId(dest.getId());
                        dest.writeTo(storage);
                    }
                    CTrieNode temp;
                    if (pref == newLab.size()) {
                        temp = e.getDestination();
                    } else {
                        temp = new CTrieNode();
                        CTrieEdge tempEdge = e.makeClone();
                        tempEdge.setStart(e.getStart() + pref);
                        temp.addEdge(tempEdge);
                    }
                    queue.add(temp);
                    queue2.add(dest.getId());
                }
                for (Pos pos : e.getPositions()) {
                    int projId = projects.toProjectId(storage, pos.getProject());
                    long fileId = filePaths.toFileId(storage, pos.getFile());
                    CPPos pp = new CPPos(projId, fileId, pos.getStart(), pos.getEnd(),
                            pos.getMethodStart(), pos.getMethodEnd());
                    pe.addPos(pp);
                }
                p.writeTo(storage);
            }
        }
        if (nextStmtMap != null) {
            nextStmtMap.addNextStmtMap(storage, trie.getNextStmtMap());
        }
    }

    private int commonPrefix(List<Integer> lab1, List<Integer> lab2) {
        int n = Math.min(lab1.size(), lab2.size());
        int i = 0;
        while (i < n) {
            Integer a = lab1.get(i);
            Integer b = lab2.get(i);
            if (!a.equals(b)) {
                return i;
            }
            i++;
        }
        return i;
    }

    @Override
    public void print() throws IOException {
        CPNode n = new CPNode(0L);
        n.readFrom(storage);
        Deque<CPNode> queue = new ArrayDeque<>();
        queue.add(n);
        while (!queue.isEmpty()) {
            CPNode p = queue.remove();
            p.print(linearizations);
            CPEdgeBlock eb = p.getEdgeBlock();
            while (eb != null) {
                CPEdge[] ee = eb.getEdges();
                for (int i = 0; i < eb.getEdgeCount(); i++) {
                    CPEdge e = ee[i];
                    CPNode q = new CPNode(e.getDestId());
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
        CPNode p = new CPNode(0L);
        p.readFrom(storage);
        CPEdge edge = null;
        List<Integer> plin = linearizations.getBuffer();
        int i = 0;
        while (i < tokens.size()) {
            String token = tokens.get(i);
            Integer first = linearizations.findLabel(token);
            edge = p.getEdgeBlock().findEdge(first, plin);
            if (edge == null) {
                return found;
            }
            int j = edge.getStart();
            while (i < tokens.size() && j < edge.getEnd()) {
                token = tokens.get(i);
                Integer tokId = linearizations.findLabel(token);
                Integer bufTokId = linearizations.getBufferAt(j);
                if (!tokId.equals(bufTokId)) {
                    return found;
                }
                i++;
                j++;
            }
            if (j < edge.getEnd()) {
                return found;
            }
            long destId = edge.getDestId();
            p = new CPNode(destId);
            p.readFrom(storage);
        }
        if (edge == null) {
            return found;
        }
        CPPosBlock pb = edge.getPosBlock();
        Map<Integer, String> pmap = projects.getInverseMap();
        Map<Long, String> fmap = filePaths.getInverseMap();
        for (CPPos pp : pb.getPositions()) {
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
