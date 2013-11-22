
#ifndef MOTOR_H
#define	MOTOR_H

#include <stdint.h>

#ifdef	__cplusplus
extern "C" {
#endif

void motor_config();
void motor_enable(int enable);
void motor_set_speed(int16_t speedLeft, int16_t speedRight);
void servo_tilt_set(uint8_t val);
void servo_tilt_stop();

#ifdef	__cplusplus
}
#endif

#endif	/* MOTOR_H */

