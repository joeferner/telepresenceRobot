#ifndef DEBUG_H
#define	DEBUG_H

#include <stdint.h>

#ifdef	__cplusplus
extern "C" {
#endif

void debug_led_config();
void debug_led_on();
void debug_led_off();

void assert_failed(uint8_t* file, uint32_t line);
  
#ifdef	__cplusplus
}
#endif

#endif	/* DEBUG_H */

