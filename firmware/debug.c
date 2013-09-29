
#include "platform_config.h"
#include "debug.h"
#include "delay.h"
#include "util.h"
#include <stm32f10x_usart.h>
#include <stm32f10x_rcc.h>
#include <stm32f10x_gpio.h>

void debug_led_config() {
  GPIO_InitTypeDef GPIO_Config;

  RCC_APB2PeriphClockCmd(DEBUG_LED_RCC, ENABLE);
  GPIO_Config.GPIO_Pin = DEBUG_LED_PIN;
  GPIO_Config.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(DEBUG_LED, &GPIO_Config);
  debug_led_off();
}

void debug_led_on() {
  GPIO_SetBits(DEBUG_LED, DEBUG_LED_PIN);
}

void debug_led_off() {
  GPIO_ResetBits(DEBUG_LED, DEBUG_LED_PIN);
}
