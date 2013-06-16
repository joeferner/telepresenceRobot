#ifndef __PLATFORM_CONFIG_H
#define __PLATFORM_CONFIG_H

#include <stm32f10x.h>
#include <stm32f10x_exti.h>
#include <stm32f10x_usart.h>
#include <stm32f10x_pwr.h>
#include <stm32f10x_rcc.h>
#include <usb_lib.h>

#define         ID1          (0x1FFFF7E8)
#define         ID2          (0x1FFFF7EC)
#define         ID3          (0x1FFFF7F0)

#define USB_DISCONNECT                      GPIOB  
#define USB_DISCONNECT_PIN                  GPIO_Pin_9
#define RCC_AHBPeriph_GPIO_DISCONNECT       RCC_APB2Periph_GPIOB
#define EVAL_COM1_IRQHandler                USART1_IRQHandler 
#define EVAL_COM1_IRQn                      USART1_IRQn

#define COMn                             3

/**
 * @brief Definition for COM port1, connected to USART1
 */
#define EVAL_COM1                        USART1
#define EVAL_COM1_CLK                    RCC_APB2Periph_USART1
#define EVAL_COM1_TX_PIN                 GPIO_Pin_9
#define EVAL_COM1_TX_GPIO_PORT           GPIOA
#define EVAL_COM1_TX_GPIO_CLK            RCC_APB2Periph_GPIOA
#define EVAL_COM1_RX_PIN                 GPIO_Pin_10
#define EVAL_COM1_RX_GPIO_PORT           GPIOA
#define EVAL_COM1_RX_GPIO_CLK            RCC_APB2Periph_GPIOA
#define EVAL_COM1_IRQn                   USART1_IRQn

/**
 * @brief Definition for COM port2, connected to USART2
 */
#define EVAL_COM2                        USART2
#define EVAL_COM2_CLK                    RCC_APB1Periph_USART2
#define EVAL_COM2_TX_PIN                 GPIO_Pin_2
#define EVAL_COM2_TX_GPIO_PORT           GPIOA
#define EVAL_COM2_TX_GPIO_CLK            RCC_APB2Periph_GPIOA
#define EVAL_COM2_RX_PIN                 GPIO_Pin_3
#define EVAL_COM2_RX_GPIO_PORT           GPIOA
#define EVAL_COM2_RX_GPIO_CLK            RCC_APB2Periph_GPIOA
#define EVAL_COM2_IRQn                   USART2_IRQn

/**
 * @brief Definition for COM port3, connected to USART3
 */
#define EVAL_COM3                        USART3
#define EVAL_COM3_CLK                    RCC_APB1Periph_USART3
#define EVAL_COM3_TX_PIN                 GPIO_Pin_10
#define EVAL_COM3_TX_GPIO_PORT           GPIOB
#define EVAL_COM3_TX_GPIO_CLK            RCC_APB2Periph_GPIOB
#define EVAL_COM3_RX_PIN                 GPIO_Pin_11
#define EVAL_COM3_RX_GPIO_PORT           GPIOB
#define EVAL_COM3_RX_GPIO_CLK            RCC_APB2Periph_GPIOB
#define EVAL_COM3_IRQn                   USART3_IRQn

typedef enum {
  COM1 = 0,
  COM2 = 1,
  COM3 = 2
} COM_TypeDef;

void STM_EVAL_COMInit(COM_TypeDef COM, USART_InitTypeDef* USART_InitStruct);

#endif /* __PLATFORM_CONFIG_H */
