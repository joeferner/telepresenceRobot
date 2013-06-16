
mkdir -p build
cd build
rm stm32-ft312d-test.elf
make stm32-ft312d-test.bin && \
make stm32-ft312d-test.list && \
cd .. && \
sudo /opt/arm-linaro-eabi-4.6/bin/openocd

