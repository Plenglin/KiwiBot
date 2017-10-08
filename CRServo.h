#include <Arduino.h>

class CRServo {
  int power;

  int zero;
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

    int getPulseWidth();

    void setPower(int power);

    /**
     * A number between -128 and 127 describing how fast
     * to run the motor.
     */
    void write();
    
};
