#include "hw_config.h"
#include <stm32f10x.h>
#include <stm32f10x_gpio.h>
#include <stm32f10x_rcc.h>
 #include "stm32f10x_tim.h"

int main(void) {
  Set_System();
  Set_USBClock();
  USB_Interrupts_Config();
  USB_Init();
  
  // Turn on LED (GPIOA6)
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);

  GPIO_InitTypeDef GPIO_Config;
  GPIO_Config.GPIO_Pin =  GPIO_Pin_6;
  GPIO_Config.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_Config);

  GPIO_SetBits(GPIOA, GPIO_Pin_6);
  
  for (;;);
  return 0;
}
