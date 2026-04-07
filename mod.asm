  # Name:  Victoria, Ryo
  # Section:  315 -01
  # Description:  mod numbers 


# declare global so programmer can see actual addresses.
.globl welcome
.globl prompt
.globl sumText

#  Data Area (this area contains strings to be displayed during the program)
.data

welcome:
	.asciiz " This program mods two numbers \n\n"

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

	# Clear $s0 for the res
	ori     $s0, $0, 0	

	# Add 1st integer to res 
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
	
	add $t0, $v0, 0
	li $t1, 0
	loop:
		srl $t0, $t0, 1
		addi $t1, $t1, 1
		bnez $t0, loop
													
	syscall
	# $t1 has value of n in 2^n b/c v0 should be power of 2
	
	# mod 2 ints
	srl $s0, $s0, $t1 #shifts s0 by no. of bits in t1, stores result in s1
	#sub $s0, $s0, $s1 #stores remainder(mod number) in s0
	
	# Display the res text
	ori     $v0, $0, 4			
	lui     $a0, 0x1001
	ori     $a0, $a0,0x36
	syscall
	
	# Display the res
	# load 1 into $v0 to display an integer
	ori     $v0, $0, 1			
	add 	$a0, $s0, $0
	syscall
	
	# Exit (load 10 into $v0)
	ori     $v0, $0, 10
	syscall
  
