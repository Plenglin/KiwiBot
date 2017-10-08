class PIDGen {

  public:
    int p;
    int i;
    int d;
    int sum;
    int lastError;

    /**
       Give it error, and it will spit out new values.
       dt is in microseconds.
    */
    long pushError(long error, int dt) {

      int delta = (error - lastError) / dt;
      sum += error * dt;
      long out = p * error + i * sum + d * delta;
      lastError = error;
      return out;

    }

};

