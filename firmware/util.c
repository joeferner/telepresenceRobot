
#include "util.h"
#include "android.h"
#include <string.h>
#include <math.h>

extern void usb_write(const uint8_t* data, uint16_t len);
char* itoa(int32_t value, char* result, int base);

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

uint8_t parse_hex8(const char* str) {
  uint8_t highNibble = parse_hex_nibble(str[0]);
  uint8_t lowNibble = parse_hex_nibble(str[1]);
  return (highNibble << 4) | lowNibble;
}

uint32_t parse_hex32(const char* str) {
  uint32_t b3 = parse_hex8(str);
  uint32_t b2 = parse_hex8(str + 1);
  uint32_t b1 = parse_hex8(str + 2);
  uint32_t b0 = parse_hex8(str + 3);
  return (b3 << 24) + (b2 << 16) + (b1 << 8) + (b0 << 0);
}

void print(const char* str) {
  int len = strlen(str);
  android_write_bytes((const uint8_t*) str, len);
  usb_write((const uint8_t*) str, len);
}

#define TO_HEX(i) ( (((i) & 0xf) <= 9) ? ('0' + ((i) & 0xf)) : ('A' - 10 + ((i) & 0xf)) )

void print_u32(uint32_t val, uint8_t base) {
  char str[9];
  if (base == 16) {
    str[0] = TO_HEX(val >> 28);
    str[1] = TO_HEX(val >> 24);
    str[2] = TO_HEX(val >> 20);
    str[3] = TO_HEX(val >> 16);
    str[4] = TO_HEX(val >> 12);
    str[5] = TO_HEX(val >> 8);
    str[6] = TO_HEX(val >> 4);
    str[7] = TO_HEX(val >> 0);
    str[8] = '\0';
    print(str);
  } else {
    print_error("NOT IMPLEMENTED");
  }
}

void print_u8(uint8_t val, uint8_t base) {
  char str[4];
  if (base == 16) {
    str[0] = TO_HEX(val >> 4);
    str[1] = TO_HEX(val >> 0);
    str[2] = '\0';
    print(str);
  } else {
    print_error("NOT IMPLEMENTED");
  }
}

void print_8(int8_t val, uint8_t base) {
  char str[10];
  if (base == 10) {
    itoa(val, str, 10);
    print(str);
  } else {
    print_error("NOT IMPLEMENTED");
  }
}

int is_whitespace(char ch) {
  switch (ch) {
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
  while (is_whitespace(*p)) {
    *p-- = '\0';
  }
}

char* itoa(int32_t value, char* result, int base) {
  // check that the base if valid
  if (base < 2 || base > 36) {
    *result = '\0';
    return result;
  }

  char* ptr = result, *ptr1 = result, tmp_char;
  int tmp_value;

  do {
    tmp_value = value;
    value /= base;
    *ptr++ = "zyxwvutsrqponmlkjihgfedcba9876543210123456789abcdefghijklmnopqrstuvwxyz" [35 + (tmp_value - value * base)];
  } while (value);

  // Apply negative sign
  if (tmp_value < 0) {
    *ptr++ = '-';
  }
  *ptr-- = '\0';
  while (ptr1 < ptr) {
    tmp_char = *ptr;
    *ptr-- = *ptr1;
    *ptr1++ = tmp_char;
  }
  return result;
}
