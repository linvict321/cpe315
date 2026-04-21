import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

/*In first pass, all it does is looks for label definitions and
introduces them in the symbol table
(a dynamic table which includes the label name and address for each label in the source program).
In the second pass, after the symbol table is complete,
it does the actual assembly by translating the operations into machine codes and so on. */

// and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
public class Assembler{

    public static void main(String args[]) {
        //try reading file
        try (Scanner scan = new Scanner(new File(args[0]))){
            while(scan.hasNextLine()){
            //parse through lines, get rid of white space, comments,
            }
            //opcode table: opcode, machine code, type of opcode, length of opcode
            HashMap<String, Integer> opcode = new HashMap<>();
            opcode.put("ADD", 00110011);
            opcode.put("SUB", 00110011);

            //register table: register name, machine constant
            //no need for $at, $k0, $k1, $gp, $fp.
            HashMap<String, Integer> registerTable = new HashMap<>();
            registerTable.put("$0", 0);
            registerTable.put("$v0", 2);
            registerTable.put("$v1", 3);
            registerTable.put("$a0", 4);
            registerTable.put("$a1", 5);
            registerTable.put("$a2", 6);
            registerTable.put("$a3", 7);
            registerTable.put("$t0", 8);
            registerTable.put("$t1", 9);
            registerTable.put("$t2", 10);
            registerTable.put("$t3", 11);
            registerTable.put("$t4", 12);
            registerTable.put("$t5", 13);
            registerTable.put("$t6", 14);
            registerTable.put("$t7", 15);
            registerTable.put("$s0", 16);
            registerTable.put("$s0", 16);
            registerTable.put("$s0", 16);
            registerTable.put("$s0", 16);
            registerTable.put("$s0", 16);

            //symbol table: symbol num, name, addr, length
            HashMap<String, Integer> symbolTable = new HashMap<>();

            //literal table: literal no, literal, literal address


        }
        catch (Exception e) {

        }

        //pass1

    }

        //pass2


}