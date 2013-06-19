#include "hw_config.h"
#include <stm32f10x.h>
#include <stm32f10x_gpio.h>
#include <stm32f10x_rcc.h>
 #include "stm32f10x_tim.h"

int main(void) {
  // Turn on LED (GPIOA6)
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);

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
