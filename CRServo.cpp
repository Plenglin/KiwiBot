#include <Arduino.h>

#include "CRServo.h"

void CRServo::attach(int pin) {
  pinMode(pin, OUTPUT);
  this->pin = pin;
}

void CRServo::setForwardDeadzone(int zero, int max) {
  fZero = zero;
  fMax = max;
}

void CRServo::setReverseDeadzone(int max, int zero) {
  rMax = max;
  rZero = zero;
}

int CRServo::write(int power) {
  power = constrain(power, -128, 128);
  int out;
  if (power == 0) {
    out = 0;
  } else if (power > 0) {
    out = map(power, 0, 128, fZero, fMax);
  } else {
    out = map(-power, 0, 128, rMax, rZero);
  }
  writeRaw(out);
  return out;
}

void CRServo::writeRaw(int width) {
  analogWrite(pin, width);
}

