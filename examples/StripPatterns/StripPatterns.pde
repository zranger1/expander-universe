/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 1D Strip with Patterns Example  - This example shows how to set up an LED strip or
 stand display and draw to it using Pixelblaze style patterns.  
 
 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 a supported (by the Output Expander) strip or strand of addressable LEDs.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details 
 
 9/20/21 JEM (ZRanger1)
*/

import processing.serial.*;
import pbxuniverse.*;

// used to fake pointers to functions in Java, which
// doesn't support them.  
interface PatternFn {
  void runPattern();
}

// 'Handles' for the objects we'll be creating.  
ExpanderVerse leds;
PBXSerial port;
PBXBoard b0;
PBXDataChannel ch1;

// global variables used for drawing
int nPatterns = 4;
int patternIndex = 0;
PatternFn[] patterns;
String[] patternText;

// convert's pixelblaze's 65.536*n second interval values to milliseconds
float getPixelblazeTime(float n) {
  return 65536.0*n;  
}

// A couple of helper functions:
// sawtooth - roughly equivalent to Pixelblaze time(), except cycle frequency is in ms
float sawtooth(float ms) {
  float t = millis();
  return (t % (ms+1))/ ms;  
}

float triangle(float n) {
  return  2 * (0.5 - abs(n % 1 - 0.5));
}

// wave - roughly equivalent to Pixelblaze wave().
float wave(float n) {
  return 0.5+(sin(TWO_PI * (n % 1)) * 0.5);
}

// set up a table of patterns so the user can switch
// between them with a keypress
void patternSetup() {
  patterns = new PatternFn[nPatterns];
  patternText = new String[nPatterns];
  
  patterns[0] = new Edgeburst();
  patternText[0] = new String("Edgeburst");
  
  patterns[1] = new newPattern();
  patternText[1] = new String("Default Pattern");
  
  patterns[2] = new noiseFire1D();
  patternText[2] = new String("Simple Noise-based Fire");
  
  patterns[3] = new rainbowMelt();
  patternText[3] = new String("Rainbow Melt");
}

// Pixelblaze Edgeburst pattern, sped up just a bit
class Edgeburst implements PatternFn {
  void runPattern() {
    PVector m = new PVector();
    float t1 = triangle(sawtooth(getPixelblazeTime(.03)));
    for (PBXPixel p : leds.getPixelList()) {
      p.getNormalizedCoordinates(m);
      float edge = constrain(triangle(m.x) + t1 * 4 - 2,0,1);
      float v = triangle(edge);
      float h = (edge * edge - 0.2) % 1;
      p.setColor(color(h,1,v));       
    }
  }
}

// the Pixelblaze's default new pattern
class newPattern implements PatternFn {
  void runPattern() {
    PVector m = new PVector();
    
    float t1 = sawtooth(getPixelblazeTime(0.1));
    for (PBXPixel p : leds.getPixelList()) {
      p.getNormalizedCoordinates(m);
      float h = (t1 + m.x) % 1;
      p.setColor(color(h,1,1));       
    }
  }
}

class rainbowMelt implements PatternFn {
  void runPattern() {
    PVector n = new PVector();
    float t1 = sawtooth(getPixelblazeTime(0.1));
    float t2 = sawtooth(getPixelblazeTime(0.13));
    
    for (PBXPixel p : leds.getPixelList()) {
      p.getNormalizedCoordinates(n);
      float c1 = 1 - 2 * abs(0.5-n.x);
      float c2 = wave(c1);
      float c3 = wave(c2 + t1);
      float v = wave(c3 + t1);
      v = v * v;
      p.setColor(color((c1 + t2) % 1,1,v));       
    }
  }
}
 
