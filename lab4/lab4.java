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
/*lab 4:
In the previous lab, you wrote an emulator which executes MIPS instructions.
For this lab, you will add a CPU simulator which will model the flow of instructions through a pipelined processor.
Your program will simulate a 5-stage pipeline similar to the pipeline datapath studied in class.

Your processor should accurately simulate the following pipeline delays:
3 cycle delay for taken conditional branches
1 cycle delay for a use-after-load condition
1 cycle delay for any unconditional jump (j, jal, and jr)
 */

/*lab 3: or this lab, you will write a MIPS emulator which will model the execution of instructions on a MIPS CPU.
This program will work like SPIM in that it will emulate the state of the registers and memory.
 basically: run through all instr in pass 2 and put them into instr class,
 then go through the instr in process commands and executes them if the command given says so*/

/*lab2: In first pass, all it does is looks for label definitions and
introduces them in the symbol table
(a dynamic table which includes the label name and address for each label in the source program).
In the second pass, after the symbol table is complete,
it does the actual assembly by translating the operations into machine codes and so on. */

// and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
public class lab4{
    static HashMap<String, Integer> symbolTable = new HashMap<>();
    static HashMap<String, Integer> opcode = new HashMap<>();
    static HashMap<String, Integer> functTable = new HashMap<>();
    static HashMap<String, Integer> registerTable = new HashMap<>();

    //32 MIPS registers to store the value of every register
    static int[] registers = new int[32];
    //memory for clearing data mem, etc.
    static int[] dataMem = new int[8192];
    //program counter
    static int pc = 0;
    //lab4
    static Instruction IF_ID = null, ID_EX = null, EX_MEM = null, MEM_WB = null;
    static boolean branchTaken = false;

    static int cycles = 0;
    static int instructionsExecuted = 0;
    static int stallCycles = 0;

    static int delay = 0; // branch/jump/load-use delay counter

    static ArrayList<Instruction> program = new ArrayList<>(); //for lab 3

    //lab4 bubble, to stall coode
     static Instruction make_bubble() {
        Instruction bubble = new Instruction();
        bubble.op = "squash";
        return bubble;
    }

    static class Instruction {
        //op name
        String op;
        int rs, rt, rd, imm, target, shamt; //get all fields
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

        if (args.length == 2) {
            runScript(args[1]);
        } else {
            runInteractive();
        }
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
            List<String> valid = Arrays.asList("and", "or", "add", "sub", "slt", "sll", "addi", "beq", "bne", "lw",
                    "sw", "j", "jr", "jal");
            line = line.replaceAll("\\$", " \\$");
            String[] tokens = line.trim().split("[,\\s()]+");
            String instruction = tokens[0];
            if (!valid.contains(instruction)) {
                System.out.println("invalid instruction: " + instruction);
                System.exit(1);
            }

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

                Instruction inst = new Instruction();
                inst.op = instruction;
                inst.rd = r_rd;
                inst.rs = r_rs;
                inst.rt = r_rt;
                inst.shamt = r_shamt;
                program.add(inst);

            }

            //6: opcode, 5: rs, 5: rt, 16: imm
            else if(itype.contains(instruction)){
                int i_op = 0, i_rs = 0, i_rt = 0, i_imm = 0;
                i_op = opcode.get(instruction);
                //3 cases: addi, branches (bne, beq), lw/sw
                Instruction inst = new Instruction();
                inst.op = instruction;
                if(instruction.equals("addi")){
                    i_rt = registerTable.get(tokens[1]);
                    i_rs = registerTable.get(tokens[2]);
                    i_imm = Integer.parseInt(tokens[3]);
                } else if(instruction.equals("bne") || instruction.equals("beq")){
                    i_rs = registerTable.get(tokens[1]);
                    i_rt = registerTable.get(tokens[2]);
                    int labelAddr = symbolTable.get(tokens[3]); //find the label on the symbol table
                    inst.target = labelAddr / 4;
                    i_imm = (labelAddr - (currentAddress + 4)) / 4; // +4 (bc pc + 4) and /4 (bc words are 4 bytes)
                } else if(instruction.equals("lw") || instruction.equals("sw")) {
                    i_rt = registerTable.get(tokens[1]);//destination
                    i_imm = Integer.parseInt(tokens[2]);//offset
                    i_rs = registerTable.get(tokens[3]);//and the base register
                }
                inst.op = instruction;
                inst.rt = i_rt;
                inst.rs = i_rs;
                inst.imm = i_imm;
                program.add(inst);

            }

