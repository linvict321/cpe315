import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/*  Ryo Sannomiya, Victoria Lin
    CPE 315
*/
/*In first pass, all it does is looks for label definitions and
introduces them in the symbol table
(a dynamic table which includes the label name and address for each label in the source program).
In the second pass, after the symbol table is complete,
it does the actual assembly by translating the operations into machine codes and so on. */

// and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
public class lab2{

    public static void main(String args[]) {
        //try reading file
        try (Scanner scan = new Scanner(new File(args[0]))){
            while(scan.hasNextLine()){
                String line = cleanLine(scan.nextLine());
            //parse through lines, get rid of white space, comments,
            }
            //opcode table: opcode, machine code, type of opcode, length of opcode
            //and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
            HashMap<String, Integer> opcode = new HashMap<>();
            opcode.put("and", 0);
            opcode.put("or", 0);
            opcode.put("add", 0);
            opcode.put("addi", 8);
            opcode.put("sll", 0);
            opcode.put("sub", 0);
            opcode.put("slt", 0);
            opcode.put("beq", 4);
            opcode.put("bne", 5);
            opcode.put("lw", 35);
            opcode.put("sw", 43);
            opcode.put("j", 2);
            opcode.put("jr", 0);
            opcode.put("jal", 3);

            //only for and, or, add, sll, sub, slt
            Map<String, Integer> functTable = new HashMap<>(); //for the function bits
            functTable.put("and", 36);
            functTable.put("or", 37);
            functTable.put("add", 32);
            functTable.put("sll", 0);
            functTable.put("slt", 42);


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
            registerTable.put("$s1", 17);
            registerTable.put("$s2", 18);
            registerTable.put("$s3", 19);
            registerTable.put("$s4", 20);
            registerTable.put("$s5", 21);
            registerTable.put("$s6", 22);
            registerTable.put("$s7", 23);
            registerTable.put("$t8", 24);
            registerTable.put("$t9", 25);
            registerTable.put("$sp", 29);
            registerTable.put("$ra", 31);

            //symbol table: symbol num, name, addr, length
            HashMap<String, Integer> symbolTable = new HashMap<>();



        }
        catch (Exception e) {
            System.err.println("Error reading file: " + args[0]);
            System.exit(1);
        }

        pass1();
        pass2();

    }

    static String cleanLine(String line){
        int comIndex = line.indexOf("#");
        if (comIndex != -1){
            line = line.substring(0, comIndex);
        }
        return line.trim();
    }

        //pass2


}