// really, really simple Perlin noise based fire. 
class noiseFire1D implements PatternFn {
  float noiseScale = .125;
  void runPattern() {
    float h,s,b,pct;
    float t1;
    PVector n = new PVector();
    PVector m = new PVector();
    
    // get a noise value to control the fire's speed and movement
    t1 = 5 + noise(millis() / 100) * 30;
    t1 = millis()/t1;    
    
    for (PBXPixel p : leds.getPixelList()) {
      p.getMapCoordinates(m);
      p.getNormalizedCoordinates(n);
      
      // get a noise value that we manipulate to produce the fire's color pattern
      b = noise(noiseScale*(t1-m.x)); b = b * b; 
      
      // create a steep curve from 1 to 0 as we move along the strip
      pct = 1-n.x; 
      pct = constrain(pct * pct,0,1);
      
      // set up brightness to generally decay towards the end 
      b = b * pct ;    

      // add some white to the super hot parts by desaturating a little
      // near the bottom of the strip.
      s = 1.25 - b;
      
      // shift hue a little based on 'temperature'. Negative shift values
      // will make the fire redder, positive values yellower, so we've
      // aranged it so way more red (positive) values will be produced
      // near the "cool" end of the fire.
      h = 0.027 + (0.06 * b * (pct-0.5));
     
      p.setColor(color(h,s,b));       
    }
  }
}

void setup() {
  size(640,480);
  
  // First create an ExpanderVerse object to manage this display
  leds = new ExpanderVerse(this);
  
  // Get a list of available serial ports.  On my machine,
  // USB->Serial connectors are the only serial ports available. I'll
  // just take the first one. (By design, this will throw an exception if there
  // are no serial ports available on your machine.
  String portName = leds.listSerialPorts()[0];
 
  // Open the serial port. If the doesn't support the required datarate,
  // openPort() will throw an exception
  port = leds.openPort(portName);
  
  // add a single output expander board to this port
  b0 = leds.addOutputExpander(port,0);
  
  // create a single channel (on to board channel 1) with 300 APA102 LEDs 
  // using color order BGR and running at 4Mhz, and attach it to our 
  // previously created expander board.
  // We don't actually need to keep the channel object (ch1) around, but
  // if we do, we can use it to control per channel brightness and gamma
  // correction.  (The same is true at the expander board level.)
  ch1 = leds.addChannelAPA102(b0,0,300,4000000,"BGR"); 
  
  // (to use WS2812-protocol LEDs instead, substitute:
  // ch1 = leds.addChannelWS2812(b0,0,300,"BGR");    
  
  // if using APA102-style LEDs, you also need to dedicate one channel on the
  // expander to the APA's clock signal.  We'll create a clock channel on
  // board channel 2.  The clock channel should be set to the same speed as
  // the LED channel, although the output expander will have the last word.
  // If WS28xx LEDs are connected to the expander board, all APA102s will run
  // at the WS's 800khz clock speed.
  leds.addChannelAPAClock(b0,1,4000000);

  // set Processing's color mode for whatever you find convenient. You 
  // can even change colorMode at any time.  ExpanderVerse and Processing
  // will handle any required conversion.
  colorMode(HSB,1);
  
  // Limit the brightness to keep  power supplies happy.  If you know
  // your LEDs can run brighter, by all means, turn it up.
  leds.setGlobalBrightness(0.5);  
  // Turn system gamma correction off.   
  leds.setGammaCorrection(1);
  
  // load and intialize all the patterns we'll be using
  patternSetup();  
}

int frameCount = 0;
void draw() {
  int yPos = height / 4;
  background(0);
  textSize(14*displayDensity());
  text("Use '+' and '-' keys to change patterns",10,yPos);
  yPos += 20 * displayDensity();
  textSize(10 * displayDensity());
  text(patternText[patternIndex],10,yPos);
 
  // The patterns in this example draw to the LEDs by using a Pixelblaze-like method.
  // They Loop through every pixel in the display at and render it based on its mapped
  // coordinates.  Both the original (world coordinate) map and a version normalized   
  // to range 0..1 are automatically configured for 1D strips and available for use
  // at render time.  The example switches between several patterns.
  
  patterns[patternIndex].runPattern();
  
  // Periodically print the frame rate to the Processing console. 
  // Processing's frame rate is normally capped at 60fps, and 30 when it's
  // running in the background.
  // You can change this with a call to the frameRate() function, although
  // it isn't usually necessary.  
  // The maximum output framerate is limited by the speed of communication with
  // the expander board.   
  frameCount++;  
  if (frameCount >= 100) {
    println(frameRate);
    frameCount = 0;
  }
   
  // call this to send data to the LEDs 
  leds.draw();
}

// handle pattern switching keys for demo
void keyPressed() {
  switch (key) {
    case '+':
      patternIndex = (patternIndex + 1) % nPatterns;
      break;
    case '-':
      patternIndex--;
      if (patternIndex < 0) patternIndex = nPatterns - 1;
      break;
  }
}
