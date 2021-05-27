package edu.tarleton.jindex.rename;

import edu.tarleton.jindex.symtab.Entry;
import edu.tarleton.jindex.symtab.SymbolTable;

/**
 * The renaming strategy.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class RenameStrategy {

    private SymbolTable symbolTable = new SymbolTable(true, null);

    public void enterBlock() {
        symbolTable = new SymbolTable(false, symbolTable);
    }

    public void exitBlock() {
        symbolTable = symbolTable.getNext();
    }

    public String declare(String name) {
        return symbolTable.declare(name);
    }

    public String declareVar(String name, String type) {
        return symbolTable.declareVar(name, type);
    }

    public String declareGlobal(String name) {
        return symbolTable.declareGlobal(name);
    }

    public String rename(String name) {
        return symbolTable.lookup(name);
    }

    public Entry lookup(String name) {
        return symbolTable.lookupEntry(name);
    }
}
