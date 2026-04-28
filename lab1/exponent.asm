# Name:  Victoria, Ryo
# Section:  315 -01
# Description:  exponent (x raised to the y)
 
#   CPE 315

#java code: for x^y
#int res = x
#for(int i = 0; i < y; i++){
#	int accum = 0;
#	int addcount = x;
#	while(addcounter > 0){
#		accum += res;
#		addCounter -= 1;
#	}
#	result = accum
#}


# declare global so programmer can see actual addresses.
.globl welcome
.globl prompt
.globl sumText

#  Data Area (this area contains strings to be displayed during the program)
.data

welcome:
	.asciiz " This program exps two numbers \n\n"

prompt:
	.asciiz " Enter an integer: "

sumText: 
	.asciiz " \n Result = "

#Text Area (i.e. instructions)
.text

main:

	# Display the welcome message (load 4 into $v0 to display)
	ori     $v0, $0, 4			

	# This generates the starting address for the welcome message.
	# (assumes the register first contains 0).
	lui     $a0, 0x1001
	syscall

	# Display prompt
	ori     $v0, $0, 4			
	
	# This is the starting address of the prompt (notice the
	# different address from the welcome message)
	lui     $a0, 0x1001
	ori     $a0, $a0,0x22
	syscall

	# Read 1st integer from the user (5 is loaded into $v0, then a syscall)
	ori     $v0, $0, 5
	syscall

	# Clear $s0 for the sum
	ori     $s0, $0, 0	

	# Add 1st integer to sum 
	# (could have put 1st integer into $s0 and skipped clearing it above)
	addu    $s0, $v0, $s0
	
	# Display prompt (4 is loaded into $v0 to display)
	# 0x22 is hexidecimal for 34 decimal (the length of the previous welcome message)
	ori     $v0, $0, 4			
	lui     $a0, 0x1001
	ori     $a0, $a0,0x22
	syscall

	# Read 2nd integer 
	ori	$v0, $0, 5	
						
	syscall
	# $v0 now has the value of the second integer
	
	#exponent x^y
	addi $t0, $s0, 0 #first int = x
	addi $t1, $v0, 0 #second int = y
	
	addi $t2, $s0, 0 #result = x (first int)
	addi $t3, $t1, -1 #exp counter = y (fake multiply this many times)
	#fake multiply for the number of times of the 2nd int
	loop:
		beqz $t3, base_case #if exp counter = 0, go to base case (done calculating)
		
		addi $t4, $0, 0 #accumulator = 0 (contains all added numbers in all rounds)
		addi $t5, $t0, 0 #addcounter = x
		#if not base_case, then add to itself
		mult_loop:  
			beqz $t5, add_done #if addcounter > 0
			add $t4, $t4, $t2 #accumulator += result
			sub $t5, $t5, 1 #addcounter -=1
			j mult_loop #loop again
		add_done:
			addi $t2, $t4, 0 #result += accumulator
			sub $t3, $t3, 1 #exp counter -=1 	
			j loop	#loop again
	
	base_case: #when exp counter = 0
		addi $v0, $t2, 0
	
	# Add 2nd integer to sum
	#addu    $s0, $v0, $s0 

	# Display the sum text
	ori     $v0, $0, 4			
	lui     $a0, 0x1001
	ori     $a0, $a0,0x36
	syscall
	
	# Display the sum
	# load 1 into $v0 to display an integer
	ori     $v0, $0, 1			
	add 	$a0, $t2, $0
	syscall
	
	# Exit (load 10 into $v0)
	ori     $v0, $0, 10
	syscall
