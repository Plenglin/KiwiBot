#include <Arduino.h>

class CRServo {
  int power;

  int rZero;
  int rMax;
  int fZero;
  int fMax;
  
  public:
    
    int pin;
  
    void attach(int pin);

    void setZero(int zero);

    void setForwardDeadzone(int zero, int one);

    void setReverseDeadzone(int zero, int one);

    /**
     * A number between -128 and 127 describing how fast
     * to run the motor.
     */
    int write(int power);

    /**
     * Write a raw PWM signal.
     */
    void writeRaw(int power);
};
