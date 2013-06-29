
#ifndef UTIL_H
#define	UTIL_H

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

#ifdef	__cplusplus
}
#endif

#endif	/* UTIL_H */

