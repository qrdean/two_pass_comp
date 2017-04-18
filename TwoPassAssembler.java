import java.util.*;
import java.io.*;

/*This is a adaptation of Dr. McGuire's SimpleAsm.java file.
Also included is a file SymbolTable.java that includes the ADT SymbolTable and its functions*/

public class TwoPassAssembler {
  public static final int HALT    = 0;
  public static final int PUSH    = 1;
  public static final int RVALUE  = 2;
  public static final int LVALUE  = 3;
  public static final int POP     = 4;
  public static final int STO     = 5;
  public static final int COPY    = 6;
  public static final int ADD     = 7;
  public static final int SUB     = 8;
  public static final int MPY     = 9;
  public static final int DIV     = 10;
  public static final int MOD     = 11;
  public static final int NEG     = 12;
  public static final int NOT     = 13;
  public static final int OR      = 14;
  public static final int AND     = 15;
  public static final int EQ      = 16;
  public static final int NE      = 17;
  public static final int GT      = 18;
  public static final int GE      = 19;
  public static final int LT      = 20;
  public static final int LE      = 21;
  public static final int LABEL   = 22;
  public static final int GOTO    = 23;
  public static final int GOFALSE = 24;
  public static final int GOTRUE  = 25;
  public static final int PRINT   = 26;
  public static final int READ    = 27;
  public static final int GOSUB   = 28;
  public static final int RET     = 29;

  // opcodes array
  public static String [] opcodes = {
    "HALT",
    "PUSH",
    "RVALUE",
    "LVALUE",
    "POP",
    "STO",
    "COPY",
    "ADD",
    "SUB",
    "MPY",
    "DIV",
    "MOD",
    "NEG",
    "NOT",
    "OR",
    "AND",
    "EQ",
    "NE",
    "GT",
    "GE",
    "LT",
    "LE",
    "LABEL",
    "GOTO",
    "GOFALSE",
    "GOTRUE",
    "PRINT",
    "READ",
    "GOSUB",
    "RET",
  };

  // Driving the bus
  public static void main(String [] args) throws IOException {
    String filename;
    SymbolTable table = new SymbolTable();

    if (args.length != 0) {
      filename = args[0];
    } else {
      filename = "lab1.asm";
    }

    Scanner infile = new Scanner(new File(filename));
    System.out.println("\nA Sacrificial Offering to McGuire\n");
    pass1(infile, table);
    infile.close();
    dumpSymbolTable(table);
    infile = new Scanner(new File(filename));

    pass2(infile, table);
    infile.close();

    System.out.println("\nDone");
  }

  public static int lookUpOpcode(String s) {
    for(int i = 0; i < opcodes.length; i++) {
      if (s.equalsIgnoreCase(opcodes[i])) {
        return i;
      }
    }
    System.err.println("Invalid opcode:" + s);
    return -1;
  }

  // This starts the first pass
  public static void pass1(Scanner infile, SymbolTable table) {
    int locationCounter = 0;
    String line;
    Scanner input;
    String lexeme;
    String type;
    int address;

    do {
      line = infile.nextLine();
      input = new Scanner(line);
    } while (!input.next().equalsIgnoreCase("Section"));

    if (!input.next().equalsIgnoreCase(".data")) {
      System.err.println("Error: Missing 'Section .data' directive");
      System.exit(1);
    } else   {
      System.out.println("Parsing data section, pass 1");
    }

    line = infile.nextLine();
    input = new Scanner(line);

    while(!(lexeme = input.next()).equalsIgnoreCase("Section"))   {
      int pos = lexeme.indexOf(':');
      if (pos > 0) {
        lexeme = lexeme.substring(0,pos);
      } else {
        System.err.println("error parsing" + line);
      }


      table.insert(lexeme,"Int",locationCounter);
      locationCounter++;
      line = infile.nextLine();
      input = new Scanner(line);
    }

    System.out.println("Parsing code section, pass 1\n");
    locationCounter = 0;
    while(infile.hasNext()) {
      line = infile.nextLine();
      input = new Scanner(line);
      lexeme = input.next();

      if (lexeme.equalsIgnoreCase("label")) {
        lexeme = input.next();
        table.insert(lexeme,"Code",locationCounter);
      }
      locationCounter++;
    }
  }

