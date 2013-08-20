#ifndef __PLATFORM_CONFIG_H
#define __PLATFORM_CONFIG_H

#include <stm32f10x.h>
#include <stm32f10x_gpio.h>
#include <stm32f10x_exti.h>
#include <stm32f10x_pwr.h>
#include <stm32f10x_rcc.h>
#include <stm32f10x_adc.h>
#include <usb_lib.h>

#define ID1                    (0x1FFFF7E8)
#define ID2                    (0x1FFFF7EC)
#define ID3                    (0x1FFFF7F0)

#define USB_DISCONNECT         GPIOA
#define USB_DISCONNECT_PIN     GPIO_Pin_8
#define USB_DISCONNECT_RCC     RCC_APB2Periph_GPIOA

#define BATTERY_VOLTAGE        GPIOA
#define BATTERY_VOLTAGE_PIN    GPIO_Pin_0
#define BATTERY_VOLTAGE_RCC    RCC_APB2Periph_GPIOA
#define BATTERY_VOLTAGE_ADC_CH ADC_Channel_0

#define MOTOR_PWM_TIMER_RCC    RCC_APB1Periph_TIM2

#define MOTOR_EN               GPIOA
#define MOTOR_EN_PIN           GPIO_Pin_13
#define MOTOR_EN_RCC           RCC_APB2Periph_GPIOA

#define MOTOR_LEFT_DIR         GPIOA
#define MOTOR_LEFT_DIR_PIN     GPIO_Pin_15
#define MOTOR_LEFT_DIR_RCC     RCC_APB2Periph_GPIOA

// TIM2_CH3
#define MOTOR_LEFT_PWM         GPIOA
#define MOTOR_LEFT_PWM_PIN     GPIO_Pin_2
#define MOTOR_LEFT_PWM_RCC     RCC_APB2Periph_GPIOA | RCC_APB2Periph_AFIO

#define MOTOR_RIGHT_DIR        GPIOA
#define MOTOR_RIGHT_DIR_PIN    GPIO_Pin_14
#define MOTOR_RIGHT_DIR_RCC    RCC_APB2Periph_GPIOA

// TIM2_CH4
#define MOTOR_RIGHT_PWM        GPIOA
#define MOTOR_RIGHT_PWM_PIN    GPIO_Pin_3
#define MOTOR_RIGHT_PWM_RCC    RCC_APB2Periph_GPIOA | RCC_APB2Periph_AFIO

// TIM2_CH2
#define SERVO_TILT_PWM         GPIOA
#define SERVO_TILT_PWM_PIN     GPIO_Pin_1
#define SERVO_TILT_PWM_RCC     RCC_APB2Periph_GPIOA | RCC_APB2Periph_AFIO

#define DEBUG_USART            USART1
#define DEBUG_USART_BAUD       9600
#define DEBUG_USART_IRQ        USART1_IRQn
#define DEBUG_USART_RCC        RCC_APB2Periph_GPIOA | RCC_APB2Periph_AFIO | RCC_APB2Periph_USART1
#define DEBUG_USART_TX         GPIOA
#define DEBUG_USART_TX_PIN     GPIO_Pin_9
#define DEBUG_USART_RX         GPIOA
#define DEBUG_USART_RX_PIN     GPIO_Pin_10

#endif /* __PLATFORM_CONFIG_H */
