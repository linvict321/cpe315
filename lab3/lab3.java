import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;

/*  Ryo Sannomiya, Victoria Lin
    CPE 315
*/
/*In first pass, all it does is looks for label definitions and
introduces them in the symbol table
(a dynamic table which includes the label name and address for each label in the source program).
In the second pass, after the symbol table is complete,
it does the actual assembly by translating the operations into machine codes and so on. */

// and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
public class lab3{
    static HashMap<String, Integer> symbolTable = new HashMap<>();
    static HashMap<String, Integer> opcode = new HashMap<>();
    static HashMap<String, Integer> functTable = new HashMap<>();
    static HashMap<String, Integer> registerTable = new HashMap<>();


    static int[] registers = new int[32];
    static int[] dataMem = new int[8192];
    static int pc = 0;

    static ArrayList<Instruction> program = new ArrayList<>();

    static class Instruction {
        String op;
        String[] tokens;

        Instruction(String op, String[] tokens) {
            this.op = op;
            this.tokens = tokens;
        }
    }

    public static void main(String args[]) {

        //opcode table: opcode, machine code, type of opcode, length of opcode
        //and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
        //HashMap<String, Integer> opcode = new HashMap<>();
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

        //only for and, or, add, sll, sub, slt, *jr
        //Map<String, Integer> functTable = new HashMap<>(); //for the function bits
        functTable.put("and", 36);
        functTable.put("or", 37);
        functTable.put("add", 32);
        functTable.put("sll", 0);
        functTable.put("sub", 34);
        functTable.put("slt", 42);
        functTable.put("jr", 8);

        //register table: register name, machine constant
        //no need for $at, $k0, $k1, $gp, $fp.
        //HashMap<String, Integer> registerTable = new HashMap<>();
        registerTable.put("$zero", 0);
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
        //HashMap<String, Integer> symbolTable = new HashMap<>();

        //all lines read from the file input
        ArrayList<String> lines = new ArrayList<>();

        //try reading file
        try (Scanner scan = new Scanner(new File(args[0]))){
            while(scan.hasNextLine()){
                lines.add(scan.nextLine());
            }
        }
        catch (Exception e) {
            System.err.println("Error reading file: " + args[0]);
            System.exit(1);
        }


        pass1(lines); //wait yo don't we wanna pass 1 in the try block?
        pass2(lines);

    }

    static void pass1(ArrayList<String> lines) {
        //tracking current byte
        int address = 0;
        //checks through all the lines found from the scanner
        for (String linen : lines){
            //clean line
            String line = cleanLine(linen);
            //if there is nothing after cleanLine, just move on
            if (line.isEmpty()){
                continue;
            }
            if (line.contains(":")){
                //find the index of which ":" is at
                int colIdx = line.indexOf(":");
                //get everything before the ":" and remove white space
                String label = line.substring(0, colIdx).trim();
                symbolTable.put(label, address);
                //get everything after the ":"
                line = line.substring(colIdx + 1).trim();
            }

            //if there is nothing after doing all, move on
            if (line.isEmpty()){
                continue;
            }
            //increment address by 4
            address += 4;
        }
    }

    static String cleanLine(String line){
        int comIndex = line.indexOf("#");
        if (comIndex != -1){
            line = line.substring(0, comIndex);
        }
        return line.trim();
    }

