#ifndef DEBUG_H
#define	DEBUG_H

#include <stdint.h>

#ifdef	__cplusplus
extern "C" {
#endif

void debug_config();
void debug_write(const char* str);
void debug_write_ch(char ch);
void debug_write_u8(uint32_t val, int base);
void debug_write_u32(uint32_t val, int base);
void debug_write_line(const char* str);
void debug_write_bytes(uint8_t *data, uint16_t len);

void assert_failed(uint8_t* file, uint32_t line);
  
#ifdef	__cplusplus
}
#endif

#endif	/* DEBUG_H */

