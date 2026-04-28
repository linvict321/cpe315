# Name:  Victoria, Ryo
# Section:  315 -01
# Description:  reverse numbers 
.globl welcome
.globl prompt
.globl sumText
.data

welcome:
	.asciiz " This program reverses a number \n\n"

prompt:
	.asciiz " Enter an integer: "

sumText: 
	.asciiz " \n Result = "

.text

main:

ori $v0, $0, 4
lui $a0, 0x1001
syscall

ori     $v0, $0, 4
lui     $a0, 0x1001
ori     $a0, $a0, 0x23
syscall

ori $v0, $0, 5
syscall

ori $s0, $0, 0
addu $s0, $s0, $v0

ori $s1, $0, 0
ori $t0, $0, 32

loop:

beq $t0, $0, done
sll $s1, $s1, 1
andi $t1, $s0, 1
or $s1, $s1, $t1
srl $s0, $s0, 1
addi $t0, $t0, -1
j loop

done:
ori     $v0, $0, 4
lui     $a0, 0x1001
ori     $a0, $a0, 0x37  
syscall


ori     $v0, $0, 1
addu    $a0, $s1, $0
syscall

ori     $v0, $0, 10
syscall
