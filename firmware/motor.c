
#include "motor.h"
#include "platform_config.h"
#include "util.h"
#include <stm32f10x_tim.h>

#define PWM_PERIOD 0x2000

int8_t lastSpeedLeft = 0xff;
int8_t lastSpeedRight = 0xff;

uint32_t speed_to_pwm_compare(int8_t speed);

void motor_config() {
  GPIO_InitTypeDef GPIO_Config;
  uint16_t prescalerValue = 0;
  TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
  TIM_OCInitTypeDef TIM_OCInitStructure;

  print_info("motor_config\n");

  RCC_APB1PeriphClockCmd(MOTOR_PWM_TIMER_RCC, ENABLE);

  // motor enable
  RCC_APB2PeriphClockCmd(MOTOR_EN_RCC, ENABLE);
  GPIO_Config.GPIO_Pin = MOTOR_EN_PIN;
  GPIO_Config.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(MOTOR_EN, &GPIO_Config);
  GPIO_ResetBits(MOTOR_EN, MOTOR_EN_PIN);

  // motor left direction
  RCC_APB2PeriphClockCmd(MOTOR_LEFT_DIR_RCC, ENABLE);
  GPIO_Config.GPIO_Pin = MOTOR_LEFT_DIR_PIN;
  GPIO_Config.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(MOTOR_LEFT_DIR, &GPIO_Config);
  GPIO_ResetBits(MOTOR_LEFT_DIR, MOTOR_LEFT_DIR_PIN);

  // motor right direction
  RCC_APB2PeriphClockCmd(MOTOR_RIGHT_DIR_RCC, ENABLE);
  GPIO_Config.GPIO_Pin = MOTOR_RIGHT_DIR_PIN;
  GPIO_Config.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(MOTOR_RIGHT_DIR, &GPIO_Config);
  GPIO_ResetBits(MOTOR_RIGHT_DIR, MOTOR_RIGHT_DIR_PIN);

  // motor left pwm direction
  RCC_APB2PeriphClockCmd(MOTOR_LEFT_PWM_RCC, ENABLE);
  GPIO_Config.GPIO_Pin = MOTOR_LEFT_PWM_PIN;
  GPIO_Config.GPIO_Mode = GPIO_Mode_AF_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(MOTOR_LEFT_PWM, &GPIO_Config);

  // motor right pwm direction
  RCC_APB2PeriphClockCmd(MOTOR_RIGHT_PWM_RCC, ENABLE);
  GPIO_Config.GPIO_Pin = MOTOR_RIGHT_PWM_PIN;
  GPIO_Config.GPIO_Mode = GPIO_Mode_AF_PP;
  GPIO_Config.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(MOTOR_RIGHT_PWM, &GPIO_Config);

  /*
   * TIM2 Configuration: generate 2 PWM signals with 2 different duty cycles:
   * The TIM2CLK frequency is set to SystemCoreClock (Hz), to get TIM2 counter
   * clock at 24 MHz the Pre-scaler is computed as following:
   *  - Pre-scaler = (TIM2CLK / TIM2 counter clock) - 1
   * SystemCoreClock is set to 72 MHz for Low-density, Medium-density, High-density
   * and Connectivity line devices and to 24 MHz for Low-Density Value line and
   * Medium-Density Value line devices
   *
   * The TIM2 is running at 36 KHz: TIM2 Frequency = TIM2 counter clock/(ARR + 1) = 24 MHz / 666 = 36 KHz
   *     TIM2 Channel1 duty cycle = (TIM2_CCR1 / TIM2_ARR) * 100 = 50%
   *     TIM2 Channel2 duty cycle = (TIM2_CCR2 / TIM2_ARR) * 100 = 37.5%
   */

  // Compute the pre-scaler value
  prescalerValue = (uint16_t) (SystemCoreClock / 24000000) - 1;

  // Time base configuration
  TIM_TimeBaseStructInit(&TIM_TimeBaseStructure);
  TIM_TimeBaseStructure.TIM_Period = PWM_PERIOD;
  TIM_TimeBaseStructure.TIM_Prescaler = prescalerValue;
  TIM_TimeBaseStructure.TIM_ClockDivision = 0;
  TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;
  TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure);

  TIM_OCStructInit(&TIM_OCInitStructure);
  TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_PWM1;
  TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_High;
  TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;

  // PWM1 Mode configuration: Channel3
  TIM_OCInitStructure.TIM_Pulse = 0;
  TIM_OC3Init(TIM2, &TIM_OCInitStructure);
  TIM_OC3PreloadConfig(TIM2, TIM_OCPreload_Enable);

  // PWM1 Mode configuration: Channel4
  TIM_OCInitStructure.TIM_Pulse = 0;
  TIM_OC4Init(TIM2, &TIM_OCInitStructure);
  TIM_OC4PreloadConfig(TIM2, TIM_OCPreload_Enable);

  TIM_SelectOnePulseMode(TIM2, TIM_OPMode_Repetitive);
  TIM_ARRPreloadConfig(TIM2, ENABLE);
  TIM_Cmd(TIM2, ENABLE);
}

void motor_enable(int enable) {
  if (enable) {
    GPIO_SetBits(MOTOR_EN, MOTOR_EN_PIN);
    print_info("motor_enabled\n");
  } else {
    GPIO_ResetBits(MOTOR_EN, MOTOR_EN_PIN);
    print_info("motor_disabled\n");
  }
}

void motor_set_speed(int8_t speedLeft, int8_t speedRight) {
  if (speedLeft != lastSpeedLeft) {
    if (speedLeft > 0) {
      GPIO_SetBits(MOTOR_LEFT_DIR, MOTOR_LEFT_DIR_PIN);
    } else {
      GPIO_ResetBits(MOTOR_LEFT_DIR, MOTOR_LEFT_DIR_PIN);
    }
    TIM_SetCompare3(TIM2, speed_to_pwm_compare(speedLeft));
    lastSpeedLeft = speedLeft;
  }

  if (speedRight != lastSpeedRight) {
    if (speedRight > 0) {
      GPIO_SetBits(MOTOR_RIGHT_DIR, MOTOR_RIGHT_DIR_PIN);
    } else {
      GPIO_ResetBits(MOTOR_RIGHT_DIR, MOTOR_RIGHT_DIR_PIN);
    }
    TIM_SetCompare4(TIM2, speed_to_pwm_compare(speedRight));
    lastSpeedRight = speedRight;
  }
}

uint32_t speed_to_pwm_compare(int8_t speed) {
  uint32_t absSpeed = abs(speed);
  return absSpeed * (PWM_PERIOD / 128);
}
