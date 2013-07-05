
#include "debug.h"
#include "status_led.h"
#include "util.h" 

void NMI_Handler(void) {
}

void HardFault_Handler(void) {
  print_error("HardFault_Handler\n");
  status_led_infinite_loop();
}

void MemManage_Handler(void) {
  print_error("MemManage_Handler\n");
  status_led_infinite_loop();
}

void BusFault_Handler(void) {
  print_error("BusFault_Handler\n");
  status_led_infinite_loop();
}

void UsageFault_Handler(void) {
  print_error("UsageFault_Handler\n");
  status_led_infinite_loop();
}

void SVC_Handler(void) {
  print_error("SVC_Handler\n");
  status_led_infinite_loop();
}

void DebugMon_Handler(void) {
  print_error("DebugMon_Handler\n");
  status_led_infinite_loop();
}

void PendSV_Handler(void) {
  print_error("PendSV_Handler\n");
  status_led_infinite_loop();
}
