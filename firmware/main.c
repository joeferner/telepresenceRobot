
#include "delay.h"
#include "debug.h"
#include "status_led.h"
#include "usb.h"
#include "ring_buffer.h"
#include <misc.h>
#include <string.h>

#define INPUT_BUFFER_SIZE 100
uint8_t input_buffer[INPUT_BUFFER_SIZE];
ring_buffer input_ring_buffer;

void process_input(uint8_t* data, uint16_t len);
void process_input_line(const char* line);

int main(void) {
  // Configure the NVIC Preemption Priority Bits
  // 2 bit for pre-emption priority, 2 bits for subpriority
  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
  
  ring_buffer_init(&input_ring_buffer, input_buffer, INPUT_BUFFER_SIZE);
  
  debug_config();
  delay_ms(100);
  debug_write_line("****************************************");
  debug_write_line("BEGIN Init");
  status_led_config();
  usb_config();
  debug_write_line("END Init");
  
  while(1) {
    
  }
  return 0;
}

void debug_on_rx(uint8_t* data, uint16_t len) {
  process_input(data, len);
}

void usb_on_rx(uint8_t* data, uint16_t len) {
  process_input(data, len);
}

void process_input(uint8_t* data, uint16_t len) {
  char line[100];
  
  ring_buffer_write(&input_ring_buffer, data, len);
  if(ring_buffer_readline(&input_ring_buffer, line, 100) > 0) {
    process_input_line(line);
  }
  
}

void process_input_line(const char* line) {
  uint16_t len = strlen(line);
  
  debug_write_bytes((uint8_t*)"OK", 2);
  debug_write_bytes((const uint8_t*)line, len);
  usb_write((uint8_t*)"OK", 2);
  usb_write((const uint8_t*)line, len);
}
