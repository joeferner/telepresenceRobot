#include "hw_config.h"
#include "ring_buffer.h"
#include <misc.h>
#include <stm32f10x.h>
#include <stm32f10x_gpio.h>
#include <stm32f10x_rcc.h>
#include <stm32f10x_usart.h>
#include <stm32f10x_tim.h>
#include <string.h>

uint8_t g_usart1_tx_buffer_storage[100];
ring_buffer g_usart1_tx_buffer;

void usart_config();
void status_led_config();

int main(void) {
  ring_buffer_init(&g_usart1_tx_buffer, g_usart1_tx_buffer_storage, 100);

  // Configure the NVIC Preemption Priority Bits
  // 2 bit for pre-emption priority, 2 bits for subpriority
  NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
  
  // Turn on LED (GPIOA6)
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);

  usart_config();
  debug_write_line("****************************************");
  debug_write_line("BEGIN Init");
  status_led_config();
  Set_System();
  delay_ms(100);
  USB_Init();  
  debug_write_line("END Init");
    
  for (;;);
  return 0;
}

void assert_failed(uint8_t* file, uint32_t line) {
  debug_write("Wrong parameters value: file ");
  debug_write((const char*)file);
  debug_write(" on line ");
  debug_write_u32(line, 10);
  debug_write_line("");

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

void status_led_config() {
  GPIO_InitTypeDef GPIO_Config;
  GPIO_Config.GPIO_Pin =  GPIO_Pin_6;
  GPIO_Config.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_Config);

  GPIO_ResetBits(GPIOA, GPIO_Pin_6);  
}

void usart_config() {
  USART_InitTypeDef usartInitStructure;
  GPIO_InitTypeDef gpioInitStructure;
  NVIC_InitTypeDef nvicInitStructure;

  /* Enable the USART1 Interrupt */
  nvicInitStructure.NVIC_IRQChannel = USART1_IRQn;
  nvicInitStructure.NVIC_IRQChannelPreemptionPriority = 3;
  nvicInitStructure.NVIC_IRQChannelSubPriority = 3;
  nvicInitStructure.NVIC_IRQChannelCmd = ENABLE;
  NVIC_Init(&nvicInitStructure);

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

  /* Enable the USART interrupts */
  USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);
  USART_ITConfig(USART1, USART_IT_TXE, DISABLE);
}

void USART1_IRQHandler(void) {
  if(USART_GetITStatus(USART1, USART_IT_RXNE) != RESET) {
    USART_ReceiveData(USART1); // TODO do something with this data
  }
  
//  if(USART_GetITStatus(USART1, USART_IT_TXE) != RESET) {
//    if(g_usart1_tx_buffer.available > 0) {
//      uint8_t v = ring_buffer_read(&g_usart1_tx_buffer);
//      USART_SendData(USART1, v);
//    }
//    if(g_usart1_tx_buffer.available == 0) {
//      USART_ITConfig(USART1, USART_IT_TXE, DISABLE);
//    }
//  }
  
//  USART_ClearITPendingBit(USART1, (USART_FLAG_CTS | USART_FLAG_LBD | USART_FLAG_TC | USART_FLAG_RXNE));
}

void debug_write_line(const char* str) {
  debug_write(str);
  debug_write_ch('\n');
}

void debug_write_bytes(uint8_t *data, uint16_t len) {
  for(uint16_t i=0; i<len; i++) {
    debug_write_ch((char)data[i]);
  }
}

void debug_write(const char* str) {
  const char *p = str;
  while(*p) {
    debug_write_ch(*p);
    p++;
  }
}

void debug_write_ch(char ch) {
  ring_buffer_write(&g_usart1_tx_buffer, ch);
  USART_SendData(USART1, ring_buffer_read(&g_usart1_tx_buffer));
  while(USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET);
  //USART_ITConfig(USART1, USART_IT_TXE, ENABLE);
}

#define TO_HEX(i) ( (((i) & 0xf) <= 9) ? ('0' + ((i) & 0xf)) : ('A' - 10 + ((i) & 0xf)) )

void debug_write_u8(uint32_t val, int base) {
  char str[4];
  if(base == 16) {
    str[0] = TO_HEX(val >> 4);   
    str[1] = TO_HEX(val >> 0);   
    str[2] = '\0';
    debug_write(str);
  } else {
    debug_write_line("NOT IMPLEMENTED");    
  }
}

void debug_write_u32(uint32_t val, int base) {
  debug_write_line("NOT IMPLEMENTED");
}

void delay_ms(uint32_t ms) {
  volatile uint32_t i;
  for(i = ms; i != 0; i--) {
    delay_us(1000);
  }
}

void delay_us(uint32_t us) {
  volatile uint32_t i;
  for(i = (5 * us); i != 0; i--) {}
}
