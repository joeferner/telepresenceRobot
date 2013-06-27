
#include "usb.h"
#include "debug.h"
#include "platform_config.h"
#include <stm32f10x_rcc.h>
#include <stm32f10x_exti.h>

/* Interval between sending IN packets in frame number (1 frame = 1ms) */
#define VCOMPORT_IN_FRAME_INTERVAL             5

uint8_t USB_Rx_Buffer[VIRTUAL_COM_PORT_DATA_SIZE];
extern uint8_t USART_Rx_Buffer[];
extern uint32_t USART_Rx_ptr_out;
extern uint32_t USART_Rx_length;
extern uint8_t  USB_Tx_State;

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

/* !!! Interrupt handler - Don't change this function name !!! */
void USB_LP_CAN1_RX0_IRQHandler(void) {
  USB_Istr();
}

/* !!! Interrupt handler - Don't change this function name !!! */
void USBWakeUp_IRQHandler(void) {
  EXTI_ClearITPendingBit(EXTI_Line18);
}

void EP1_IN_Callback (void) {
  uint16_t USB_Tx_ptr;
  uint16_t USB_Tx_length;
    
  if (USB_Tx_State == 1) {
    if (USART_Rx_length == 0) {
      USB_Tx_State = 0;
    } else {
      if (USART_Rx_length > VIRTUAL_COM_PORT_DATA_SIZE) {
        USB_Tx_ptr = USART_Rx_ptr_out;
        USB_Tx_length = VIRTUAL_COM_PORT_DATA_SIZE;
        
        USART_Rx_ptr_out += VIRTUAL_COM_PORT_DATA_SIZE;
        USART_Rx_length -= VIRTUAL_COM_PORT_DATA_SIZE;    
      } else {
        USB_Tx_ptr = USART_Rx_ptr_out;
        USB_Tx_length = USART_Rx_length;
        
        USART_Rx_ptr_out += USART_Rx_length;
        USART_Rx_length = 0;
      }
      UserToPMABufferCopy(&USART_Rx_Buffer[USB_Tx_ptr], ENDP1_TXADDR, USB_Tx_length);
      SetEPTxCount(ENDP1, USB_Tx_length);
      SetEPTxValid(ENDP1); 
    }
  }
}

void EP3_OUT_Callback(void) {
  uint16_t USB_Rx_Cnt;

  /* Get the received data buffer and update the counter */
  USB_Rx_Cnt = USB_SIL_Read(EP3_OUT, USB_Rx_Buffer);

  /* USB data will be immediately processed, this allow next USB traffic being 
  NAKed till the end of the USART Xfer */

  debug_write_bytes(USB_Rx_Buffer, USB_Rx_Cnt);
  // TODO USB_To_USART_Send_Data(USB_Rx_Buffer, USB_Rx_Cnt);

  /* Enable the receive of data on EP3 */
  SetEPRxValid(ENDP3);
}

void SOF_Callback(void) {
  static uint32_t FrameCount = 0;
  
  if(bDeviceState == CONFIGURED) {
    if (FrameCount++ == VCOMPORT_IN_FRAME_INTERVAL) {
      /* Reset the frame counter */
      FrameCount = 0;
      
      /* Check the data to be sent through IN pipe */
      Handle_USBAsynchXfer();
    }
  }  
}
