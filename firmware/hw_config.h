#ifndef __HW_CONFIG_H
#define __HW_CONFIG_H

#include "platform_config.h"
#include "usb_type.h"
#include <stdio.h>

#define MASS_MEMORY_START     0x04002000
#define BULK_MAX_PACKET_SIZE  0x00000040
#define LED_ON                0xF0
#define LED_OFF               0xFF

#define USART_RX_DATA_SIZE   2048

void Set_System(void);
void Enter_LowPowerMode(void);
void Leave_LowPowerMode(void);
void USB_Cable_Config (FunctionalState NewState);
void Handle_USBAsynchXfer (void);
void Get_SerialNum(void);

void delay_ms(uint32_t ms);
void delay_us(uint32_t us);

void debug_write(const char* str);
void debug_write_ch(char ch);
void debug_write_u8(uint32_t val, int base);
void debug_write_u32(uint32_t val, int base);
void debug_write_line(const char* str);
void debug_write_bytes(uint8_t *data, uint16_t len);

#endif  /*__HW_CONFIG_H*/
