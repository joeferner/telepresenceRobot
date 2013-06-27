#ifndef STATUS_LED_H
#define	STATUS_LED_H

#ifdef	__cplusplus
extern "C" {
#endif

void status_led_config();
void status_led_on();
void status_led_off();
void status_led_infinite_loop();

#ifdef	__cplusplus
}
#endif

#endif	/* STATUS_LED_H */

