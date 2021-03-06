EESchema Schematic File Version 2
LIBS:power
LIBS:telepresenceRobot
LIBS:userFavorites
LIBS:main-cache
EELAYER 24 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 2 4
Title ""
Date ""
Rev ""
Comp ""
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
Text HLabel 4900 4000 2    60   Output ~ 0
VIN+
Text Label 4650 4000 0    60   ~ 0
VIN+
Text Notes 800  3750 0    40   ~ 0
Float Charge = 13.5 - 13.8V per cell = 27 - 27.6V\nCycle use charge = 14.6 - 15.0V per cell = 29.2 - 30V\nVth = 10.5V per cell = 21V\n\nImax charge = 2100mA\nNOTE: Compontent selection will not allow\n     this so we will choose a much smaller value 500mA?\n\nBD242CG hfemin = 20\n\nRc = 2.3V / 50uA = 46k prefered\n\nVfloat = 2.3V * (Ra + Rb + Rc) / Rc\n   -> Ra + Rb = (27.3 * 46k / 2.3) - 46k\n   -> Ra + Rb = 500k\n\nVboost = Vref * (Ra + Rb + Rc//Rd) / Rc//Rd\n    -> 29.6 = 2.3 * (500k + 46k//Rd) / 46k//Rd\n    -> 12.87 = (500k / 46k//Rd) + 1\n    -> 11.87 = 500k / 46k//Rd\n    -> 46k//Rd = 42,124.5\n    -> (46k * Rd) / (46k + Rd) = 42,124.5\n    -> 46k * Rd = 1,937,727,000 + (42,124.5 * Rd)\n    -> 3,875.5 * Rd = 1,937,727,000\n    -> Rd = 499,994 = 500k prefered\n\nVth = Vref * (Ra + Rb + Rc//Rd) / (Rb + Rc//Rd)\n    -> 21 = 2.3 * (Ra + Rb + 42,124.5) / (Rb + 42,124.5)\n    -> (9.13 * Rb) + 384,615 = 500k + 42,124.5\n    -> 9.13 * Rb = 157,509.5\n    -> Rb = 17,251.86 = 16.9k prefered\n\nRa + Rb = 500k\n    -> Ra = 483,100 = 487k prefered\n\nIpre = (Vin - Vpre - Vdext - Vbat) / Rt\n    -> 10mA = (30 - 2 - 0.7 - 21) / Rt\n    -> 10mA * Rt = 6.3\n    -> Rt = 630 = 649 prefered\n\nImax = Vilim / Risns\n    -> 0.5 = 250mV / Risns\n    -> Risns = 0.5\n\nRp = (Vinmin - 2) / Imax * hfemin\n    -> Rp = (30 - 2) / 500mA * 20\n    -> Rp = 1,120 = 1.1k prefered\n
Text Notes 3850 3650 0    60   ~ 0
Vin > 30V
$Comp
L GND #PWR029
U 1 1 51EDC6BD
P 7700 2800
F 0 "#PWR029" H 7700 2800 30  0001 C CNN
F 1 "GND" H 7700 2730 30  0001 C CNN
F 2 "" H 7700 2800 60  0000 C CNN
F 3 "" H 7700 2800 60  0000 C CNN
	1    7700 2800
	1    0    0    -1  
$EndComp
$Comp
L +24V #PWR030
U 1 1 51EDC722
P 6000 1200
F 0 "#PWR030" H 6000 1150 20  0001 C CNN
F 1 "+24V" H 6000 1300 30  0000 C CNN
F 2 "" H 6000 1200 60  0000 C CNN
F 3 "" H 6000 1200 60  0000 C CNN
	1    6000 1200
	1    0    0    -1  
$EndComp
$Comp
L CPSMALL C12
U 1 1 51EDC914
P 950 1600
F 0 "C12" H 975 1650 30  0000 L CNN
F 1 "100uF 35V" H 975 1550 30  0000 L CNN
F 2 "" H 950 1600 60  0000 C CNN
F 3 "" H 950 1600 60  0000 C CNN
	1    950  1600
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR031
U 1 1 51EDC971
P 950 1800
F 0 "#PWR031" H 950 1800 30  0001 C CNN
F 1 "GND" H 950 1730 30  0001 C CNN
F 2 "" H 950 1800 60  0000 C CNN
F 3 "" H 950 1800 60  0000 C CNN
	1    950  1800
	1    0    0    -1  
$EndComp
$Comp
L CSMALL C13
U 1 1 51EDC9BC
P 1400 1600
F 0 "C13" H 1425 1650 30  0000 L CNN
F 1 "0.1uF 35V" H 1425 1550 30  0000 L CNN
F 2 "" H 1400 1600 60  0000 C CNN
F 3 "" H 1400 1600 60  0000 C CNN
	1    1400 1600
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR032
U 1 1 51EDCA46
P 1400 1800
F 0 "#PWR032" H 1400 1800 30  0001 C CNN
F 1 "GND" H 1400 1730 30  0001 C CNN
F 2 "" H 1400 1800 60  0000 C CNN
F 3 "" H 1400 1800 60  0000 C CNN
	1    1400 1800
	1    0    0    -1  
$EndComp
$Comp
L INDUCTORSMALL L1
U 1 1 51EDD3F6
P 10100 1700
F 0 "L1" H 10100 1600 60  0000 C CNN
F 1 "68uH" H 10100 1800 60  0000 C CNN
F 2 "" H 10100 1700 60  0000 C CNN
F 3 "" H 10100 1700 60  0000 C CNN
	1    10100 1700
	-1   0    0    1   
$EndComp
$Comp
L +5V #PWR033
U 1 1 51EDD7D1
P 10950 1550
F 0 "#PWR033" H 10950 1640 20  0001 C CNN
F 1 "+5V" H 10950 1640 30  0000 C CNN
F 2 "" H 10950 1550 60  0000 C CNN
F 3 "" H 10950 1550 60  0000 C CNN
	1    10950 1550
	1    0    0    -1  
$EndComp
$Comp
L PWR_FLAG #FLG034
U 1 1 51EDE0AB
P 10750 1600
F 0 "#FLG034" H 10750 1695 30  0001 C CNN
F 1 "PWR_FLAG" H 10750 1780 30  0000 C CNN
F 2 "" H 10750 1600 60  0000 C CNN
F 3 "" H 10750 1600 60  0000 C CNN
	1    10750 1600
	1    0    0    -1  
$EndComp
$Comp
L CPSMALL C21
U 1 1 51EDEB39
P 10650 2250
F 0 "C21" H 10675 2300 30  0000 L CNN
F 1 "100uF" H 10675 2200 30  0000 L CNN
F 2 "" H 10650 2250 60  0000 C CNN
F 3 "" H 10650 2250 60  0000 C CNN
	1    10650 2250
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR035
U 1 1 51EDEB3F
P 10650 2800
F 0 "#PWR035" H 10650 2800 30  0001 C CNN
F 1 "GND" H 10650 2730 30  0001 C CNN
F 2 "" H 10650 2800 60  0000 C CNN
F 3 "" H 10650 2800 60  0000 C CNN
	1    10650 2800
	1    0    0    -1  
$EndComp
Text HLabel 6350 2200 2    60   Output ~ 0
BATCENTER
Wire Wire Line
	4400 4000 4900 4000
Wire Wire Line
	4400 4100 4500 4100
Wire Wire Line
	950  1500 950  1350
Connection ~ 950  1350
Wire Wire Line
	950  1700 950  1800
Wire Wire Line
	1400 1350 1400 1500
Connection ~ 1400 1350
Wire Wire Line
	1400 1700 1400 1800
Wire Wire Line
	10950 1700 10950 1550
Wire Wire Line
	10750 1700 10750 1600
Connection ~ 10750 1700
Wire Wire Line
	10650 2350 10650 2800
Wire Wire Line
	10650 1700 10650 2150
Connection ~ 10650 1700
Wire Wire Line
	2700 1350 2950 1350
Wire Wire Line
	2750 1700 2750 1350
Connection ~ 2750 1350
Wire Wire Line
	2850 1700 2850 1350
Connection ~ 2850 1350
Wire Wire Line
	2450 1350 2450 1700
Wire Wire Line
	600  1350 2500 1350
Wire Wire Line
	3150 1700 3150 1600
Wire Wire Line
	1900 2100 1750 2100
Wire Wire Line
	1750 2100 1750 1350
Connection ~ 2450 1350
$Comp
L GND #PWR036
U 1 1 51F55868
P 1600 3350
F 0 "#PWR036" H 1600 3350 30  0001 C CNN
F 1 "GND" H 1600 3280 30  0001 C CNN
F 2 "" H 1600 3350 60  0000 C CNN
F 3 "" H 1600 3350 60  0000 C CNN
	1    1600 3350
	1    0    0    -1  
$EndComp
$Comp
L CSMALL C14
U 1 1 51F5586E
P 1600 3150
F 0 "C14" H 1625 3200 30  0000 L CNN
F 1 "0.1uF" H 1625 3100 30  0000 L CNN
F 2 "" H 1600 3150 60  0000 C CNN
F 3 "" H 1600 3150 60  0000 C CNN
	1    1600 3150
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR037
U 1 1 51F5588F
P 1800 3350
F 0 "#PWR037" H 1800 3350 30  0001 C CNN
F 1 "GND" H 1800 3280 30  0001 C CNN
F 2 "" H 1800 3350 60  0000 C CNN
F 3 "" H 1800 3350 60  0000 C CNN
	1    1800 3350
	1    0    0    -1  
$EndComp
Wire Wire Line
	1900 2900 1600 2900
Wire Wire Line
	1600 2900 1600 3050
Wire Wire Line
	1900 3000 1800 3000
Wire Wire Line
	1800 3000 1800 3350
Wire Wire Line
	1600 3250 1600 3350
Wire Wire Line
	4400 1350 4400 1500
$Comp
L RSMALL R14
U 1 1 51F55B90
P 2600 1350
F 0 "R14" V 2545 1350 30  0000 C CNN
F 1 "0.4" V 2655 1350 30  0000 C CNN
F 2 "" H 2600 1350 60  0000 C CNN
F 3 "" H 2600 1350 60  0000 C CNN
	1    2600 1350
	0    -1   -1   0   
$EndComp
$Comp
L RSMALL R16
U 1 1 51F55BAC
P 4400 1600
F 0 "R16" V 4345 1600 30  0000 C CNN
F 1 "590R" V 4455 1600 30  0000 C CNN
F 2 "" H 4400 1600 60  0000 C CNN
F 3 "" H 4400 1600 60  0000 C CNN
	1    4400 1600
	-1   0    0    1   
$EndComp
$Comp
L RSMALL R18
U 1 1 51F55BB2
P 4900 2050
F 0 "R18" V 4845 2050 30  0000 C CNN
F 1 "???" V 4955 2050 30  0000 C CNN
F 2 "" H 4900 2050 60  0000 C CNN
F 3 "" H 4900 2050 60  0000 C CNN
	1    4900 2050
	-1   0    0    1   
$EndComp
$Comp
L RSMALL R19
U 1 1 51F55BB8
P 4900 2350
F 0 "R19" V 4845 2350 30  0000 C CNN
F 1 "???" V 4955 2350 30  0000 C CNN
F 2 "" H 4900 2350 60  0000 C CNN
F 3 "" H 4900 2350 60  0000 C CNN
	1    4900 2350
	-1   0    0    1   
$EndComp
$Comp
L RSMALL R20
U 1 1 51F55BF9
P 4900 2950
F 0 "R20" V 4845 2950 30  0000 C CNN
F 1 "46K" V 4955 2950 30  0000 C CNN
F 2 "" H 4900 2950 60  0000 C CNN
F 3 "" H 4900 2950 60  0000 C CNN
	1    4900 2950
	-1   0    0    1   
$EndComp
Wire Wire Line
	1900 2400 1750 2400
Wire Wire Line
	1750 2400 1750 2500
Wire Wire Line
	1750 2500 1900 2500
$Comp
L BQ24450 U2
U 1 1 51F55FCA
P 2200 2350
F 0 "U2" H 2200 1550 60  0000 L CNN
F 1 "BQ24450" H 3600 1550 60  0000 L CNN
F 2 "" H 2200 2350 60  0000 C CNN
F 3 "" H 2200 2350 60  0000 C CNN
	1    2200 2350
	1    0    0    -1  
$EndComp
$Comp
L RSMALL R17
U 1 1 51F55FEB
P 4600 2800
F 0 "R17" V 4545 2800 30  0000 C CNN
F 1 "???" V 4655 2800 30  0000 C CNN
F 2 "" H 4600 2800 60  0000 C CNN
F 3 "" H 4600 2800 60  0000 C CNN
	1    4600 2800
	0    1    1    0   
$EndComp
Wire Wire Line
	4900 1350 4900 1950
Connection ~ 4400 1350
$Comp
L BATTERY BT1
U 1 1 51F56720
P 5650 1800
F 0 "BT1" H 5650 2000 50  0000 C CNN
F 1 "BATTERY" H 5650 1610 50  0000 C CNN
F 2 "" H 5650 1800 60  0000 C CNN
F 3 "" H 5650 1800 60  0000 C CNN
	1    5650 1800
	0    1    1    0   
$EndComp
$Comp
L BATTERY BT2
U 1 1 51F5672D
P 5650 2600
F 0 "BT2" H 5650 2800 50  0000 C CNN
F 1 "BATTERY" H 5650 2410 50  0000 C CNN
F 2 "" H 5650 2600 60  0000 C CNN
F 3 "" H 5650 2600 60  0000 C CNN
	1    5650 2600
	0    1    1    0   
$EndComp
Connection ~ 4900 1350
Wire Wire Line
	5650 1350 5650 1500
Connection ~ 5650 1350
Wire Wire Line
	5650 2100 5650 2300
Wire Wire Line
	5650 2200 6350 2200
Connection ~ 5650 2200
Wire Wire Line
	5650 2900 5650 3200
$Comp
L GND #PWR038
U 1 1 51F56975
P 5650 3200
F 0 "#PWR038" H 5650 3200 30  0001 C CNN
F 1 "GND" H 5650 3130 30  0001 C CNN
F 2 "" H 5650 3200 60  0000 C CNN
F 3 "" H 5650 3200 60  0000 C CNN
	1    5650 3200
	1    0    0    -1  
$EndComp
Connection ~ 1750 1350
Text Label 600  1350 0    60   ~ 0
VIN+
$Comp
L GND #PWR039
U 1 1 51F56B11
P 4500 4300
F 0 "#PWR039" H 4500 4300 30  0001 C CNN
F 1 "GND" H 4500 4230 30  0001 C CNN
F 2 "" H 4500 4300 60  0000 C CNN
F 3 "" H 4500 4300 60  0000 C CNN
	1    4500 4300
	1    0    0    -1  
$EndComp
Wire Wire Line
	4500 4100 4500 4300
$Comp
L +12V #PWR040
U 1 1 51F56F09
P 6000 2050
F 0 "#PWR040" H 6000 2000 20  0001 C CNN
F 1 "+12V" H 6000 2150 30  0000 C CNN
F 2 "" H 6000 2050 60  0000 C CNN
F 3 "" H 6000 2050 60  0000 C CNN
	1    6000 2050
	1    0    0    -1  
$EndComp
Wire Wire Line
	6000 1350 6000 1200
Connection ~ 6000 1350
Wire Wire Line
	6000 2050 6000 2200
Connection ~ 6000 2200
$Comp
L +12V #PWR041
U 1 1 51F57319
P 7700 1300
F 0 "#PWR041" H 7700 1250 20  0001 C CNN
F 1 "+12V" H 7700 1400 30  0000 C CNN
F 2 "" H 7700 1300 60  0000 C CNN
F 3 "" H 7700 1300 60  0000 C CNN
	1    7700 1300
	1    0    0    -1  
$EndComp
$Comp
L PWR_FLAG #FLG042
U 1 1 51F5740D
P 6250 2100
F 0 "#FLG042" H 6250 2195 30  0001 C CNN
F 1 "PWR_FLAG" H 6250 2280 30  0000 C CNN
F 2 "" H 6250 2100 60  0000 C CNN
F 3 "" H 6250 2100 60  0000 C CNN
	1    6250 2100
	1    0    0    -1  
$EndComp
$Comp
L PWR_FLAG #FLG043
U 1 1 51F57424
P 6250 1250
F 0 "#FLG043" H 6250 1345 30  0001 C CNN
F 1 "PWR_FLAG" H 6250 1430 30  0000 C CNN
F 2 "" H 6250 1250 60  0000 C CNN
F 3 "" H 6250 1250 60  0000 C CNN
	1    6250 1250
	1    0    0    -1  
$EndComp
Wire Wire Line
	6250 1350 6250 1250
Wire Wire Line
	6250 2100 6250 2200
Connection ~ 6250 2200
NoConn ~ 4300 2900
$Comp
L RSMALL R15
U 1 1 51F5C7CA
P 3750 1650
F 0 "R15" V 3695 1650 30  0000 C CNN
F 1 "760" V 3805 1650 30  0000 C CNN
F 2 "" H 3750 1650 60  0000 C CNN
F 3 "" H 3750 1650 60  0000 C CNN
	1    3750 1650
	-1   0    0    1   
$EndComp
$Comp
L GND #PWR044
U 1 1 51F5C7D0
P 3750 1850
F 0 "#PWR044" H 3750 1850 30  0001 C CNN
F 1 "GND" H 3750 1780 30  0001 C CNN
F 2 "" H 3750 1850 60  0000 C CNN
F 3 "" H 3750 1850 60  0000 C CNN
	1    3750 1850
	1    0    0    -1  
$EndComp
Wire Wire Line
	3750 1750 3750 1850
Wire Wire Line
	3750 1550 3750 1450
Wire Wire Line
	3750 1450 3450 1450
Wire Wire Line
	3450 1450 3450 1700
Text Notes 3800 1550 0    60   ~ 0
Rp
$Comp
L PNP Q2
U 1 1 51F5CB56
P 3150 1400
F 0 "Q2" V 3450 1500 60  0000 C CNN
F 1 "BD242CG" V 3350 1350 60  0000 C CNN
F 2 "" H 3150 1400 60  0000 C CNN
F 3 "" H 3150 1400 60  0000 C CNN
	1    3150 1400
	0    -1   -1   0   
$EndComp
Text Notes 1200 3050 0    60   ~ 0
Ccomp
$Comp
L DIODE D1
U 1 1 51F5CF18
P 4650 1350
F 0 "D1" H 4650 1450 40  0000 C CNN
F 1 "DIODE" H 4650 1250 40  0000 C CNN
F 2 "" H 4650 1350 60  0000 C CNN
F 3 "" H 4650 1350 60  0000 C CNN
	1    4650 1350
	1    0    0    -1  
$EndComp
Wire Wire Line
	3350 1350 4450 1350
Text Notes 4450 1750 0    60   ~ 0
Rt
Text Notes 5000 2100 0    60   ~ 0
Ra
Text Notes 5000 2400 0    60   ~ 0
Rb
Text Notes 4550 2700 0    60   ~ 0
Rd
Wire Wire Line
	4300 2100 4400 2100
Wire Wire Line
	4400 2100 4400 1700
Wire Wire Line
	4300 2200 4900 2200
Wire Wire Line
	4900 2150 4900 2250
Connection ~ 4900 2200
Wire Wire Line
	4900 2450 4900 2850
Wire Wire Line
	4900 2500 4300 2500
Wire Wire Line
	4300 2800 4500 2800
Wire Wire Line
	4900 2800 4700 2800
Connection ~ 4900 2500
Connection ~ 4900 2800
Wire Wire Line
	4900 3050 4900 3150
Wire Wire Line
	4900 3150 4700 3150
Wire Wire Line
	4700 3150 4700 3000
Wire Wire Line
	4700 3000 4300 3000
Text Notes 5000 3000 0    60   ~ 0
Rc
$Comp
L VREG_VOUTCENTER U3
U 1 1 5206C05A
P 8200 5450
F 0 "U3" H 8350 5254 60  0000 C CNN
F 1 "ZLDO1117" H 8200 5650 60  0000 C CNN
F 2 "" H 8200 5450 60  0000 C CNN
F 3 "" H 8200 5450 60  0000 C CNN
	1    8200 5450
	1    0    0    -1  
$EndComp
$Comp
L +5V #PWR045
U 1 1 5206C067
P 7550 5200
F 0 "#PWR045" H 7550 5290 20  0001 C CNN
F 1 "+5V" H 7550 5290 30  0000 C CNN
F 2 "" H 7550 5200 60  0000 C CNN
F 3 "" H 7550 5200 60  0000 C CNN
	1    7550 5200
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR046
U 1 1 5206C06D
P 8200 5800
F 0 "#PWR046" H 8200 5800 30  0001 C CNN
F 1 "GND" H 8200 5730 30  0001 C CNN
F 2 "" H 8200 5800 60  0000 C CNN
F 3 "" H 8200 5800 60  0000 C CNN
	1    8200 5800
	1    0    0    -1  
$EndComp
Wire Wire Line
	7800 5400 7550 5400
Wire Wire Line
	7550 5400 7550 5200
Wire Wire Line
	8200 5700 8200 5800
$Comp
L +3.3V #PWR047
U 1 1 5206C156
P 9100 5200
F 0 "#PWR047" H 9100 5160 30  0001 C CNN
F 1 "+3.3V" H 9100 5310 30  0000 C CNN
F 2 "" H 9100 5200 60  0000 C CNN
F 3 "" H 9100 5200 60  0000 C CNN
	1    9100 5200
	1    0    0    -1  
$EndComp
Wire Wire Line
	8600 5400 9100 5400
NoConn ~ 8600 5500
$Comp
L CPSMALL C15
U 1 1 5206CE7D
P 8850 5600
F 0 "C15" H 8875 5650 30  0000 L CNN
F 1 "100uF" H 8875 5550 30  0000 L CNN
F 2 "" H 8850 5600 60  0000 C CNN
F 3 "" H 8850 5600 60  0000 C CNN
	1    8850 5600
	1    0    0    -1  
$EndComp
$Comp
L CSMALL C16
U 1 1 5206CE9B
P 9100 5600
F 0 "C16" H 9125 5650 30  0000 L CNN
F 1 "0.1uF" H 9125 5550 30  0000 L CNN
F 2 "" H 9100 5600 60  0000 C CNN
F 3 "" H 9100 5600 60  0000 C CNN
	1    9100 5600
	1    0    0    -1  
$EndComp
Wire Wire Line
	9100 5200 9100 5500
Wire Wire Line
	8850 5500 8850 5400
Connection ~ 8850 5400
Connection ~ 9100 5400
$Comp
L LED D2
U 1 1 5206D1C0
P 6650 6950
F 0 "D2" H 6650 7050 50  0000 C CNN
F 1 "PWR" H 6650 6850 50  0000 C CNN
F 2 "" H 6650 6950 60  0000 C CNN
F 3 "" H 6650 6950 60  0000 C CNN
	1    6650 6950
	0    1    1    0   
$EndComp
$Comp
L GND #PWR048
U 1 1 5206D1CD
P 6650 7250
F 0 "#PWR048" H 6650 7250 30  0001 C CNN
F 1 "GND" H 6650 7180 30  0001 C CNN
F 2 "" H 6650 7250 60  0000 C CNN
F 3 "" H 6650 7250 60  0000 C CNN
	1    6650 7250
	1    0    0    -1  
$EndComp
$Comp
L RSMALL R21
U 1 1 5206D26E
P 6650 6550
F 0 "R21" V 6595 6550 30  0000 C CNN
F 1 "100R" V 6750 6550 30  0000 C CNN
F 2 "" H 6650 6550 60  0000 C CNN
F 3 "" H 6650 6550 60  0000 C CNN
	1    6650 6550
	1    0    0    -1  
$EndComp
Wire Wire Line
	6650 6650 6650 6750
Wire Wire Line
	6650 7150 6650 7250
$Comp
L +3.3V #PWR049
U 1 1 5206D36D
P 6650 6300
F 0 "#PWR049" H 6650 6260 30  0001 C CNN
F 1 "+3.3V" H 6650 6410 30  0000 C CNN
F 2 "" H 6650 6300 60  0000 C CNN
F 3 "" H 6650 6300 60  0000 C CNN
	1    6650 6300
	1    0    0    -1  
$EndComp
Wire Wire Line
	6650 6300 6650 6450
$Comp
L GND #PWR050
U 1 1 5206D3EC
P 8850 5800
F 0 "#PWR050" H 8850 5800 30  0001 C CNN
F 1 "GND" H 8850 5730 30  0001 C CNN
F 2 "" H 8850 5800 60  0000 C CNN
F 3 "" H 8850 5800 60  0000 C CNN
	1    8850 5800
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR051
U 1 1 5206D3F2
P 9100 5800
F 0 "#PWR051" H 9100 5800 30  0001 C CNN
F 1 "GND" H 9100 5730 30  0001 C CNN
F 2 "" H 9100 5800 60  0000 C CNN
F 3 "" H 9100 5800 60  0000 C CNN
	1    9100 5800
	1    0    0    -1  
$EndComp
Wire Wire Line
	9100 5700 9100 5800
Wire Wire Line
	8850 5700 8850 5800
$Comp
L CONN_4 P6
U 1 1 5206B9B3
P 10200 4400
F 0 "P6" V 10150 4400 50  0000 C CNN
F 1 "POWER" V 10250 4400 50  0000 C CNN
F 2 "" H 10200 4400 60  0000 C CNN
F 3 "" H 10200 4400 60  0000 C CNN
	1    10200 4400
	1    0    0    -1  
$EndComp
$Comp
L GND #PWR052
U 1 1 5206B9C0
P 9750 4650
F 0 "#PWR052" H 9750 4650 30  0001 C CNN
F 1 "GND" H 9750 4580 30  0001 C CNN
F 2 "" H 9750 4650 60  0000 C CNN
F 3 "" H 9750 4650 60  0000 C CNN
	1    9750 4650
	1    0    0    -1  
$EndComp
Wire Wire Line
	9850 4550 9750 4550
Wire Wire Line
	9750 4550 9750 4650
$Comp
L +5V #PWR053
U 1 1 5206BA42
P 9600 4100
F 0 "#PWR053" H 9600 4190 20  0001 C CNN
F 1 "+5V" H 9600 4190 30  0000 C CNN
F 2 "" H 9600 4100 60  0000 C CNN
F 3 "" H 9600 4100 60  0000 C CNN
	1    9600 4100
	1    0    0    -1  
$EndComp
$Comp
L +24V #PWR054
U 1 1 5206BA48
P 9750 4100
F 0 "#PWR054" H 9750 4050 20  0001 C CNN
F 1 "+24V" H 9750 4200 30  0000 C CNN
F 2 "" H 9750 4100 60  0000 C CNN
F 3 "" H 9750 4100 60  0000 C CNN
	1    9750 4100
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR055
U 1 1 5206BA4E
P 9450 4100
F 0 "#PWR055" H 9450 4060 30  0001 C CNN
F 1 "+3.3V" H 9450 4210 30  0000 C CNN
F 2 "" H 9450 4100 60  0000 C CNN
F 3 "" H 9450 4100 60  0000 C CNN
	1    9450 4100
	1    0    0    -1  
$EndComp
Wire Wire Line
	9850 4450 9450 4450
Wire Wire Line
	9450 4450 9450 4100
Wire Wire Line
	9850 4350 9600 4350
Wire Wire Line
	9600 4350 9600 4100
Wire Wire Line
	9850 4250 9750 4250
Wire Wire Line
	9750 4250 9750 4100
$Comp
L P02 P5
U 1 1 5206DA5F
P 4200 4200
F 0 "P5" H 4250 4250 60  0000 C CNN
F 1 "POWER-CHARGE" H 4000 3850 60  0000 L CNN
F 2 "" H 4200 4200 60  0000 C CNN
F 3 "" H 4200 4200 60  0000 C CNN
	1    4200 4200
	-1   0    0    1   
$EndComp
$Comp
L GND #PWR056
U 1 1 5206E43C
P 8150 4650
F 0 "#PWR056" H 8150 4650 30  0001 C CNN
F 1 "GND" H 8150 4580 30  0001 C CNN
F 2 "" H 8150 4650 60  0000 C CNN
F 3 "" H 8150 4650 60  0000 C CNN
	1    8150 4650
	1    0    0    -1  
$EndComp
$Comp
L +12V #PWR057
U 1 1 5206E561
P 7900 4100
F 0 "#PWR057" H 7900 4050 20  0001 C CNN
F 1 "+12V" H 7900 4200 30  0000 C CNN
F 2 "" H 7900 4100 60  0000 C CNN
F 3 "" H 7900 4100 60  0000 C CNN
	1    7900 4100
	1    0    0    -1  
$EndComp
$Comp
L +24V #PWR058
U 1 1 5206E567
P 8100 4100
F 0 "#PWR058" H 8100 4050 20  0001 C CNN
F 1 "+24V" H 8100 4200 30  0000 C CNN
F 2 "" H 8100 4100 60  0000 C CNN
F 3 "" H 8100 4100 60  0000 C CNN
	1    8100 4100
	1    0    0    -1  
$EndComp
Wire Wire Line
	8250 4250 8100 4250
Wire Wire Line
	8100 4250 8100 4100
Wire Wire Line
	4850 1350 5250 1350
Wire Wire Line
	5250 1350 5250 1250
Wire Wire Line
	5350 1250 5350 1350
Wire Wire Line
	5350 1350 6250 1350
$Comp
L CSMALL C19
U 1 1 51EDCC5A
P 9800 1550
F 0 "C19" H 9825 1600 30  0000 L CNN
F 1 "0.1uF" H 9825 1500 30  0000 L CNN
F 2 "" H 9800 1550 60  0000 C CNN
F 3 "" H 9800 1550 60  0000 C CNN
	1    9800 1550
	1    0    0    -1  
$EndComp
$Comp
L AP6502 U4
U 1 1 52143251
P 8900 1750
F 0 "U4" H 8850 3350 60  0000 C CNN
F 1 "AP6502" H 8850 1200 60  0000 C CNN
F 2 "" H 8900 1750 60  0000 C CNN
F 3 "" H 8900 1750 60  0000 C CNN
	1    8900 1750
	1    0    0    -1  
$EndComp
$Comp
L RSMALL R24
U 1 1 521434E8
P 10350 1850
F 0 "R24" V 10295 1850 30  0000 C CNN
F 1 "45.3k" V 10405 1850 30  0000 C CNN
F 2 "" H 10350 1850 60  0000 C CNN
F 3 "" H 10350 1850 60  0000 C CNN
	1    10350 1850
	-1   0    0    1   
$EndComp
$Comp
L RSMALL R25
U 1 1 52143507
P 10350 2150
F 0 "R25" V 10295 2150 30  0000 C CNN
F 1 "10k" V 10405 2150 30  0000 C CNN
F 2 "" H 10350 2150 60  0000 C CNN
F 3 "" H 10350 2150 60  0000 C CNN
	1    10350 2150
	-1   0    0    1   
$EndComp
$Comp
L CSMALL C20
U 1 1 5214352B
P 9800 2250
F 0 "C20" H 9825 2300 30  0000 L CNN
F 1 "6.8nF" H 9825 2200 30  0000 L CNN
F 2 "" H 9800 2250 60  0000 C CNN
F 3 "" H 9800 2250 60  0000 C CNN
	1    9800 2250
	1    0    0    -1  
$EndComp
$Comp
L RSMALL R23
U 1 1 52143531
P 9800 2550
F 0 "R23" V 9745 2550 30  0000 C CNN
F 1 "6.8k" V 9855 2550 30  0000 C CNN
F 2 "" H 9800 2550 60  0000 C CNN
F 3 "" H 9800 2550 60  0000 C CNN
	1    9800 2550
	-1   0    0    1   
$EndComp
$Comp
L CPSMALL C17
U 1 1 5214354B
P 7700 2550
F 0 "C17" H 7725 2600 30  0000 L CNN
F 1 "100uF 15V" H 7400 2500 30  0000 L CNN
F 2 "" H 7700 2550 60  0000 C CNN
F 3 "" H 7700 2550 60  0000 C CNN
	1    7700 2550
	1    0    0    -1  
$EndComp
Wire Wire Line
	7700 1300 7700 2450
Wire Wire Line
	7500 1400 8200 1400
Wire Wire Line
	7700 2650 7700 2800
$Comp
L GND #PWR059
U 1 1 52143AC9
P 8100 2800
F 0 "#PWR059" H 8100 2800 30  0001 C CNN
F 1 "GND" H 8100 2730 30  0001 C CNN
F 2 "" H 8100 2800 60  0000 C CNN
F 3 "" H 8100 2800 60  0000 C CNN
	1    8100 2800
	1    0    0    -1  
$EndComp
Wire Wire Line
	8200 2100 8100 2100
Wire Wire Line
	8100 2100 8100 2800
Connection ~ 7700 1400
$Comp
L CSMALL C18
U 1 1 52143B61
P 7900 2550
F 0 "C18" H 7925 2600 30  0000 L CNN
F 1 "0.1uF" H 7925 2500 30  0000 L CNN
F 2 "" H 7900 2550 60  0000 C CNN
F 3 "" H 7900 2550 60  0000 C CNN
	1    7900 2550
	1    0    0    -1  
$EndComp
Wire Wire Line
	8200 1900 7900 1900
Wire Wire Line
	7900 1900 7900 2450
$Comp
L GND #PWR060
U 1 1 52143C72
P 7900 2800
F 0 "#PWR060" H 7900 2800 30  0001 C CNN
F 1 "GND" H 7900 2730 30  0001 C CNN
F 2 "" H 7900 2800 60  0000 C CNN
F 3 "" H 7900 2800 60  0000 C CNN
	1    7900 2800
	1    0    0    -1  
$EndComp
Wire Wire Line
	7900 2650 7900 2800
Wire Wire Line
	9550 1400 9800 1400
Wire Wire Line
	9800 1400 9800 1450
Wire Wire Line
	9550 1700 9950 1700
Wire Wire Line
	9800 1700 9800 1650
Connection ~ 9800 1700
Wire Wire Line
	10350 1700 10350 1750
Wire Wire Line
	10350 1950 10350 2050
Wire Wire Line
	10350 2250 10350 2800
$Comp
L GND #PWR061
U 1 1 52143FBE
P 10350 2800
F 0 "#PWR061" H 10350 2800 30  0001 C CNN
F 1 "GND" H 10350 2730 30  0001 C CNN
F 2 "" H 10350 2800 60  0000 C CNN
F 3 "" H 10350 2800 60  0000 C CNN
	1    10350 2800
	1    0    0    -1  
$EndComp
Wire Wire Line
	9550 2000 10350 2000
Connection ~ 10350 2000
Wire Wire Line
	9550 2100 9800 2100
Wire Wire Line
	9800 2100 9800 2150
Wire Wire Line
	9800 2350 9800 2450
$Comp
L GND #PWR062
U 1 1 521441C4
P 9800 2800
F 0 "#PWR062" H 9800 2800 30  0001 C CNN
F 1 "GND" H 9800 2730 30  0001 C CNN
F 2 "" H 9800 2800 60  0000 C CNN
F 3 "" H 9800 2800 60  0000 C CNN
	1    9800 2800
	1    0    0    -1  
$EndComp
Wire Wire Line
	9800 2650 9800 2800
Connection ~ 10350 1700
$Comp
L RSMALL R22
U 1 1 5214486B
P 7900 1600
F 0 "R22" V 7845 1600 30  0000 C CNN
F 1 "100k" V 7955 1600 30  0000 C CNN
F 2 "" H 7900 1600 60  0000 C CNN
F 3 "" H 7900 1600 60  0000 C CNN
	1    7900 1600
	-1   0    0    1   
$EndComp
Wire Wire Line
	8200 1800 7900 1800
Wire Wire Line
	7900 1800 7900 1700
Wire Wire Line
	7900 1500 7900 1400
Connection ~ 7900 1400
Text Notes 8050 3300 0    60   ~ 0
L = ( Vout * ( Vin - Vout ) ) / ( Vin * dIL * fsw )\nL = ( 5V * ( 12V - 5V ) ) / ( 5V * 0.3 * 340kHz )\nL = 35 / 510,000 = 68uH
$Comp
L P02 P8
U 1 1 52145BAB
P 7300 1600
F 0 "P8" H 7350 1650 60  0000 C CNN
F 1 "12VIN" H 7300 1250 60  0000 L CNN
F 2 "" H 7300 1600 60  0000 C CNN
F 3 "" H 7300 1600 60  0000 C CNN
	1    7300 1600
	-1   0    0    1   
$EndComp
$Comp
L GND #PWR063
U 1 1 52145C57
P 7550 1600
F 0 "#PWR063" H 7550 1600 30  0001 C CNN
F 1 "GND" H 7550 1530 30  0001 C CNN
F 2 "" H 7550 1600 60  0000 C CNN
F 3 "" H 7550 1600 60  0000 C CNN
	1    7550 1600
	1    0    0    -1  
$EndComp
Wire Wire Line
	7500 1500 7550 1500
Wire Wire Line
	7550 1500 7550 1600
$Comp
L P03 P9
U 1 1 52146134
P 8450 4150
F 0 "P9" H 8500 4205 60  0000 C CNN
F 1 "BAT" H 8525 3695 60  0000 C CNN
F 2 "" H 8450 4150 60  0000 C CNN
F 3 "" H 8450 4150 60  0000 C CNN
	1    8450 4150
	1    0    0    -1  
$EndComp
Wire Wire Line
	8250 4350 7900 4350
Wire Wire Line
	7900 4350 7900 4100
Wire Wire Line
	8250 4450 8150 4450
Wire Wire Line
	8150 4450 8150 4650
$Comp
L P02 P7
U 1 1 5214B691
P 5150 1050
F 0 "P7" H 5200 1100 60  0000 C CNN
F 1 "BATCHG" H 5150 700 60  0000 L CNN
F 2 "" H 5150 1050 60  0000 C CNN
F 3 "" H 5150 1050 60  0000 C CNN
	1    5150 1050
	0    -1   -1   0   
$EndComp
$Comp
L PWR_FLAG #FLG064
U 1 1 5214BC49
P 4550 3900
F 0 "#FLG064" H 4550 3995 30  0001 C CNN
F 1 "PWR_FLAG" H 4550 4080 30  0000 C CNN
F 2 "" H 4550 3900 60  0000 C CNN
F 3 "" H 4550 3900 60  0000 C CNN
	1    4550 3900
	1    0    0    -1  
$EndComp
Wire Wire Line
	4550 3900 4550 4000
Connection ~ 4550 4000
$Comp
L PWR_FLAG #FLG065
U 1 1 5214DA3F
P 4600 4200
F 0 "#FLG065" H 4600 4295 30  0001 C CNN
F 1 "PWR_FLAG" H 4600 4380 30  0000 C CNN
F 2 "" H 4600 4200 60  0000 C CNN
F 3 "" H 4600 4200 60  0000 C CNN
	1    4600 4200
	0    1    1    0   
$EndComp
Wire Wire Line
	4600 4200 4500 4200
Connection ~ 4500 4200
Wire Wire Line
	10250 1700 10950 1700
Text Notes 2500 1250 0    60   ~ 0
Rins
$EndSCHEMATC
