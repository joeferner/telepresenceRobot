
#include "debug.h"
#include "status_led.h"

void NMI_Handler(void) {
}

void HardFault_Handler(void) {
  print("HardFault_Handler\n");
  status_led_infinite_loop();
}

void MemManage_Handler(void) {
  print("MemManage_Handler\n");
  status_led_infinite_loop();
}

void BusFault_Handler(void) {
  print("BusFault_Handler\n");
  status_led_infinite_loop();
}

void UsageFault_Handler(void) {
  print("UsageFault_Handler\n");
  status_led_infinite_loop();
}

void SVC_Handler(void) {
  print("SVC_Handler\n");
  status_led_infinite_loop();
}

void DebugMon_Handler(void) {
  print("DebugMon_Handler\n");
  status_led_infinite_loop();
}

void PendSV_Handler(void) {
  print("PendSV_Handler\n");
  status_led_infinite_loop();
}
