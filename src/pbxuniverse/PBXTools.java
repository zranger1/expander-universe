package pbxuniverse;

/**
 * Static utility methods for working with LEDs, drawn from Pixelblaze, FASTLed and other places.
 * 
 */
// TODO - convert samples to use these methods
public class PBXTools {
	// processing has this, but Java doesn't, so... 
	private final static float TWOPI = (float) (Math.PI * 2.0);
	
	/**
	 * FFS -- Java has no mod operator?  Why??  Are we not
	 * well into the Century of the Fruit Bat?  Isn't forcing
	 * all bytes to be signed trouble enough for one language?  What next?<p>
	 * @return The floored remainder of the division a/b. The result will have
	 * the same sign as b.
	 */
	public static float mod(float a, float b) {
	    float result = a % b;
	    if (result < 0) {
	        result += b;
	    }
	    return result;
	}	

	/**
	 Convert input values scaled for Pixelblaze's time() function (65.535*n, where n is in seconds) to
	 milliseconds.  Simplifies porting of Pixelblaze patterns.
	*/
	float pixelblazeTimeToMs(float n) {
	  return (float) (65536.0*n);  
	}
	
	/**
	 * Returns the height of a sawtooth wave of specified period, at the 
	 * specified time.  Height range is 0.0 to 1.0.<p>
	 * Roughly equivalent to Pixelblaze's time() function, but leaves the 
	 * actual calculation of the current time to the user.
	 * @param time - current time, in any units, but usually in milliseconds
	 * @param period - the period of the sawtooth wave.  Should be
	 * given in the same units as the time parameter above.
	 * @return the value of the sawtooth function at the current time (0.0 to 1.0)
	 */
	public static float sawtooth(float time,float period) {
	  return mod(time,period) / period;
	}

	/**
	 * Converts a value  between 0.0 and 1.0, representing a sawtooth 
	 * waveform, to a position on a triangle waveform between 0.0 to 1.0.
	 * @param n - value between 0.0 and 1.0
	 * @return
	 */
	public static float triangle(float n) {
	  return  (float) (2.0 * (0.5 - Math.abs((n % 1) - 0.5)));
	}

	/**
	 * Converts a value  between 0.0 and 1.0, representing a sawtooth 
	 * waveform, to a position on a sine wave between 0.0 to 1.0.
	 * @param n - value between 0.0 and 1.0
	 * @return
	 */
	public static float wave(float n) {
	  return (float) (0.5+(Math.sin(TWOPI * Math.abs(n % 1)) * 0.5));
	}

	/**
	 * Converts a value  between 0.0 and 1.0, representing a sawtooth 
	 * waveform, to a position on a square wave between 0.0 to 1.0, using the
	 * specified duty cycle.
	 * @param n  value between 0.0 and 1.0
	 * @param dutyCycle - percentage of time the wave is "on", range 0.0 to 1.0
	 * @return
	 */
	public static float square(float n,float dutyCycle) {
	  return (float) ((Math.abs((n % 1)) <= dutyCycle) ? 1.0 : 0.0);
	}
	
	
	

}
