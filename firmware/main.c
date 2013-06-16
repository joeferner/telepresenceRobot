#include "hw_config.h"
#include "usb_desc.h"
#include "usb_pwr.h"

int main(void) {
  Set_System();
  Set_USBClock();
  USB_Interrupts_Config();
  USB_Init();

  for (;;);
  return 0;
}