            //if it is jump/branch, then look for label in symbol table
            //6: opcode, 26: target addr
            else if(jtype.contains(instruction)){
                int j_op = opcode.get(instruction);

                if(instruction.equals("jr")){
                    int j_rs = registerTable.get(tokens[1]);

                    Instruction inst = new Instruction();
                    inst.op = instruction;
                    inst.rs = j_rs;
                    program.add(inst);

                } else if(instruction.equals("j") || instruction.equals("jal")){
                    int labelAddr = symbolTable.get(tokens[1]);
                    int j_target = labelAddr / 4; //label address follows same function as itype

                    Instruction inst = new Instruction();
                    inst.op = instruction;
                    inst.target = labelAddr / 4;
                    program.add(inst);
//
                }
            }

            else {
                System.out.println("invalid instruction: " + instruction);
                System.exit(1);
            }
            currentAddress += 4;
        }
    }

    //run the interactive mode
    static void runInteractive() {
        Scanner input = new Scanner(System.in);
        //promt user until quit
        while (true) {
            System.out.print("mips> ");   // prompt first
            String line = input.nextLine().trim().toLowerCase();  //reading input
            if (!processCommand(line)) {
                break;
            }
        }
    }

    private static void runScript(String arg) { //read the script line by line, process command for each line
        try(Scanner scanner = new Scanner(new File(arg))){
            while(scanner.hasNextLine()){
                String line = scanner.nextLine().trim().toLowerCase();
                System.out.println("mips> " + line);
                if(!processCommand(line)){ //fixed it to while -> if
                    break;
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    /*  h = show help
        d = dump register state
        p = show pipeline registers
        s = single step through the program (i.e. execute 1 instruction and stop)
        s num = step through num clock cycles
        r = run until the program ends and display timing summary
        m num1 num2 = display data memory from location num1 to num2
        c = clear all registers, memory, and the program counter to 0
        q = exit the program
    */
    //process command
    static boolean processCommand(String line) {

        if(line.isEmpty()){
            return true;
        }
        String[] parts = line.split("\\s+");
        String cmd = parts[0];
        //make table w/ all commands, if invalid command then print an error message
        ArrayList<String> commands = new ArrayList<>();
        commands.add("h");
        commands.add("d");
        commands.add("p");
        commands.add("s");
        commands.add("r");
        commands.add("m");
        commands.add("c");
        commands.add("q");
        if(commands.contains(cmd)){
            if(cmd.equals("h")){
                System.out.println();
                System.out.println("h = show help\n" +
                        "d = dump register state\n" +
                        "p = show pipeline registers\n" +
                        "s = single step through the program (i.e. execute 1 instruction and stop)\n" +
                        "s num = step through num instructions of the program\n" +
                        "r = run until the program ends\n" +
                        "m num1 num2 = display data memory from location num1 to num2\n" +
                        "c = clear all registers, memory, and the program counter to 0\n" +
                        "q = exit the program");
                System.out.println();
            }
            else if(cmd.equals("d")){
                System.out.println();
                System.out.println("pc = " + pc);

                // also find format (imma just manually do it)
                System.out.println("$0 = " + registers[0] + "          $v0 = " + registers[2] + "         $v1 = " + registers[3] + "         $a0 = " + registers[4]);
                System.out.println("$a1 = " + registers[5] + "         $a2 = " + registers[6] + "         $a3 = " + registers[7] + "         $t0 = " + registers[8]);
                System.out.println("$t1 = " + registers[9] + "         $t2 = " + registers[10] + "         $t3 = " + registers[11] + "         $t4 = " + registers[12]);
                System.out.println("$t5 = " + registers[13] + "         $t6 = " + registers[14] + "         $t7 = " + registers[15] + "         $s0 = " + registers[16]);
                System.out.println("$s1 = " + registers[17] + "         $s2 = " + registers[18] + "         $s3 = " + registers[19] + "         $s4 = " + registers[20]);
                System.out.println("$s5 = " + registers[21] + "         $s6 = " + registers[22] + "         $s7 = " + registers[23] + "         $t8 = " + registers[24]);
                if(registers[29] == 0){
                    System.out.println("$t9 = " + registers[25] + "         $sp = " + registers[29] + "         $ra = " + registers[31]);
                }
                else{
                    System.out.println("$t9 = " + registers[25] + "         $sp = " + registers[29] + "              $ra = " + registers[31]);
                }
                System.out.println();
            }
            else if(cmd.equals("p")){ //shows pipeline registers
                printPipeline();
            }
            else if(cmd.equals("s")){
                int steps = 1;
                if(parts.length > 1){
                    steps = Integer.parseInt(parts[1]);
                }
                for(int i = 0; i < steps; i++){
                    if(pc >= program.size() && pipelineEmpty()){
                        break;
                    }
                    step();
                }

                //TODO: run script and then display all the values
                printPipeline();
            }
            else if(cmd.equals("r")){ //run the program til it ends (extract the test1script.txt or whichever number it is)
                //TODO: execute program
                while(pc < program.size()) {
                    step();
                }
                cycles++;
                if(instructionsExecuted < 0){
                    return false;
                }
                System.out.println(" ");
                System.out.println("Program complete");
                double cpi = (double)cycles / program.size();
                System.out.printf("CPI = %.3f\tCycles = %d\tInstructions = %d%n", cpi, cycles, program.size());
                System.out.println();
            }
            else if(cmd.equals("m")){ // prints data memory
                int num1 = 0;
                int num2 = 0;
                if(parts.length == 3){
                    num1 = Integer.parseInt(parts[1]);
                    num2 = Integer.parseInt(parts[2]);
                }
                System.out.println();

                for(int i = num1; i <= num2; i++){
                    System.out.println("[" + i + "] = " + dataMem[i]);
                }
                System.out.println();
            }
            else if(cmd.equals("c")){ //clear all regs, memory, pc to 0
                for(int i = 0; i < registers.length; i++){
                    registers[i] = 0;
                }
                for(int i = 0; i < dataMem.length; i++){
                    dataMem[i] = 0;
                }
                IF_ID = null;
                ID_EX = null;
                EX_MEM = null;
                MEM_WB = null;
                pc = 0;
                cycles = 0;
                instructionsExecuted = 0;
                branchTaken = false;

                System.out.println("        Simulator reset");
                System.out.println();
            }
            else if(cmd.equals("q")){ //quit exit program
                return false;
            }
            else{
                System.out.println("Invalid input");
            }
        }
        else {
            System.out.println("Invalid input");
        }
        return true;
    }

    static void executeInstruction(Instruction inst){ //executed instruction for
        int pc_next = pc;
        // and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
        if(inst.op.equals("and")){
            registers[inst.rd] = registers[inst.rs] & registers[inst.rt];
        }
        if(inst.op.equals("or")){
            registers[inst.rd] = registers[inst.rs] | registers[inst.rt];
        }
        if(inst.op.equals("add")){
            registers[inst.rd] = registers[inst.rs] + registers[inst.rt];
        }
        if(inst.op.equals("addi")){
            registers[inst.rt] = registers[inst.rs] + inst.imm;
        }
        if(inst.op.equals("sll")){
            registers[inst.rd] = registers[inst.rt] << inst.shamt;
        }
        if(inst.op.equals("sub")){
            registers[inst.rd] = registers[inst.rs] - registers[inst.rt];
        }
        if(inst.op.equals("slt")){
            registers[inst.rd] = registers[inst.rs] < registers[inst.rt] ? 1: 0; //slt = 1 if rs < rt
        }
        if(inst.op.equals("beq")){
            if(registers[inst.rs] == registers[inst.rt]){
                pc_next = pc + inst.imm;
                branchTaken = true;
            }
        }
        if(inst.op.equals("bne")){
            if(registers[inst.rs] != registers[inst.rt]){
                pc_next = pc + inst.imm;
                branchTaken = true;
            }
        }
        if(inst.op.equals("lw")){ //loads from datamem
            int addr = registers[inst.rs] + inst.imm;
            registers[inst.rt] = dataMem[addr];
        }
        if(inst.op.equals("sw")){ //stores into datamem
            int addr = registers[inst.rs] + inst.imm; //source register + imm
            dataMem[addr] = registers[inst.rt]; //target reg
        }
        if(inst.op.equals("j")){
            pc_next = inst.target;
        }
        if(inst.op.equals("jr")){ //jump to reg
            pc_next = registers[inst.rs]; //source
        }
        if(inst.op.equals("jal")){
            registers[31] = pc + 1;  //return address
            pc_next = inst.target;
        }

        pc = pc_next;
        registers[0] = 0; // $0 = 0
    }

    static String instName(Instruction inst) {
        if(inst == null){
            return "empty";
        }
        return inst.op;
    }

    static void step(){
        if(pipelineEmpty() && pc >= program.size()){
            return;
        }

        //writeback
        if(MEM_WB != null && !MEM_WB.op.equals("squash")){
            instructionsExecuted++;
        }
        //load, can't use the same thing twice must stall (1x)
        boolean stall = false;
        if(ID_EX != null && ID_EX.op.equals("lw") && IF_ID != null){
            if(ID_EX.rt == IF_ID.rs || ID_EX.rt == IF_ID.rt){
                stall = true;
            }
        }
        //stall it with bubble, freeze last 3 cycles
        if(stall){
            MEM_WB = EX_MEM;
            EX_MEM = ID_EX;
            ID_EX = make_stall();

            cycles++;
            return;
        }
        //increment forward, if_id get next instr
        MEM_WB = EX_MEM;
        EX_MEM = ID_EX;
        ID_EX = IF_ID;
        if(pc < program.size()){
            IF_ID = program.get(pc);
            pc++;
        } else{
            IF_ID = null;
        }
        if(ID_EX != null && !ID_EX.op.equals("squash")){
            executeInstruction(ID_EX);
        }
        //jump is one stall (1x)
        if(ID_EX != null && (ID_EX.op.equals("jal") || ID_EX.op.equals("jr") || ID_EX.op.equals("j"))){
            IF_ID = make_bubble();
        }

        //if conditionals 3 stalls (3x)
        //have to clear first 3 (if/id, id/ex, ex/mem
        if(ID_EX != null && (ID_EX.op.equals("bne") || ID_EX.op.equals("beq"))){
            if(branchTaken){
                IF_ID = make_bubble();
                branchTaken = false;
            }
        }
        cycles++;
    }

    //idk if this is right, might be diff from yours
    static boolean pipelineEmpty() {
        return IF_ID == null && ID_EX == null && EX_MEM == null && MEM_WB == null;
    }

    static void printPipeline() {
        System.out.println(" ");
        System.out.println("pc\tif/id\tid/exe\texe/mem\tmem/wb");
        System.out.println(
                pc + "\t" + instName(IF_ID) + "\t" + instName(ID_EX) + "\t" + instName(EX_MEM) + "\t" + instName(MEM_WB)
        );
        System.out.println(" ");
    }

    static Instruction make_stall() {
        Instruction stall = new Instruction();
        stall.op = "stall";
        return stall;
    }

}