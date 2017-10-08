#include <helper_3dmath.h>
#include <MPU6050.h>

#include "CRServo.h"

#define LED_PIN 13

#define GYRO_VELOCITY mpu.getRotationX()
#define CLOCK 2


CRServo* motorA;
CRServo* motorB;
CRServo* motorC;
MPU6050 mpu;

void setup() {
  Serial.begin(9600);
  Serial.println("INIT");

  TCCR2B = (TCCR2B & 0b11111000) | 0x05;  // Set pin 3 to 244.14hz
  TCCR0B = (TCCR0B & 0b11111000) | 0x04;  // Set pin 6 to 244.14hz
  TCCR1B = (TCCR1B & 0b11111000) | 0x04;  // Set pin 9 to 112.55hz

  motorA = new CRServo();
  (*motorA).attach(3);
  (*motorA).setReverseDeadzone(32, 90);
  (*motorA).setForwardDeadzone(90, 160);

  motorB = new CRServo();
  (*motorB).attach(6);
  (*motorB).setReverseDeadzone(32, 90);
  (*motorB).setForwardDeadzone(90, 160);
  
  motorC = new CRServo();
  (*motorC).attach(9);
  (*motorC).setReverseDeadzone(16, 47);
  (*motorC).setForwardDeadzone(47, 80);
  
  mpu.initialize();
  mpu.setFullScaleGyroRange(MPU6050_GYRO_FS_1000);
  mpu.setFullScaleAccelRange(MPU6050_ACCEL_FS_8);


  pinMode(LED_PIN, OUTPUT);
  
  Serial.println("READY");

}

void loop() {

  if (Serial.available()) {

    CRServo motor;
    int power;
    char key;
    int output;
    
    delay(5);  // Wait for message
    char command = Serial.read();
    
    switch (command) {

      case 'a':  // Get MPU data

        Serial.print("MPU ax=");
        Serial.print(mpu.getAccelerationX());
        Serial.print(" ay=");
        Serial.print(mpu.getAccelerationY());
        Serial.print(" az=");
        Serial.print(mpu.getAccelerationZ());
        Serial.print(" gx=");
        Serial.print(mpu.getRotationX());
        Serial.print(" gy=");
        Serial.print(mpu.getRotationY());
        Serial.print(" gz=");
        Serial.println(mpu.getRotationZ());
        Serial.println("OK");
        break;

      case 'm':  // Set a motor to a scaled power
        key = Serial.read();
        motor = *getMotor(key);
        power = Serial.readStringUntil('\n').toInt();
        output = motor.write(power);

        // Output for debugging
        Serial.print("MOV motor=");
        Serial.print(key);
        Serial.print(" power=");
        Serial.print(power);
        Serial.print(" pin=");
        Serial.print(motor.pin);
        Serial.print(" width=");
        Serial.println(output);
        Serial.println("OK");
        break;
        
      case 'r':  // Set a motor to a raw width
        key = Serial.read();
        motor = *getMotor(key);
        power = Serial.readStringUntil('\n').toInt();
        motor.writeRaw(power);

        // Output for debugging
        Serial.print("RAW motor=");
        Serial.print(key);
        Serial.print(" width=");
        Serial.println(power);
        Serial.println("OK");
        break;
        
      case 'c':  // Calibrate
        Serial.println("CAL");
        doCalibrate(1000);

        Serial.print("ax=");
        Serial.print(mpu.getXAccelOffset());
        Serial.print(" ay=");
        Serial.print(mpu.getYAccelOffset());
        Serial.print(" az=");
        Serial.print(mpu.getZAccelOffset());
        Serial.print(" gx=");
        Serial.print(mpu.getXGyroOffset());
        Serial.print(" gy=");
        Serial.print(mpu.getYGyroOffset());
        Serial.print(" gZ=");
        Serial.println(mpu.getZGyroOffset());

        Serial.println("OK");
        break;
      
    }
    
  }
  delay(CLOCK*1);

}

CRServo* getMotor(char name) {
  switch (name) {
    case 'a': 
      return motorA;
    case 'b': return motorB;
    case 'c': return motorC;
  }
  return motorA;
}

void doCalibrate(int times) {
  digitalWrite(LED_PIN, HIGH);

  long gyroXSum = 0;
  long gyroYSum = 0;
  long gyroZSum = 0;
  long accelXSum = 0;
  long accelYSum = 0;
  long accelZSum = 0;

  mpu.setXGyroOffset(0);
  mpu.setYGyroOffset(0);
  mpu.setZGyroOffset(0);
  mpu.setXAccelOffset(0);
  mpu.setYAccelOffset(0);
  mpu.setZAccelOffset(0);

  digitalWrite(LED_PIN, LOW);

  delay(10);

  for (int i=0; i < times; i++) {

    gyroXSum += mpu.getRotationX();
    gyroYSum += mpu.getRotationY();
    gyroZSum += mpu.getRotationZ();
    accelXSum += mpu.getAccelerationX();
    accelYSum += mpu.getAccelerationY();
    //accelZSum += mpu.getAccelerationZ();
    int az = mpu.getAccelerationZ();
    accelZSum += az;

    Serial.print(az);
    Serial.print(" ");
    Serial.println(accelZSum);
    
    if (i % 100 < 50) {
      digitalWrite(LED_PIN, HIGH);
    } else {
      digitalWrite(LED_PIN, LOW);
    }
    
    delay(1);
    
  }

  mpu.setXGyroOffset(-gyroXSum / times);
  mpu.setYGyroOffset(-gyroYSum / times);
  mpu.setZGyroOffset(-gyroZSum / times);
  mpu.setXAccelOffset(-accelXSum / times);
  mpu.setYAccelOffset(-accelYSum / times);
  mpu.setZAccelOffset(-accelZSum / times);

  Serial.println(mpu.getAccelerationZ());

}

