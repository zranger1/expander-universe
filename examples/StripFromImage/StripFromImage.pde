/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 1D Strip from Image Example  - This example shows how to set up an LED strip or
 stand display and draw it to using Processing's graphics methods.
 
 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 a supported (by the Output Expander) strip or strand of addressable LEDs.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details.
 
 9/20/21 JEM (ZRanger1)
*/

import processing.serial.*;
import pbxuniverse.*;

// class used to fake pointers to functions in Java, which
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
int pixelCount = 0;
PImage img;

int nPatterns = 4;
int patternIndex = 0;
PatternFn[] patterns;
PImage[] patternImages;
String[] patternText;

int yPos = 0;
int incr = 1;
int timer = millis();

// Generates an image on the fly and sends it row by row to the
// LEDs
class LiveNoise implements PatternFn {
  void runPattern() {
    int x,y;
    float t1 = millis()/20;
    loadPixels();
  
    for (y = 0; y < height; y++) {
      for (x = 0; x < width; x++) {
        if (y == 0) {
          float hue = noise((x+t1)*noiseScale,noiseScale);
          float bri = noise((x-t1)*noiseScale,noiseScale);        
          pixels[x] = color(hue,1,bri);        
        }
        else {
          pixels[y * width + x] = pixels[x];
        }
      }
    }
    updatePixels();  
    leds.setPixelsFromImage(0,pixelCount,pixels);
  }
}

class FlameSpiral implements PatternFn {
  void runPattern() {
    runJPEGImage(patternImages[patternIndex]);
  }  
}

class NoiseMap implements PatternFn {
  void runPattern() {
    runJPEGImage(patternImages[patternIndex]);
  }
}

class Pattern1 implements PatternFn {
  void runPattern() {
    runJPEGImage(patternImages[patternIndex]);
  }  
}

void runJPEGImage(PImage img) {
  PImage strip;
  int xPos = (width / 2) - (pixelCount / 2);
  
  image(img,0,0);
  strip = get(xPos,yPos,pixelCount,1);
  strip.loadPixels();
  
  stroke(color(0.3333,1,1));
  line(0,yPos,width,yPos);
  
  leds.setPixelsFromImage(0,pixelCount,strip.pixels);
  if (millis() - timer > 16) {
    yPos +=incr;
    if (yPos < 0 || yPos >= height) {
      incr = -incr;
      yPos += incr;
    }
    timer = millis();
  }  
}

// set up a table of image based patterns so the user can switch
// between them with a keypress
void patternSetup() {
  patterns = new PatternFn[nPatterns];
  patternImages = new PImage[nPatterns];
  patternText = new String[nPatterns];
  
  patterns[0] = new LiveNoise();
  patternImages[0] = null;
  patternText[0] = new String("Live Noise Pattern");
  
  patterns[1] = new FlameSpiral();
  patternImages[1] = loadImage("flamespiral.jpg");  
  patternText[1] = new String("Flame Spiral");
  
  patterns[2] = new NoiseMap();
  patternImages[2] = loadImage("noisemap.jpg");  
  patternText[2] = new String("Color 2D Noise Map");
  
  patterns[3] = new Pattern1();
  patternImages[3] = loadImage("pattern1.jpg");  
  patternText[3] = new String("Abstract Pattern 1");
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
  leds.setGammaCorrection(2);
  
  pixelCount = leds.getPixelCount();
  
  // load and intialize all the patterns we'll be using
  patternSetup();    

}

int frameCount = 0;
float noiseScale = 0.04;
void draw() {
    fill(color(0,0,1));

  // The patterns in this example draw to the LEDs by using a Pixelblaze-like method.
  // They Loop through every pixel in the display at and render it based on its mapped
  // coordinates.  Both the original (world coordinate) map and a version normalized   
  // to range 0..1 are automatically configured for 1D strips and available for use
  // at render time.  The example switches between several patterns.
  
  patterns[patternIndex].runPattern();
  
  // display info text
  int yP = height / 4;  
  textSize(14*displayDensity());
  text("Use '+' and '-' keys to change patterns",10,yP);
  yP += 20 * displayDensity();
  textSize(10 * displayDensity());
  text(patternText[patternIndex],10,yP);  
   
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
      yPos = 0; incr = 1; timer = millis();
      break;
    case '-':
      patternIndex--;
      yPos = 0; incr = 1; timer = millis();
      if (patternIndex < 0) patternIndex = nPatterns - 1;
      break;
  }
}
