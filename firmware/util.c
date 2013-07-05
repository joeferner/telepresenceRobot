
#include "util.h"
#include "debug.h"
#include <string.h>

extern void usb_write(const uint8_t* data, uint16_t len);

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

#define TO_HEX(i) ( (((i) & 0xf) <= 9) ? ('0' + ((i) & 0xf)) : ('A' - 10 + ((i) & 0xf)) )

void print_u32(uint32_t val, uint8_t base) {
  print_error("NOT IMPLEMENTED");
}

void print_u8(uint8_t val, uint8_t base) {
  char str[4];
  if(base == 16) {
    str[0] = TO_HEX(val >> 4);
    str[1] = TO_HEX(val >> 0);
    str[2] = '\0';
    print(str);
  } else {
    print_error("NOT IMPLEMENTED");
  }
}

int is_whitespace(char ch) {
  switch(ch) {
    case '\n':
    case '\r':
    case '\t':
    case ' ':
      return TRUE;
  }
  return FALSE;
}

void trim_right(char* str) {
  char *p = str + strlen(str) - 1;
  while(is_whitespace(*p)) {
    *p-- = '\0';
  }
}