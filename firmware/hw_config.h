#ifndef __HW_CONFIG_H
#define __HW_CONFIG_H

#include "platform_config.h"
#include "usb_type.h"

#define MASS_MEMORY_START     0x04002000
#define BULK_MAX_PACKET_SIZE  0x00000040
#define LED_ON                0xF0
#define LED_OFF               0xFF

#define USART_RX_DATA_SIZE   2048

void Set_System(void);
void Set_USBClock(void);
void Enter_LowPowerMode(void);
void Leave_LowPowerMode(void);
void USB_Interrupts_Config(void);
void USB_Cable_Config (FunctionalState NewState);
void Handle_USBAsynchXfer (void);
void Get_SerialNum(void);
void debug_write_line(const char* str);

#endif  /*__HW_CONFIG_H*/
