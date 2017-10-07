#include <helper_3dmath.h>
#include <MPU6050.h>

#include <Servo.h>

class CRServo {
  int pin;

  int zero;
  int rZero;
  int rMax;
  int fZero;
  int fMax;
  public:
    void attach(int pin) {
      pinMode(pin, OUTPUT);
      this->pin = pin;
    }

    void setZero(int zero) {
      this->zero = zero;
    }

    void setForwardDeadzone(int zero, int one) {
      fZero = zero;
      fMax = one;
    }

    void setReverseDeadzone(int zero, int one) {
      rZero = zero;
      rMax = one;
    }

    /**
     * A number between -128 and 127 describing how fast
     * to run the motor.
     */
    void write(int power) {
      if (power == 0) {
        writeRaw(zero);
      } else if (power > 0) {
        writeRaw(map(power, 0, 128, fZero, fMax));
      } else {
        writeRaw(map(-power, 0, 128, rMax, rZero));
      }
    }

    /**
     * Write raw.
     */
    void writeRaw(int power) {
      analogWrite(pin, power);
    }
    
};

CRServo motorA;
CRServo motorB;
CRServo motorC;

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
  motorA.setZero(90);
  motorA.setForwardDeadzone(90, 160);
  motorA.setReverseDeadzone(90, 32);
  
  motorB.attach(6);
  motorB.setZero(90);
  motorB.setForwardDeadzone(90, 160);
  motorB.setReverseDeadzone(90, 32);
  
  motorC.attach(9);
  motorC.setZero(47);
  motorC.setForwardDeadzone(47, 80);
  motorC.setReverseDeadzone(16, 47);

  pinMode(3, OUTPUT);
  
  Serial.println("READY");

}

void loop() {

/*
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
        motor.write(power);

        // Output for debugging
        Serial.print("MOV motor=");
        Serial.print(key);
        Serial.print(" power=");
        Serial.println(power);
        Serial.println("OK");
        break;
        
      case 'r':  // Set a motor to a raw power
        key = Serial.read();
        motor = getMotor(key);
        power = Serial.readStringUntil('\n').toInt();
        motor.writeRaw(power);

        // Output for debugging
        Serial.print("RAW motor=");
        Serial.print(key);
        Serial.print(" power=");
        Serial.println(power);
        Serial.println("OK");
        break;
        

      
    }
    
  }*/

  digitalWrite(3, HIGH);
  delayMicroseconds(3000);
  digitalWrite(3, LOW);
  delay(3);
  
}

CRServo getMotor(char name) {
  switch (name) {
    case 'a': return motorA;
    case 'b': return motorB;
    case 'c': return motorC;
  }
  return motorA;
}

void pulseWidth(int pin, int width) {
  digitalWrite(pin, HIGH);
  delayMicroseconds(width*2);
  digitalWrite(pin, LOW);
}

