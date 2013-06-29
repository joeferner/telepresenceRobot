
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
void process_input_line(char* line);
void process_response_write(const char* str);

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

  while (1) {

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
#define MAX_LINE_LENGTH 100
  char line[MAX_LINE_LENGTH];

  ring_buffer_write(&input_ring_buffer, data, len);
  while (ring_buffer_readline(&input_ring_buffer, line, MAX_LINE_LENGTH) > 0) {
    process_input_line(line);
  }

}

void process_response_write(const char* str) {
  int len = strlen(len);
  debug_write_bytes((const uint8_t*) str, len);
  usb_write((const uint8_t*) str, len);
}

void process_input_line(char* line) {
  if (starts_with(line, "set ")) {
    char* p = line + strlen("set ");
    char* eq = strchr(line, '=');
    if(eq) {
      *eq = '\0';
      if(!strcmp(p, "forward")) {
        process_response_write("+OK\n");
      } else if(!strcmp(p, "back")) {
        process_response_write("+OK\n");
      } else if(!strcmp(p, "left")) {
        process_response_write("+OK\n");
      } else if(!strcmp(p, "right")) {
        process_response_write("+OK\n");
      } else {
        process_response_write("-Invalid set variable '");
        process_response_write(p);
        process_response_write("'\n");
      }
    } else {
      process_response_write("-Invalid set, no '='\n");
    }
  } else {
    process_response_write("-Invalid command: ");
    process_response_write(line); // new line is already part of line
  }
}
