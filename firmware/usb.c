
#include "usb.h"
#include "debug.h"
#include "platform_config.h"
#include "usb_istr.h"
#include <stm32f10x_rcc.h>
#include <stm32f10x_exti.h>

void usb_config(void) {
  GPIO_InitTypeDef gpioInitStructure;
  EXTI_InitTypeDef extiInitStructure;
  NVIC_InitTypeDef nvicInitStructure;

  debug_write_line("usb_config");

  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);

  /* Enable the USB disconnect GPIO clock */
  RCC_APB2PeriphClockCmd(USB_DISCONNECT_RCC, ENABLE);

  /* USB_DISCONNECT used as USB pull-up */
  gpioInitStructure.GPIO_Pin = USB_DISCONNECT_PIN;
  gpioInitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
  gpioInitStructure.GPIO_Speed = GPIO_Speed_2MHz;
  GPIO_Init(USB_DISCONNECT, &gpioInitStructure);

  /* Configure the EXTI line 18 connected internally to the USB IP */
  EXTI_ClearITPendingBit(EXTI_Line18);
  extiInitStructure.EXTI_Mode = EXTI_Mode_Interrupt;
  extiInitStructure.EXTI_Line = EXTI_Line18;
  extiInitStructure.EXTI_Trigger = EXTI_Trigger_Rising;
  extiInitStructure.EXTI_LineCmd = ENABLE;
  EXTI_Init(&extiInitStructure);
  
  USB_Cable_Config(DISABLE);

  /* Enable USB clock */
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_USB, ENABLE);
  
  nvicInitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;
  nvicInitStructure.NVIC_IRQChannelPreemptionPriority = 2;
  nvicInitStructure.NVIC_IRQChannelSubPriority = 0;
  nvicInitStructure.NVIC_IRQChannelCmd = ENABLE;
  NVIC_Init(&nvicInitStructure);
  
  /* Enable the USB Wake-up interrupt */
  nvicInitStructure.NVIC_IRQChannel = USBWakeUp_IRQn;
  nvicInitStructure.NVIC_IRQChannelPreemptionPriority = 0;
  NVIC_Init(&nvicInitStructure);
  
  debug_write_line("USB_Init");
  USB_Init();
}

// !!! Interrupt handler - Don't change this function name !!! 
void USB_LP_CAN1_RX0_IRQHandler(void) {
  USB_Istr();
}

// !!! Interrupt handler - Don't change this function name !!! 
void USBWakeUp_IRQHandler(void) {
  EXTI_ClearITPendingBit(EXTI_Line18);
}
