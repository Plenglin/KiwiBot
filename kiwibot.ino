#include <helper_3dmath.h>
#include <MPU6050.h>

#include "CRServo.h"

#define LED_PIN 13

#define GYRO_VELOCITY mpu.getRotationX()
#define CLOCK 2


CRServo motorA;
CRServo motorB;
CRServo motorC;
CRServo motors[] = {motorA, motorB, motorC};
int motorPins[] = {3, 6, 9};
MPU6050 mpu;

void setup() {
  Serial.begin(9600);
  Serial.println("INIT");
  /*
  a.attach(3);//, 0, 90, 180);
  b.attach(6);//, 0, 90, 180);
  c.attach(9);//, 0, 90, 180);*/

  TCCR2B = (TCCR2B & 0b11111000) | 0x05;  // Set pin 3 to 244.14hz
  TCCR0B = (TCCR0B & 0b11111000) | 0x04;  // Set pin 6 to 244.14hz
  TCCR1B = (TCCR1B & 0b11111000) | 0x04;  // Set pin 9 to 112.55hz

  motorA.attach(3);
  motorA.setZero(1500);
  motorA.setForwardDeadzone(1500, 2000);
  motorA.setReverseDeadzone(1500, 1000);
  
  motorB.attach(6);
  motorB.setZero(1500);
  motorB.setForwardDeadzone(1500, 2000);
  motorB.setReverseDeadzone(1500, 1000);

  motorC.attach(9);
  motorC.setZero(1500);
  motorC.setForwardDeadzone(1500, 2000);
  motorC.setReverseDeadzone(1500, 1000);
  
  mpu.initialize();

  pinMode(3, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  
  Serial.println("READY");

}

void loop() {

  if (Serial.available()) {

    CRServo motor;
    int power;
    char key;
    
    delay(5);  // Wait for message
    char command = Serial.read();
    
    switch (command) {
      
      case 'm':  // Set a motor to a scaled power
      
        key = Serial.read();
        motor = getMotor(key);
        power = Serial.readStringUntil('\n').toInt();

        motor.setPower(power);

        // Output for debugging
        Serial.print("MOV motor=");
        Serial.print(key);
        Serial.print(" power=");
        Serial.println(power);
        Serial.println("OK");
        break;

      case 'c':  // Calibrate
        Serial.println("CAL");
        doCalibrate(10000);
        Serial.println("OK");
        break;
      
    }
    
  }

  outputMotors();
  delay(CLOCK*1);

}

void outputMotors() {
  int widths[3];
  for (int m=0; m < 3; i++) {
    widths[m] = motors[m].getPulseWidth();
    if (widths[m] > 0) {
      digitalWrite(motorPins[m], HIGH);
    }
  }
  while (true) {
    bool stopLoop = true;
    for (int m=0; m < 3; i++) {
      widths[m] -= 10;
      if (widths[m] < 0) {
        digitalWrite(motorPins[m], LOW);
      } else {
        stopLoop = false;
      }
    }
    if (stopLoop) {
      break;
    }
    delayMicroseconds(CLOCK*10);
  }

}

CRServo getMotor(char name) {
  switch (name) {
    case 'a': return motorA;
    case 'b': return motorB;
    case 'c': return motorC;
  }
  return motorA;
}

void doCalibrate(int times) {

  long gyroXSum;
  long gyroYSum;
  long gyroZSum;
  long accelXSum;
  long accelYSum;
  long accelZSum;

  for (int i=0; i<times; i++) {
    
    gyroXSum += mpu.getRotationX();
    gyroYSum += mpu.getRotationY();
    gyroZSum += mpu.getRotationZ();
    accelXSum += mpu.getAccelerationX();
    accelYSum += mpu.getAccelerationY();
    accelZSum += mpu.getAccelerationZ();
    
    if (i % 100 < 50) {
      digitalWrite(LED_PIN, HIGH);
    } else {
      digitalWrite(LED_PIN, LOW);
    }
    
    delayMicroseconds(10);
    
  }

  mpu.setXGyroOffset(gyroXSum / times);
  mpu.setYGyroOffset(gyroYSum / times);
  mpu.setZGyroOffset(gyroZSum / times);
  mpu.setXAccelOffset(accelXSum / times);
  mpu.setYAccelOffset(accelYSum / times);
  mpu.setZAccelOffset(accelZSum / times);

}

