
mkdir -p build
cd build
rm stm32-ft312d-test.elf
make stm32-ft312d-test.bin && \
make stm32-ft312d-test.list
#arm-none-eabi-objcopy -Obinary stm32-ft312d-test.elf stm32-ft312d-test.bin
#arm-none-eabi-objdump -S stm32-ft312d-test.elf > stm32-ft312d-test.list

