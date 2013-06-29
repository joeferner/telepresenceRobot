
#include "util.h"
#include <string.h>

int starts_with(const char* str, const char* strTest) {
  return strncmp(strTest, str, strlen(strTest)) == 0;
}

