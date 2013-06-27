
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include "../ring_buffer.h"

#define BUFFER_SIZE 100
uint8_t buffer[BUFFER_SIZE];
ring_buffer ring;

void main() {
  char temp[100];
  uint16_t len;

  ring_buffer_init(&ring, buffer, BUFFER_SIZE);

  len = ring_buffer_readline(&ring, temp, 100);
  printf("%d - %s\n", len, temp);
  
  strcpy(temp, "line1\nline2\nline3");
  ring_buffer_write(&ring, temp, strlen(temp));
  while((len = ring_buffer_readline(&ring, temp, 100)) > 0) {
    printf("%d - %s\n", len, temp);
  }
  
  printf("available: %d\n", ring.available);
  ring_buffer_write_byte(&ring, '\n');
  while((len = ring_buffer_readline(&ring, temp, 100)) > 0) {
    printf("%d - %s\n", len, temp);
  }
}
