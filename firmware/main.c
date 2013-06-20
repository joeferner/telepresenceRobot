#include "hw_config.h"
#include <stm32f10x.h>
#include <stm32f10x_gpio.h>
#include <stm32f10x_rcc.h>
#include <stm32f10x_usart.h>
#include <stm32f10x_tim.h>
#include <string.h>

void usart_config();

int main(void) {
  // Turn on LED (GPIOA6)
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);

  usart_config();
  
  GPIO_InitTypeDef GPIO_Config;
  GPIO_Config.GPIO_Pin =  GPIO_Pin_6;
  GPIO_Config.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_Config);

  GPIO_ResetBits(GPIOA, GPIO_Pin_6);

  Set_System();
  Set_USBClock();
  USB_Interrupts_Config();
  USB_Init();  
  
  for (;;);
  return 0;
}

void assert_failed(uint8_t* file, uint32_t line)
{
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

  /* Infinite loop */
  volatile uint32_t index = 0; 
  while (1)
  {
    for(index = (34000 * 100); index != 0; index--) {}
    GPIO_SetBits(GPIOA, GPIO_Pin_6);
    for(index = (34000 * 100); index != 0; index--) {}
    GPIO_ResetBits(GPIOA, GPIO_Pin_6);
  }
}

void usart_config() {
  USART_InitTypeDef usartInitStructure;
  GPIO_InitTypeDef gpioInitStructure;

  usartInitStructure.USART_BaudRate = 9600;
  usartInitStructure.USART_WordLength = USART_WordLength_8b;
  usartInitStructure.USART_Parity = USART_Parity_No;
  usartInitStructure.USART_StopBits = USART_StopBits_1;
  usartInitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
  usartInitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;

  /* Enable clocks */
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_AFIO | RCC_APB2Periph_USART1, ENABLE);

  /* Configure USART Tx as alternate function push-pull */
  gpioInitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
  gpioInitStructure.GPIO_Pin = GPIO_Pin_9;
  gpioInitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &gpioInitStructure);

  /* Configure USART Rx as input floating */
  gpioInitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
  gpioInitStructure.GPIO_Pin = GPIO_Pin_10;
  GPIO_Init(GPIOA, &gpioInitStructure);

  /* USART configuration */
  USART_Init(USART1, &usartInitStructure);
  
  /* Enable USART */
  USART_Cmd(USART1, ENABLE);

  /* Enable the USART Receive interrupt */
  USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);
}

void debug_write_line(const char* str) {
  const char *p = str;
  while(*p) {
    USART_SendData(USART1, *p);  
  }
  USART_SendData(USART1, '\n');  
}