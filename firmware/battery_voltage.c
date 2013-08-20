
#include "battery_voltage.h"
#include "platform_config.h"
#include "debug.h"
#include "util.h"

void battery_voltage_config() {
  GPIO_InitTypeDef GPIO_Config;
  ADC_InitTypeDef ADC_InitStructure;

  print_info("battery_voltage_config\n");

  RCC_APB2PeriphClockCmd(BATTERY_VOLTAGE_RCC, ENABLE);
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_ADC1, ENABLE);
  RCC_ADCCLKConfig(RCC_PCLK2_Div2);
  ADC_DeInit(ADC1);

  GPIO_StructInit(&GPIO_Config);
  GPIO_Config.GPIO_Pin = BATTERY_VOLTAGE_PIN;
  GPIO_Config.GPIO_Mode = GPIO_Mode_AIN;
  GPIO_Init(BATTERY_VOLTAGE, &GPIO_Config);

  ADC_StructInit(&ADC_InitStructure);
  ADC_InitStructure.ADC_Mode = ADC_Mode_Independent;
  ADC_InitStructure.ADC_ScanConvMode = DISABLE;
  ADC_InitStructure.ADC_ContinuousConvMode = DISABLE;
  ADC_InitStructure.ADC_ExternalTrigConv = ADC_ExternalTrigConv_None;
  ADC_InitStructure.ADC_DataAlign = ADC_DataAlign_Right;
  ADC_InitStructure.ADC_NbrOfChannel = 1;
  ADC_Init(ADC1, &ADC_InitStructure);

  ADC_Cmd(ADC1, ENABLE);

  ADC_ResetCalibration(ADC1);
  while (ADC_GetResetCalibrationStatus(ADC1));

  ADC_StartCalibration(ADC1);
  while (ADC_GetCalibrationStatus(ADC1));
}

uint32_t battery_voltage_read() {
  ADC_ClearFlag(ADC1, ADC_FLAG_EOC);
  ADC_RegularChannelConfig(ADC1, BATTERY_VOLTAGE_ADC_CH, 1, ADC_SampleTime_239Cycles5);
  ADC_SoftwareStartConvCmd(ADC1, ENABLE);

  while (ADC_GetFlagStatus(ADC1, ADC_FLAG_EOC) == RESET);
  return ADC_GetConversionValue(ADC1);
}