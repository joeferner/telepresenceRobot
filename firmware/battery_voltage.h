#ifndef BATTERY_VOLTAGE_H
#define	BATTERY_VOLTAGE_H

#include <stdint.h>

#ifdef	__cplusplus
extern "C" {
#endif

  void battery_voltage_config();
  uint32_t battery_voltage_read();
  
#ifdef	__cplusplus
}
#endif

#endif

