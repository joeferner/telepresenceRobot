
#ifndef UTIL_H
#define	UTIL_H

#include <stdint.h>

#ifdef	__cplusplus
extern "C" {
#endif

#define FALSE 0
#define TRUE  1

#define abs(a) ( ((a) < 0) ? -(a) : (a) )  
#define min(a,b) ( ((a) < (b)) ? (a) : (b) )
#define max(a,b) ( ((a) > (b)) ? (a) : (b) )

int starts_with(const char* str, const char* strTest); 
uint8_t parse_hex_nibble(char ch);
uint8_t parse_hex8(const char* str);
uint32_t parse_hex32(const char* str);

#define print_info(str)    print("?" str)
#define print_error(str)   print("!" str)
#define print_fail(str)    print("-" str)
#define print_success(str) print("+" str)
void print(const char* str);
void print_u8(uint8_t val, uint8_t base);
void print_u32(uint32_t val, uint8_t base);

int is_whitespace(char ch);
void trim_right(char* str);

#ifdef	__cplusplus
}
#endif

#endif	/* UTIL_H */

