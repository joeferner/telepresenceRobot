#ifndef DEBUG_H
#define	DEBUG_H

#include <stdint.h>

#ifdef	__cplusplus
extern "C" {
#endif

void debug_config();
void debug_write_bytes(const uint8_t *data, uint16_t len);
extern void debug_on_rx(uint8_t* data, uint16_t len);

void assert_failed(uint8_t* file, uint32_t line);
  
#ifdef	__cplusplus
}
#endif

#endif	/* DEBUG_H */

