
#include "debug.h"
#include "status_led.h"

void NMI_Handler(void) {
}

void HardFault_Handler(void) {
  debug_write_line("HardFault_Handler");
  status_led_infinite_loop();
}

void MemManage_Handler(void) {
  debug_write_line("MemManage_Handler");
  status_led_infinite_loop();
}

void BusFault_Handler(void) {
  debug_write_line("BusFault_Handler");
  status_led_infinite_loop();
}

void UsageFault_Handler(void) {
  debug_write_line("UsageFault_Handler");
  status_led_infinite_loop();
}

void SVC_Handler(void) {
  debug_write_line("SVC_Handler");
  status_led_infinite_loop();
}

void DebugMon_Handler(void) {
  debug_write_line("DebugMon_Handler");
  status_led_infinite_loop();
}

void PendSV_Handler(void) {
  debug_write_line("PendSV_Handler");
  status_led_infinite_loop();
}

void SysTick_Handler(void) {
}
