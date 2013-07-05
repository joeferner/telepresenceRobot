
#include "delay.h"
#include "debug.h"
#include "status_led.h"
#include "usb.h"
#include "ring_buffer.h"
#include "util.h"
#include "time.h"
#include "motor.h"
#include <misc.h>
#include <string.h>

typedef struct {
  volatile int8_t speedLeft;
  volatile int8_t speedRight;
  volatile int8_t targetSpeedLeft;
  volatile int8_t targetSpeedRight;
  volatile uint32_t targetSpeedLastUpdated;
} RobotRegisters;

#define INPUT_BUFFER_SIZE 100
uint8_t input_buffer[INPUT_BUFFER_SIZE];
ring_buffer input_ring_buffer;
RobotRegisters robot_registers;
uint32_t last_update_speed = 0;

void loop();
void init_robot_registers();
void process_input(uint8_t* data, uint16_t len);
void process_input_line(char* line);
void process_set_command(char* line);
void process_get_command(char* line);
void process_connect_command(char* line);
int8_t parse_speed(const char* str);
void set_speed(int8_t speedLeft, int8_t speedRight);
void update_speed();

int main(void) {
  // Configure the NVIC Preemption Priority Bits
  // 2 bit for pre-emption priority, 2 bits for subpriority
  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);

  ring_buffer_init(&input_ring_buffer, input_buffer, INPUT_BUFFER_SIZE);

  debug_config();
  delay_ms(100);
  print_info("****************************************\n");
  print_info("BEGIN Init\n");
  init_robot_registers();
  status_led_config();
  motor_config();
  time_config();
  usb_config();
  print_info("END Init\n");

  motor_enable(TRUE);
  while (1) {
    loop();
  }
  return 0;
}

void loop() {
  update_speed();  
}

void update_speed() {
  if ((time_ms() - last_update_speed) < 100) {
    return;
  }
  
  // this timeout is for safety. If we haven't received a speed update in over x seconds we should stop the robot
  if((time_ms() - robot_registers.targetSpeedLastUpdated) > 10000) {
    set_speed(0, 0);
  }
  
  // provide for acceleration and deceleration
  if (robot_registers.targetSpeedLeft > robot_registers.speedLeft) {
    robot_registers.speedLeft += min(10, robot_registers.targetSpeedLeft - robot_registers.speedLeft);
  } else if (robot_registers.targetSpeedLeft < robot_registers.speedLeft) {
    robot_registers.speedLeft -= min(10, robot_registers.speedLeft - robot_registers.targetSpeedLeft);
  }

  if (robot_registers.targetSpeedRight > robot_registers.speedRight) {
    robot_registers.speedRight += min(10, robot_registers.targetSpeedRight - robot_registers.speedRight);
  } else if (robot_registers.targetSpeedRight < robot_registers.speedRight) {
    robot_registers.speedRight -= min(10, robot_registers.speedRight - robot_registers.targetSpeedRight);
  }

  motor_set_speed(robot_registers.speedLeft, robot_registers.speedRight);
  
  last_update_speed = time_ms();
}

void init_robot_registers() {
  robot_registers.speedLeft = 0;
  robot_registers.speedRight = 0;
  robot_registers.targetSpeedLeft = 0;
  robot_registers.targetSpeedRight = 0;
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
    trim_right(line);
    process_input_line(line);
  }
}

void process_input_line(char* line) {
  print_info("");
  print(line);
  print("\n");

  if (starts_with(line, "connect")) {
    process_connect_command(line);
  } else if (starts_with(line, "set ")) {
    process_set_command(line);
  } else if (starts_with(line, "get ")) {
    process_get_command(line);
  } else {
    print_fail("Invalid command: ");
    print(line);
    print("\n");
  }
}

void process_connect_command(char* line) {
  print_success("OK\n");
}

void process_set_command(char* line) {
  char* p = line + strlen("set ");
  char* eq = strchr(line, '=');
  if (eq) {
    char* val = eq + 1;
    *eq = '\0';
    if (!strcmp(p, "speed")) {
      int8_t speedLeft = parse_speed(val);
      int8_t speedRight = parse_speed(val + 2);
      set_speed(speedLeft, speedRight);
      print_success("OK ");
      print_u8(speedLeft, 16);
      print_u8(speedRight, 16);
      print("\n");
    } else {
      print_fail("Invalid set variable '");
      print(p);
      print("'\n");
    }
  } else {
    print_fail("Invalid set, no '='\n");
  }
}

void process_get_command(char* line) {
  char* p = line + strlen("get ");
  if (!strcmp(p, "speed")) {
    print_success("OK ");
    print_u8(robot_registers.speedLeft, 16);
    print_u8(robot_registers.speedRight, 16);
    print("\n");
  } else if (!strcmp(p, "target_speed")) {
    print_success("OK ");
    print_u8(robot_registers.targetSpeedLeft, 16);
    print_u8(robot_registers.targetSpeedRight, 16);
    print("\n");
  } else {
    print_fail("Invalid get variable '");
    print(p);
    print("'\n");
  }
}

void set_speed(int8_t targetSpeedLeft, int8_t targetSpeedRight) {
  robot_registers.targetSpeedLeft = targetSpeedLeft;
  robot_registers.targetSpeedRight = targetSpeedRight;
  robot_registers.targetSpeedLastUpdated = time_ms();
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
  print_error("assert_failed: file ");
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
