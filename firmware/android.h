#ifndef ANDROID_H
#define	ANDROID_H

#include <stdint.h>

#ifdef	__cplusplus
extern "C" {
#endif

void android_config();
void android_write_bytes(const uint8_t *data, uint16_t len);
void android_write_ch(char ch);
extern void android_on_rx(uint8_t* data, uint16_t len);

#ifdef	__cplusplus
}
#endif

#endif	/* ANDROID_H */

