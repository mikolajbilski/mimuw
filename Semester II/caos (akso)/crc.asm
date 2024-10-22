; crc.asm - computes CRC (max CRC-64) checksum of a given file
; usage: ./crc [file] [crc_poly], where:
; [file] is path to the file for which CRC is to be calculated
; [crc_poly] is the CRC polynomial without the most significant term

global _start

; constants for syscalls
SYS_READ  equ 0
SYS_WRITE equ 1
SYS_OPEN  equ 2
SYS_CLOSE equ 3
SYS_LSEEK equ 8
SYS_EXIT  equ 60

STDOUT    equ 1
O_RDONLY  equ 0
SEEK_CUR  equ 1

section .bss

ret_val:            resb 1
file_descriptor:    resq 1
crc_poly:           resq 1
crc_table:          resq 256
crc_len:            resb 1
crc_val:            resq 1
file_buffer:        resb 65536 ; enough for longest possible segment
buffer_data_size:   resq 1
segment_size:       resq 1
segment_shift:      resq 1
read_finished:      resb 1

section .text

_start:
    ; read and check parameters

    ; check if there are exactly 3 (including program name)
    cmp     qword[rsp], 0x3
    jne     .error
    
    ; try to open the file
    mov     rax, SYS_OPEN
    mov     rdi, [rsp + 16]
    mov     rsi, O_RDONLY
    syscall
    
    mov     [file_descriptor], rax
    test    rax, rax
    js      .error

    ; process CRC and check if it's correct
    mov     rax, [rsp + 24]
    mov     ecx, 65 ; loop counter
    xor     rsi, rsi ; index of current element
    xor     rdx, rdx
    xor     rsi, rsi
.crc_reader_loop:
    mov     dl, [rax + rsi]
    test    dl, dl
    jz      .crc_read_done
    sub     dl, '0'
    shl     qword[crc_poly], 1
    xor     [crc_poly], dl
    ; check for symbols other than 0 or 1
    shr     dl, 1
    jnz     .error
    inc     sil
    loop    .crc_reader_loop

.crc_read_done:
    ; if CRC length is 0 or >64, it's incorrect
    test    cl, cl
    jz      .error
    cmp     cl, 65
    je      .error

    ; shift CRC to MSB of crc_poly
    dec     cl
    shl     qword[crc_poly], cl
    mov     [crc_len], sil
    
    ; calculate CRC lookup table for each byte
    mov     rax, 255
.crc_lookup_main_loop:
    mov     dl, al      
    shl     rdx, 56     ; curent byte to be processed

    ; calculate CRC manually
    mov     cl, 8
.crc_lookup_subloop:
    shl     rdx, 1
    jnc     .no_xor
    xor     rdx, [crc_poly]
.no_xor:
    loop    .crc_lookup_subloop

    ; move CRC value into lookup table
    mov     [crc_table + 8 * rax], rdx
    dec     eax
    jns     .crc_lookup_main_loop

    ; table is ready
    ; calculate actual CRC
.read_segment:
    ; if buffer is empty and read is completed, we are done
    mov     al, [read_finished]
    test    al, al
    jnz     .finish

    ; read segment size
    mov     rax, SYS_READ
    mov     rdi, [file_descriptor]
    mov     rsi, segment_size
    mov     rdx, 2
    syscall
    cmp     rax, 2
    jne     .error
    
    ; read actual data
    mov     rax, SYS_READ
    mov     rdi, [file_descriptor]
    mov     rsi, file_buffer
    mov     rdx, [segment_size]
    syscall
    cmp     rax, [segment_size]
    jne     .error
    add     [buffer_data_size], rax
    
    ; read next segment offset
    mov     rax, SYS_READ
    mov     rdi, [file_descriptor]
    mov     rsi, segment_shift
    mov     rdx, 4
    syscall
    cmp     rax, 4
    jne     .error

    ; move to next segment
    mov     rax, SYS_LSEEK
    mov     rdi, [file_descriptor]
    mov     rsi, [segment_shift]
    movsxd  rsi, esi
    mov     rdx, SEEK_CUR
    syscall
    test    rax, rax
    js      .error

    ; check if we are at the end of file
    mov     edx, [segment_shift]
    add     edx, 6
    add     edx, [segment_size]
    jnz     .finish_read
    mov     byte[read_finished], 1
.finish_read:
    xor     rdx, rdx

.crc_calculation_loop:
    ; check if we need more data in
    cmp     qword[buffer_data_size], 0
    je      .read_segment

    ; move next byte into al
    mov     al, [file_buffer + rdx]

    ; xor it with current crc_val
    shl     rax, 56
    xor     rax, [crc_val]
    shr     rax, 56
    shl     qword[crc_val], 8
    ; get value from lookup table
    mov     rax, [crc_table + 8 * rax]

    ; update crc_val
    xor     [crc_val], rax

    ; move to next byte
    inc     rdx
    dec     qword[buffer_data_size]
    jmp     .crc_calculation_loop
    
.finish:
    ; convert CRC to printable form
    mov     cl, 64
    sub     cl, [crc_len]
    shr     qword[crc_val], cl
    mov     rcx, 63
    xor     r8, r8
.print_loop:
    ; extract single bit from crc_val
    mov     rax, [crc_val]
    shr     rax, cl
    and     rax, 1
    add     al, '0'
    mov     [file_buffer + r8], al
    inc     r8
    dec     cl
    jns     .print_loop
    mov     byte[file_buffer + 64], byte `\n`

    ; print CRC
    mov     rax, SYS_WRITE
    mov     rdi, STDOUT
    ; print only the last crc_len bytes
    mov     rsi, 64
    sub     sil, [crc_len]
    add     rsi, file_buffer
    xor     rdx, rdx
    mov     dl, [crc_len]
    inc     rdx
    syscall
    test    rax, rax
    js      .error
    jmp     .close

.error:
    mov     byte[ret_val], 1
.close:
    mov     rax, SYS_CLOSE
    mov     rdi, [file_descriptor]
    test    rdi, rdi
    jz      .error_exit
    syscall
    test    rax, rax
    jns     .exit
.error_exit:
    mov     byte[ret_val], 1
.exit:
    mov     rax, SYS_EXIT
    xor     rdi, rdi
    mov     dil, [ret_val]
    syscall