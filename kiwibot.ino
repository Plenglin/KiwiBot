#include <helper_3dmath.h>
#include <MPU6050.h>

#include "CRServo.h"
#include "PIDGen.h"

#define LED_PIN 13

const int CLOCK = 2;
const long REVOLUTION = 1474560;

CRServo* motorA;
CRServo* motorB;
CRServo* motorC;
MPU6050 mpu;

unsigned long lastTimestamp = 0;
long angle = 0;  // Divide this by 4096 for the actual angle
long vx;
long vy;

PIDGen gyroPID;  // Takes in degrees in 1/4096 increments

void setup() {
  Serial.begin(9600 / CLOCK);
  Serial.println("INIT");

  TCCR2B = (TCCR2B & 0b11111000) | 0x05;  // Set pin 3 to 244.14hz
  TCCR0B = (TCCR0B & 0b11111000) | 0x04;  // Set pin 6 to 244.14hz
  TCCR1B = (TCCR1B & 0b11111000) | 0x04;  // Set pin 9 to 112.55hz

  Serial.println("INIT CRSERVO");
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

  Serial.println("INIT MPU");
  mpu.initialize();
  delay(5);
  mpu.setFullScaleGyroRange(MPU6050_GYRO_FS_1000);
  mpu.setFullScaleAccelRange(MPU6050_ACCEL_FS_8);

  Serial.println("INIT PID");
  gyroPID.p = 1;

  pinMode(LED_PIN, OUTPUT);

  Serial.println("READY");

  Serial.println(wrapAngle(0, 360));
  Serial.println(wrapAngle(90, 360));
  Serial.println(wrapAngle(120, 360));
  Serial.println(wrapAngle(180, 360));
  Serial.println(wrapAngle(250, 360));
  Serial.println(wrapAngle(390, 360));
  Serial.println(wrapAngle(-30, 360));

  delay(100);

}

int gyroTarget;

void loop() {

  unsigned long timestamp = micros() / CLOCK;
  int delta = timestamp - lastTimestamp;
  lastTimestamp = timestamp;
  long angVel = long(mpu.getRotationX() / 16 * 16); // The angle, filtered
  angle += (angVel * delta) / 2000;

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

      case 'b':  // Get integrated values

        Serial.print("BER head=");
        Serial.print(angle);
        Serial.print(" vx=");
        Serial.print(vx);
        Serial.print(" vy=");
        Serial.println(vy);
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

      case 'g':   // Set the gyro PID target, in 4096th degrees
        gyroTarget = Serial.readStringUntil('\n').toInt();
        Serial.print("TRG GYRO value=");
        Serial.println(gyroTarget);
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

      case 's':  // Reset integrators
        Serial.println("RES");
        resetIntegrators();
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

  long angleError = wrapAngle(angle - gyroTarget, REVOLUTION);
  long gyroPIDOutput = gyroPID.pushError(angleError, delta) / 4096;
  if (abs(gyroPIDOutput) < 20) {
    gyroPIDOutput = 0;
  }
  motorA->write(gyroPIDOutput);
  motorB->write(gyroPIDOutput);
  motorC->write(gyroPIDOutput);
  delay(CLOCK * 1);

}

long wrapAngle(long angle, long revolution) {
  return fmod(angle - revolution/2, revolution) - revolution/2;
}

long fmod(long a, long b) {
  if (a >= 0) {
    return a % b;
  } else {
    return a % b + b;
  }
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

void resetIntegrators() {
  angle = 0;
  vx = 0;
  vy = 0;
}

void doCalibrate(int times) {
  digitalWrite(LED_PIN, HIGH);
  motorA->write(0);
  motorB->write(0);
  motorC->write(0);

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

  for (int i = 0; i < times; i++) {

    gyroXSum += mpu.getRotationX();
    gyroYSum += mpu.getRotationY();
    gyroZSum += mpu.getRotationZ();
    accelXSum += mpu.getAccelerationX();
    accelYSum += mpu.getAccelerationY();
    //accelZSum += mpu.getAccelerationZ();
    int az = mpu.getAccelerationZ();
    accelZSum += az;

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

  resetIntegrators();

}

