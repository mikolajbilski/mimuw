global mdiv

; mdiv(uint_64* x, size_t n, int_64 y) - a function that divides value from array x by y.

; rdi - pointer to array x, dividend, also put result here
; rsi - n, size of table x
; rdx - y, divisor
; rax - put return value(remainder) here

; "INT_MIN" refers to the lowest possible value of x, i.e. -(2^(64*n))
; we use esi instead of rsi as we can safely assume that array x is less than 8GB in size

mdiv:
    mov  r11,rdx                       ; store y in r11 
    mov  r10,rdx                       ; remember the sign of y
    mov  r9,[rdi+8*rsi-8]              ; check the sign of x
    test r9,r9                         ; if x is negative
    js   negate                        ; negate it before division

overflow_test:                         ; checks for the "INT_MIN"/-1 case
    mov  al,[rdi+8*rsi-1]              ; get the sign of x after normalization
    test al, al                        ; if it is still negative, it means x = "INT_MIN"
    jns  preloop                       ; if not, we are fine and can proceed to the division
    cmp  rdx,-1                        ; check if y = -1
    jne  preloop                       ; if it does, we need to interrupt
    div  ecx                           ; we know that ecx = 0 as we are after negation

preloop:                               ; prepare for the division loop
    mov  ecx,esi                       ; set loop counter to n
    xor  edx,edx                       ; set current remainder to 0
    test r11,r11                       ; if y is not negative
    jns  division_loop                 ; we can start division
    neg  r11                           ; otherwise, negate it

division_loop:                         ; do the actual division
    mov  rax,[rdi+8*rcx-8]             ; move the next segment to rax
    div  r11                           ; after that remainder is in rdx, where we want it to be
    mov  [rdi+8*rcx-8],rax             ; move the result back to x
    loop division_loop
                                       ; past the division loop
    xor  r10,r9                        ; check if x and y have different signs
    jns  end                           ; if they do, negate the result

negate:                                ; negate the contents of array x
    xor  eax,eax                       ; i, index of the current element of the array
    mov  ecx,esi                       ; set the loop counter to n
    stc                                ; set CF so we can add 1 in the first loop iteration
                    
negation_loop:                         ; negate it chunk-by-chunk
    NOT  qword[rdi+8*rax]              ; flip all the bits in x[i]
    adc  qword[rdi+8*rax],0            ; add possible carry from previous iterations
    inc  eax                           ; ++i
    loop negation_loop                 

    cmp  r11,rdx                       ; we need to know where to go back
    je   overflow_test                 ; if r11 and rdx are equal we are before the division as remainder != divisor

end:                                   ; put the results where they need to be
    mov  rax,rdx                       ; put the remainder to rax
    test r9,r9                         ; if x wasn't negative
    jns  return                        ; we are done
    neg  rax                           ; otherwise negate the remainder

return:
    ret