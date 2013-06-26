
#include "ring_buffer.h"
#include "delay.h"
#include "debug.h"
#include "status_led.h"
#include "usb.h"
#include <misc.h>
#include <stm32f10x.h>
#include <stm32f10x_rcc.h>
#include <string.h>

void status_led_config();

int main(void) {
  // Configure the NVIC Preemption Priority Bits
  // 2 bit for pre-emption priority, 2 bits for subpriority
  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
  
  debug_config();
  delay_ms(100);
  debug_write_line("****************************************");
  debug_write_line("BEGIN Init");
  status_led_config();
  usb_config();
  debug_write_line("END Init");
  
  for (;;);
  return 0;
}

