#include "ring_buffer.h"

void ring_buffer_init(ring_buffer* buffer, uint8_t* storage, uint16_t size) {
  buffer->storage = storage;
  buffer->size = size;
  buffer->end = buffer->storage + buffer->size;
  buffer->read = buffer->storage;
  buffer->write = buffer->storage;
  buffer->available = 0;
}

uint8_t ring_buffer_read(ring_buffer* buffer) {
  if(buffer->available == 0) {
    return 0;
  }
  uint8_t ret = *buffer->read++;
  buffer->available--;
  if(buffer->read >= buffer->end) {
    buffer->read = buffer->storage;
  }
  return ret;
}

void ring_buffer_write(ring_buffer* buffer, uint8_t b) {
  if(buffer->available == buffer->size) {
    return;
  }
  *buffer->write = b;
  buffer->write++;
  buffer->available++;
  if(buffer->write >= buffer->end) {
    buffer->write = buffer->storage;
  }
}