    //pass 2
    static void pass2(ArrayList<String> lines) {
        //strip and clean lines, skip blanks and label-only lines, going to take from pass1

        int currentAddress = 0;
        for (String linen : lines){
            //clean line
            String line = cleanLine(linen);
            //if there is nothing after cleanLine, just move on
            if (line.isEmpty()){
                continue;
            }
            //skip label-only lines
            if(line.contains(":")){
                //find the index of which ":" is at
                int colIdx = line.indexOf(":");
                //get everything after the ":", if it's nothing then skip the line
                line = line.substring(colIdx + 1).trim();
                if(line.isEmpty()){
                    continue;
                }
            }

            //string of types to sort through in pass 2
            List<String> rtype = Arrays.asList("and", "or", "add", "sub", "slt", "sll");
            List<String> itype = Arrays.asList("addi", "beq", "bne", "lw", "sw");
            List<String> jtype = Arrays.asList("j", "jr", "jal");

            // split line into tokens by commas, whitespace, and parentheses
            // and insert space before '$' so registers don't stick to instructions
            line = line.replaceAll("\\$", " \\$");
            String[] tokens = line.trim().split("[,\\s()]+");
            String instruction = tokens[0];

            //identify instruction type (R, I, or J)
            //find opcode/funct # on opcode table
            //find register number on register table
            //assemble 32-bit binary word

            //6: opcode, 5: rs, 5: rt, 5:rd, 5: shamt, 6: funct
            if(rtype.contains(instruction)){
                int r_opcode = 0; //gets opcode
                int r_rd = registerTable.get(tokens[1]);//destination
                int r_rs = 0;
                int r_rt = 0;
                int r_shamt = 0; //put sll case under
                int r_funct = functTable.get(instruction);//function table finds the operation

                if(instruction.equals("sll")){//just for sll
                    r_rt = registerTable.get(tokens[2]);
                    r_shamt = Integer.parseInt(tokens[3]);
                } else {//everything else
                    r_rs = registerTable.get(tokens[2]);
                    r_rt = registerTable.get(tokens[3]);
                }
                //pads empty spaces in front with 0, makes sure each takes up the right num of spaces, converts int -> binary
                String op  = String.format("%6s",  Integer.toBinaryString(r_opcode)).replace(' ', '0');
                String rs  = String.format("%5s",  Integer.toBinaryString(r_rs)).replace(' ', '0');
                String rt  = String.format("%5s",  Integer.toBinaryString(r_rt)).replace(' ', '0');
                String rd  = String.format("%5s",  Integer.toBinaryString(r_rd)).replace(' ', '0');
                String sha = String.format("%5s",  Integer.toBinaryString(r_shamt)).replace(' ', '0');
                String funct = String.format("%6s",  Integer.toBinaryString(r_funct)).replace(' ', '0');

                System.out.println(op + " " + rs + " " + rt + " " + rd + " " + sha + " " + funct);
            }

            //6: opcode, 5: rs, 5: rt, 16: imm
            else if(itype.contains(instruction)){
                int i_op = 0, i_rs = 0, i_rt = 0, i_imm = 0;
                i_op = opcode.get(instruction);
                //3 cases: addi, branches (bne, beq), lw/sw
                if(instruction.equals("addi")){
                    i_rt = registerTable.get(tokens[1]);
                    i_rs = registerTable.get(tokens[2]);
                    i_imm = Integer.parseInt(tokens[3]);
                } else if(instruction.equals("bne") || instruction.equals("beq")){
                    i_rs = registerTable.get(tokens[1]);
                    i_rt = registerTable.get(tokens[2]);
                    int labelAddr = symbolTable.get(tokens[3]); //find the label on the symbol table
                    i_imm = (labelAddr - (currentAddress + 4)) / 4; // +4 (bc pc + 4) and /4 (bc words are 4 bytes)
                } else if(instruction.equals("lw") || instruction.equals("sw")) {
                    i_rt = registerTable.get(tokens[1]);//destination
                    i_imm = Integer.parseInt(tokens[2]);//offset
                    i_rs = registerTable.get(tokens[3]);//and the base register
                }

                String op  = String.format("%6s",  Integer.toBinaryString(i_op)).replace(' ', '0');
                String rs  = String.format("%5s",  Integer.toBinaryString(i_rs)).replace(' ', '0');
                String rt  = String.format("%5s",  Integer.toBinaryString(i_rt)).replace(' ', '0');
                String imm = String.format("%16s", Integer.toBinaryString(i_imm & 0xFFFF)).replace(' ', '0'); // 0&FFFF gets rid of -1 case to prevent printing of 32 1's

                System.out.println(op + " " + rs + " " + rt + " " + imm);
            }

            //if it is jump/branch, then look for label in symbol table
            //6: opcode, 26: target addr
            //**CHECK THIS BLOCK IF ANY BUGS SHOW UP!!!!!
            else if(jtype.contains(instruction)){
                int j_op = opcode.get(instruction);

                if(instruction.equals("jr")){
                    int j_rs = registerTable.get(tokens[1]);
                    String op    = String.format("%6s", Integer.toBinaryString(0)).replace(' ', '0');
                    String rs    = String.format("%5s", Integer.toBinaryString(j_rs)).replace(' ', '0');
                    String funct   = String.format("%6s", Integer.toBinaryString(8)).replace(' ', '0');
                    System.out.println(op + " " + rs + " " + "00000" + " " + "00000" + " " + "00000" + " " + funct);

                } else if(instruction.equals("j") || instruction.equals("jal")){
                    int labelAddr = symbolTable.get(tokens[1]);
                    int j_target = labelAddr / 4; //label address follows same function as itype
                    String op     = String.format("%6s",  Integer.toBinaryString(j_op)).replace(' ', '0');
                    String target = String.format("%26s", Integer.toBinaryString(j_target)).replace(' ', '0');
                    System.out.println(op + " " + target);
                }
            } else {
                System.out.println("invalid instruction: " + instruction);
                System.exit(1);
            }
            currentAddress += 4;
        }
    }


}