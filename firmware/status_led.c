
#include "status_led.h"
#include "platform_config.h"
#include "debug.h"
#include <stm32f10x_gpio.h>
#include <stm32f10x_rcc.h>

void status_led_config() {
  GPIO_InitTypeDef GPIO_Config;

  debug_write_line("status_led_config");

  RCC_APB2PeriphClockCmd(STATUS_LED_RCC, ENABLE);

  GPIO_Config.GPIO_Pin =  STATUS_LED_PIN;
  GPIO_Config.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(STATUS_LED, &GPIO_Config);

  GPIO_ResetBits(STATUS_LED, STATUS_LED_PIN);  
}

void status_led_on() {
  GPIO_SetBits(STATUS_LED, STATUS_LED_PIN);
}

void status_led_off() {
  GPIO_ResetBits(STATUS_LED, STATUS_LED_PIN);  
}