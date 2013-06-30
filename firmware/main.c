
#include "delay.h"
#include "debug.h"
#include "status_led.h"
#include "usb.h"
#include "ring_buffer.h"
#include "util.h"
#include "time.h"
#include <misc.h>
#include <string.h>

typedef struct {
  int8_t speedLeft;
  int8_t speedRight;
} RobotRegisters;

#define INPUT_BUFFER_SIZE 100
uint8_t input_buffer[INPUT_BUFFER_SIZE];
ring_buffer input_ring_buffer;
RobotRegisters robot_registers;

void process_input(uint8_t* data, uint16_t len);
void process_input_line(char* line);
int8_t parse_speed(const char* str);
void set_speed(int8_t speedLeft, int8_t speedRight);

int main(void) {
  // Configure the NVIC Preemption Priority Bits
  // 2 bit for pre-emption priority, 2 bits for subpriority
  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);

  ring_buffer_init(&input_ring_buffer, input_buffer, INPUT_BUFFER_SIZE);

  debug_config();
  delay_ms(100);
  print("?****************************************\n");
  print("?BEGIN Init\n");
  status_led_config();
  time_config();
  usb_config();
  print("?END Init\n");

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

void process_input_line(char* line) {
  if (starts_with(line, "set ")) {
    char* p = line + strlen("set ");
    char* eq = strchr(line, '=');
    if (eq) {
      char* val = eq + 1;
      *eq = '\0';
      if (!strcmp(p, "speed")) {
        int8_t speedLeft = parse_speed(val);
        int8_t speedRight = parse_speed(val + 2);
        set_speed(speedLeft, speedRight);
        print("+OK\n");
      } else {
        print("-Invalid set variable '");
        print(p);
        print("'\n");
      }
    } else {
      print("-Invalid set, no '='\n");
    }
  } else {
    print("-Invalid command: ");
    print(line); // new line is already part of line
  }
}

void set_speed(int8_t speedLeft, int8_t speedRight) {
  robot_registers.speedLeft = speedLeft;
  robot_registers.speedRight = speedRight;
}

/**
 * Speed is encoded using a single byte.
 * @param str String containing a hex byte string
 * @return the speed from -128 to 128
 */
int8_t parse_speed(const char* str) {
  return parse_hex_byte(str);
}

void assert_failed(uint8_t* file, uint32_t line) {
  print("!assert_failed: file ");
  print((const char*) file);
  print(" on line ");
  print_u32(line, 10);
  print("\n");

  /* Infinite loop */
  while (1) {
    delay_ms(100);
    status_led_on();
    delay_ms(100);
    status_led_off();
  }
}
