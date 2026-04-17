# Name:  Victoria, Ryo
# Section:  315 -01
# Description:  

.globl welcome
.globl prompt64High
.globl prompt64Low
.globl promptDivisor
.globl answerHigh
.globl answerLow
.data

welcome:
	.asciiz " This program divides a 64-bit number by a number \n\n"

prompt64High:
	.asciiz " Enter upper 32 bits: "

prompt64Low:
	.asciiz " Enter lower 32 bits: "

promptDivisor:
	.asciiz " Enter divisor: "

answerHigh:
	.asciiz " \n Quotient high = "

answerLow:
	.asciiz " \n Quotient low = "

.text

main:

ori $v0, $0, 4
lui $a0, 0x1001
syscall


ori $v0, $0, 4
lui $a0, 0x1001
ori $a0, $a0, 0x39
syscall


ori $v0, $0, 5
syscall
addu $s0, $v0, $0


ori $v0, $0, 4
lui $a0, 0x1001
ori $a0, $a0, 0x50
syscall


ori $v0, $0, 5
syscall
addu $s1, $v0, $0


ori $v0, $0, 4
lui $a0, 0x1001
ori $a0, $a0, 0x66
syscall


ori $v0, $0, 5
syscall
addu $s2, $v0, $0


ori $t0, $0, 0
addu $t1, $s2, $0

loop:
beq $t1, 1, endshift
srl $t1, $t1, 1
addi $t0, $t0, 1
j loop

endshift:
beq $t0, $0, divideByOne

srlv $s3, $s0, $t0

ori $t2, $0, 32
subu $t2, $t2, $t0

sllv $t3, $s0, $t2
srlv $t4, $s1, $t0
or $s4, $t3, $t4

j printResult

divideByOne:
addu $s3, $s0, $0
addu $s4, $s1, $0

printResult:

ori $v0, $0, 4
lui $a0, 0x1001
ori $a0, $a0, 0x84
syscall

ori $v0, $0, 1
addu $a0, $s3, $0
syscall

ori $v0, $0, 4
lui $a0, 0x1001
ori $a0, $a0, 0x98
syscall

ori $v0, $0, 1
addu $a0, $s4, $0
syscall

ori $v0, $0, 10
syscall
