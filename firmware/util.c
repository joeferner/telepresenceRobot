
#include "util.h"
#include <string.h>

int starts_with(const char* str, const char* strTest) {
  return strncmp(strTest, str, strlen(strTest)) == 0;
}

uint8_t parse_hex_nibble(char ch) {
  if (ch >= '0' && ch <= '9') {
    return ch - '0';
  }

  if (ch >= 'a' && ch <= 'f') {
    return (ch - 'a') + 0xa;
  }

  if (ch >= 'A' && ch <= 'F') {
    return (ch - 'A') + 0xa;
  }

  return 0;
}

uint8_t parse_hex_byte(const char* str) {
  uint8_t highNibble = parse_hex_nibble(str[0]);
  uint8_t lowNibble = parse_hex_nibble(str[1]);
  return (highNibble << 4) | lowNibble;
}

void print(const char* str) {
  int len = strlen(str);
  debug_write_bytes((const uint8_t*) str, len);
  usb_write((const uint8_t*) str, len);
}

void print_u32(uint32_t val, uint8_t base) {
  print("!NOT IMPLEMENTED");
}
