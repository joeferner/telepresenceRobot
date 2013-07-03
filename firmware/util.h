
#ifndef UTIL_H
#define	UTIL_H

#include <stdint.h>

#ifdef	__cplusplus
extern "C" {
#endif

#define FALSE 0
#define TRUE  1
  
#define min(a,b) ( ((a) < (b)) ? (a) : (b) )
#define max(a,b) ( ((a) > (b)) ? (a) : (b) )

int starts_with(const char* str, const char* strTest); 
uint8_t parse_hex_nibble(char ch);
uint8_t parse_hex_byte(const char* str);

void print(const char* str);
void print_u8(uint8_t val, uint8_t base);
void print_u32(uint32_t val, uint8_t base);

#ifdef	__cplusplus
}
#endif

#endif	/* UTIL_H */
