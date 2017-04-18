import java.util.*;

// This is a the Struct SymbolElem that contains <lexeme, type, address>
class SymbolElem {
  String lexeme;
  String type;
  int address;

  // These return the elements
  public String getLexeme() {return lexeme;}
  public String getType()   {return type;}
  public int getAddress()   {return address;}

  // This sets the elements
  void setTable(String lexeme, String type, int address) {
    this.lexeme  = lexeme;
    this.type    = type;
    this.address = address;
  }
}

// This is our ADT SymbolTable.
// We use an ArrayList of SymbolElems called tableList.
class SymbolTable {
  ArrayList<SymbolElem> tableList = new ArrayList<SymbolElem>();

  // inserts a new SymbolElem into the ArrayList
  void insert(String lexeme, String type, int address) {
    SymbolElem elem = new SymbolElem();
    elem.setTable(lexeme, type, address);
    tableList.add(elem);
  }

  // Basically an unwrap function for get()
  SymbolElem get(int i) {
    return tableList.get(i);
  }

  // Basically an unwrap function for size()
  int size() {
    return tableList.size();
  }

  // Compares the lexeme passed into the function and the lexemes in the ArrayList.
  // Returns the position
  // If it does not return anything then we kill the program and print the offending
  // lexeme.
  int lookup(String lexeme) {
    for (int i = 0; i < this.size(); i++) {
      if (this.get(i).getLexeme().equals(lexeme)) {
        return i;
      }
    }
    System.err.println("unimplemented label: " + lexeme);
    System.exit(-1);
    return -1;
  }
}
