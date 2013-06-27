#ifndef __PLATFORM_CONFIG_H
#define __PLATFORM_CONFIG_H

#include <stm32f10x.h>
#include <stm32f10x_gpio.h>
#include <stm32f10x_exti.h>
#include <stm32f10x_pwr.h>
#include <stm32f10x_rcc.h>
#include <usb_lib.h>

#define ID1                    (0x1FFFF7E8)
#define ID2                    (0x1FFFF7EC)
#define ID3                    (0x1FFFF7F0)

#define USB_DISCONNECT         GPIOB  
#define USB_DISCONNECT_PIN     GPIO_Pin_9
#define USB_DISCONNECT_RCC     RCC_APB2Periph_GPIOB

#define STATUS_LED             GPIOA
#define STATUS_LED_PIN         GPIO_Pin_6
#define STATUS_LED_RCC         RCC_APB2Periph_GPIOA

#endif /* __PLATFORM_CONFIG_H */
