
#include "debug.h"
#include "util.h" 

void NMI_Handler(void) {
}

void HardFault_Handler(void) {
  print_error("HardFault_Handler\n");
}

void MemManage_Handler(void) {
  print_error("MemManage_Handler\n");
}

void BusFault_Handler(void) {
  print_error("BusFault_Handler\n");
}

void UsageFault_Handler(void) {
  print_error("UsageFault_Handler\n");
}

void SVC_Handler(void) {
  print_error("SVC_Handler\n");
}

void DebugMon_Handler(void) {
  print_error("DebugMon_Handler\n");
}

void PendSV_Handler(void) {
  print_error("PendSV_Handler\n");
}