  // This starts the second pass
  public static void pass2(Scanner infile, SymbolTable table) throws IOException {
    int locationCounter = 0;
    String line;
    Scanner input;
    String lexeme;
    int symTabPtr;
    final int NULL = -1;
    FileOutputStream fstream = new FileOutputStream("myCatzOutputz.bin");
    DataOutputStream outputFile = new DataOutputStream(fstream);

    do {
      line = infile.nextLine();
      input = new Scanner(line);
    } while (!input.next().equalsIgnoreCase("Section"));
    if (!input.next().equalsIgnoreCase(".data")) {
      System.err.println("Error: Missing 'Section .data' directive");
      System.exit(1);
    }
    else {
      System.out.println("\nParsing data section, pass 2");
    }
    line = infile.nextLine();
    input = new Scanner(line);

    while(!(lexeme = input.next()).equalsIgnoreCase("Section")) {
      line = infile.nextLine();
      input = new Scanner(line);
    }

    System.out.println("Parsing code section, pass 2\n");
    locationCounter = 0;

    while(infile.hasNext()) {
      line = infile.nextLine();
      input = new Scanner(line);
      lexeme = input.next();
      int ptr;
      int binary;
      int temp;
      int opcode = lookUpOpcode(lexeme);
      switch(opcode)
      {
        case HALT:
          insertCode(locationCounter, HALT);
          binary = HALT<<16;
          outputFile.writeInt(binary);
          break;
        case PUSH:
          lexeme = input.next();
          insertCode(locationCounter, PUSH, Integer.parseInt(lexeme));
          binary = PUSH<<16;
          temp = Integer.parseInt(lexeme);
          binary = binary | temp;
          outputFile.writeInt(binary);
          break;
        case RVALUE:
          lexeme = input.next();
          ptr = table.lookup(lexeme);
          insertCode(locationCounter, RVALUE, table.get(ptr).getAddress());
          binary = RVALUE<<16;
          temp = table.get(ptr).getAddress();
          binary = binary | temp;
          outputFile.writeInt(binary);
          break;
        case LVALUE:
          lexeme = input.next();
          ptr = table.lookup(lexeme);
          insertCode(locationCounter, LVALUE, table.get(ptr).getAddress());
          binary = LVALUE<<16;
          temp = table.get(ptr).getAddress();
          binary = binary | temp;
          outputFile.writeInt(binary);
          break;
        case POP:
          insertCode(locationCounter, POP);
          binary = POP<<16;
          outputFile.writeInt(binary);
          break;
        case STO:
          insertCode(locationCounter, STO);
          binary = STO<<16;
          outputFile.writeInt(binary);
          break;
        case COPY:
          insertCode(locationCounter, COPY);
          binary = COPY<<16;
          outputFile.writeInt(binary);
          break;
        case ADD:
          insertCode(locationCounter, ADD);
          binary = ADD<<16;
          outputFile.writeInt(binary);
          break;
        case SUB:
          insertCode(locationCounter, SUB);
          binary = SUB<<16;
          outputFile.writeInt(binary);
          break;
        case MPY:
          insertCode(locationCounter, MPY);
          binary = MPY<<16;
          outputFile.writeInt(binary);
          break;
        case DIV:
          insertCode(locationCounter, DIV);
          binary = DIV<<16;
          outputFile.writeInt(binary);
          break;
        case MOD:
          insertCode(locationCounter, MOD);
          binary = MOD<<16;
          outputFile.writeInt(binary);
          break;
        case NEG:
          insertCode(locationCounter, NEG);
          binary = NEG<<16;
          outputFile.writeInt(binary);
          break;
        case NOT:
          insertCode(locationCounter, NOT);
          binary = NOT<<16;
          outputFile.writeInt(binary);
          break;
        case OR:
          insertCode(locationCounter, OR);
          binary = OR<<16;
          outputFile.writeInt(binary);
          break;
        case AND:
          insertCode(locationCounter, AND);
          binary = AND<<16;
          outputFile.writeInt(binary);
          break;
        case EQ:
          insertCode(locationCounter, EQ);
          binary = EQ<<16;
          outputFile.writeInt(binary);
          break;
        case NE:
          insertCode(locationCounter, NE);
          binary = NE<<16;
          outputFile.writeInt(binary);
          break;
        case GT:
          insertCode(locationCounter, GT);
          binary = GT<<16;
          outputFile.writeInt(binary);
          break;
        case GE:
          insertCode(locationCounter, GE);
          binary = GE<<16;
          outputFile.writeInt(binary);
          break;
        case LT:
          insertCode(locationCounter, LT);
          binary = LT<<16;
          outputFile.writeInt(binary);
          break;
        case LE:
          insertCode(locationCounter, LE);
          binary = LE<<16;
          outputFile.writeInt(binary);
          break;
        case LABEL:
          lexeme = input.next();
          ptr = table.lookup(lexeme);
          insertCode(locationCounter, LABEL, table.get(ptr).getAddress());
          binary = LABEL<<16;
          temp = table.get(ptr).getAddress();
          binary = binary | temp;
          outputFile.writeInt(binary);
          break;
        case GOTO:
          lexeme = input.next();
          ptr = table.lookup(lexeme);
          insertCode(locationCounter, GOTO, table.get(ptr).getAddress());
          binary = GOTO<<16;
          temp = table.get(ptr).getAddress();
          binary = binary | temp;
          outputFile.writeInt(binary);
          break;
        case GOFALSE:
          lexeme = input.next();
          ptr = table.lookup(lexeme);
          insertCode(locationCounter, GOFALSE, table.get(ptr).getAddress());
          binary = GOFALSE<<16;
          temp = table.get(ptr).getAddress();
          binary = binary | temp;
          outputFile.writeInt(binary);
          break;
        case GOTRUE:
          lexeme = input.next();
          ptr = table.lookup(lexeme);
          insertCode(locationCounter, GOTRUE, table.get(ptr).getAddress());
          binary = GOTRUE<<16;
          temp = table.get(ptr).getAddress();
          binary = binary | temp;
          outputFile.writeInt(binary);
          break;
        case PRINT:
          insertCode(locationCounter, PRINT);
          binary = PRINT<<16;
          outputFile.writeInt(binary);
          break;
        case READ:
          insertCode(locationCounter, READ);
          binary = READ<<16;
          outputFile.writeInt(binary);
          break;
        case GOSUB:
          lexeme = input.next();
          ptr = table.lookup(lexeme);
          insertCode(locationCounter, GOSUB, table.get(ptr).getAddress());
          binary = GOSUB<<16;
          temp = table.get(ptr).getAddress();
          binary = binary | temp;
          outputFile.writeInt(binary);
          break;
        case RET:
          insertCode(locationCounter, RET);
          binary = RET<<16;
          outputFile.writeInt(binary);
          break;
        default:
          System.err.println("unimplemented opcode: " + opcode);
          System.exit(opcode);
      }
      locationCounter++;
    }
  }

  // Prints the opcodes and operands of the .code section
  public static void insertCode(int loc, int opcode, int operand)
  {
    System.out.println(loc + ":\t" + opcode + "\t" + operand);
  }

  // Overload of the above
  public static void insertCode(int loc, int opcode)
  {
    insertCode(loc,opcode,0);
  }

  // Dumps the symbol table.
  public static void dumpSymbolTable(SymbolTable table)
  {
    String lex, type;
    int address;
    System.out.println("lexeme \ttype \taddress");
    System.out.println("-----------------------");
    for(int i=0; i < table.size(); i++)
    {
      lex = table.get(i).getLexeme();
      type = table.get(i).getType();
      address = table.get(i).getAddress();
      System.out.println(lex + "\t" + type + "\t" + address);
    }
  }
}
