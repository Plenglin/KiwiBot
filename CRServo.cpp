#include "CRServo.h"

void CRServo::attach(int pin) {
  pinMode(pin, OUTPUT);
  this->pin = pin;
}

void CRServo::setZero(int zero) {
  this->zero = zero;
}

void CRServo::setForwardDeadzone(int zero, int one) {
  fZero = zero;
  fMax = one;
}

void CRServo::setReverseDeadzone(int zero, int one) {
  rZero = zero;
  rMax = one;
}

int CRServo::getPulseWidth() {
  if (power == 0) {
    return 0;
  } else if (power > 0) {
    return map(power, 0, 128, fZero, fMax);
  } else {
    return map(-power, 0, 128, rMax, rZero);
  }
}

void CRServo::setPower(int power) {
  this->power = power;
}

void CRServo::write() {
  outputPulseWidth(pin, getPulseWidth());
}

