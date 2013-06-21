#include <stm32f10x_exti.h>
#include <stm32f10x_misc.h>
#include "stm32_it.h"
#include "usb_lib.h"
#include "usb_prop.h"
#include "usb_desc.h"
#include "hw_config.h"
#include "usb_pwr.h"

ErrorStatus HSEStartUpStatus;
uint8_t USART_Rx_Buffer [USART_RX_DATA_SIZE];
uint32_t USART_Rx_ptr_in = 0;
uint32_t USART_Rx_ptr_out = 0;
uint32_t USART_Rx_length = 0;

uint8_t USB_Tx_State = 0;
static void IntToUnicode(uint32_t value, uint8_t *pbuf, uint8_t len);

extern LINE_CODING linecoding;

/**
 * Configures Main system clocks & power
 */
void Set_System(void) {
  debug_write_line("BEGIN Set_System");

  GPIO_InitTypeDef GPIO_InitStructure;
  EXTI_InitTypeDef EXTI_InitStructure;

  /* Enable the USB disconnect GPIO clock */
  RCC_AHBPeriphClockCmd(RCC_AHBPeriph_GPIO_DISCONNECT, ENABLE);

  /* USB_DISCONNECT used as USB pull-up */
  GPIO_InitStructure.GPIO_Pin = USB_DISCONNECT_PIN;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_2MHz;
  GPIO_Init(USB_DISCONNECT, &GPIO_InitStructure);

  /* Configure the EXTI line 18 connected internally to the USB IP */
  EXTI_ClearITPendingBit(EXTI_Line18);
  EXTI_InitStructure.EXTI_Line = EXTI_Line18;
  EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Rising;
  EXTI_InitStructure.EXTI_LineCmd = ENABLE;
  EXTI_Init(&EXTI_InitStructure);
  
  USB_Cable_Config(DISABLE);
}

/**
 * Configures USB Clock input (48MHz)
 */
void Set_USBClock(void) {
  debug_write_line("BEGIN Set_USBClock");

  /* Enable USB clock */
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_USB, ENABLE);
}

/**
 * Power-off system clocks and power while entering suspend mode
 */
void Enter_LowPowerMode(void) {
  /* Set the device state to suspend */
  bDeviceState = SUSPENDED;
}

/**
 * Restores system clocks and power while exiting suspend mode
 */
void Leave_LowPowerMode(void) {
  DEVICE_INFO *pInfo = &Device_Info;

  /* Set the device state to the correct state */
  if (pInfo->Current_Configuration != 0) {
    /* Device configured */
    bDeviceState = CONFIGURED;
  } else {
    bDeviceState = ATTACHED;
  }
  /*Enable SystemCoreClock*/
  SystemInit();
}

/**
 * Configures the USB interrupts
 */
void USB_Interrupts_Config(void) {
  NVIC_InitTypeDef NVIC_InitStructure;

  debug_write_line("BEGIN USB_Interrupts_Config");

  NVIC_InitStructure.NVIC_IRQChannel = USB_LP_CAN1_RX0_IRQn;
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 2;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
  NVIC_Init(&NVIC_InitStructure);
  
    /* Enable the USB Wake-up interrupt */
  NVIC_InitStructure.NVIC_IRQChannel = USBWakeUp_IRQn;
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
  NVIC_Init(&NVIC_InitStructure);
}

/**
 * Software Connection/Disconnection of USB Cable
 */
void USB_Cable_Config(FunctionalState NewState) {
  if (NewState != DISABLE) {
    debug_write_line("USB+");
    GPIO_ResetBits(USB_DISCONNECT, USB_DISCONNECT_PIN); // P-channel MOSFET - ON
  } else {
    debug_write_line("USB-");
    GPIO_SetBits(USB_DISCONNECT, USB_DISCONNECT_PIN); // P-channel MOSFET - OFF
  }
}

/**
 * send data to USB.
 */
void Handle_USBAsynchXfer(void) {

  uint16_t USB_Tx_ptr;
  uint16_t USB_Tx_length;

  if (USB_Tx_State != 1) {
    if (USART_Rx_ptr_out == USART_RX_DATA_SIZE) {
      USART_Rx_ptr_out = 0;
    }

    if (USART_Rx_ptr_out == USART_Rx_ptr_in) {
      USB_Tx_State = 0;
      return;
    }

    if (USART_Rx_ptr_out > USART_Rx_ptr_in) /* rollback */ {
      USART_Rx_length = USART_RX_DATA_SIZE - USART_Rx_ptr_out;
    } else {
      USART_Rx_length = USART_Rx_ptr_in - USART_Rx_ptr_out;
    }

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
    USB_Tx_State = 1;
    UserToPMABufferCopy(&USART_Rx_Buffer[USB_Tx_ptr], ENDP1_TXADDR, USB_Tx_length);
    SetEPTxCount(ENDP1, USB_Tx_length);
    SetEPTxValid(ENDP1);
  }
}

/**
 * Create the serial number string descriptor.
 */
void Get_SerialNum(void) {
  uint32_t Device_Serial0, Device_Serial1, Device_Serial2;
  debug_write_line("BEGIN Get_SerialNum");

  Device_Serial0 = *(uint32_t*) ID1;
  Device_Serial1 = *(uint32_t*) ID2;
  Device_Serial2 = *(uint32_t*) ID3;

  Device_Serial0 += Device_Serial2;

  if (Device_Serial0 != 0) {
    IntToUnicode(Device_Serial0, &Virtual_Com_Port_StringSerial[2], 8);
    IntToUnicode(Device_Serial1, &Virtual_Com_Port_StringSerial[18], 4);
  }
}

/**
 * Convert Hex 32Bits value into char.
 */
static void IntToUnicode(uint32_t value, uint8_t *pbuf, uint8_t len) {
  uint8_t idx = 0;

  for (idx = 0; idx < len; idx++) {
    if (((value >> 28)) < 0xA) {
      pbuf[ 2 * idx] = (value >> 28) + '0';
    } else {
      pbuf[2 * idx] = (value >> 28) + 'A' - 10;
    }

    value = value << 4;

    pbuf[ 2 * idx + 1] = 0;
  }
}
