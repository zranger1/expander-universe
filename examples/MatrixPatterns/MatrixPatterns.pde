/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 Matrix Patterns Example -  This example shows how to set up a matrix display and
 draw to it using Pixelblaze style patterns, using both of the available maps -
 world coordinates and normalized.  
 
 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 a supported (by the Output Expander) addressable LED matrix.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details. 
 
 9/20/21 JEM (ZRanger1)
*/

import processing.serial.*;
import pbxuniverse.*;

// Set the dimensions of your matrix here
// NOTE: 'height' and 'width' are reserved words in Processing
int mHeight = 16;
int mWidth = 16;

// 'Handles' for the objects we'll be creating.  
ExpanderVerse leds;
PBXSerial port;
PBXBoard b0;
PBXDataChannel ch1;

// A couple of helper functions:
// sawtooth - roughly equivalent to Pixelblaze time(), except cycle frequency is in ms
float sawtooth(float ms) {
  float t = millis();
  return (t % (ms+1))/ ms;  
}

// wave - roughly equivalent to Pixelblaze wave().
float wave(float n) {
  return 0.5+(sin(TWO_PI * (n % 1)) * 0.5);
}

// Pattern functions
// axisCalibration - displays moving line on both x and y axes, using world map coordinates
void axisCalibration() {
    PVector c = new PVector();
    float zoom = wave(sawtooth(1000));
     
    for (PBXPixel p : leds.getPixelList()) {
      p.getMapCoordinates(c);
      if (c.x == floor(zoom * mWidth)) {
        p.setColor(color(0.333,1,1));        
      } else if (c.y == (15-floor(zoom * mHeight))) {
        p.setColor(color(0,1,1));  
      }
      else {
        p.setColor(color(0));        
      }
    }  
}

// lineDancer - displays a pattern of twisting, zooming vertical lines, using
// normalized map coordinates.
void lineDancer() {
    float timebase = (float) millis() / 200.0; 
    PVector c = new PVector();
    float zoom = wave(sawtooth(5000));
  
    for (PBXPixel p : leds.getPixelList()) {
      float h,b,radius,theta;
      p.getNormalizedCoordinates(c);
      c.x -= 0.5; c.y -=0.5;
      radius = 1.27-(c.x * c.x + c.y * c.y)*2.4;
      theta = radius * radius * sin(radius + timebase);
      c.x = (cos(theta) * c.x) - (sin(theta) * c.y);
      
      b = 1 - wave(c.x * 4.6 * zoom);
      h = (c.x * zoom) + zoom + theta/TWO_PI;
      
      p.setColor(color(h%1,1,b*b));
    }  
}

void setup() {
  size(1000,1000);
  
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
  
  // create a single channel (channel 0), of WS2812 LEDs with color
  // order GRB and attach it to our previously created expander board.
  // We don't actually need to keep the channel object (ch1) around, but
  // if we do, we can use it to control per channel brightness and gamma
  // correction.  (The same is true at the expander board level.)
  ch1 = leds.addChannel(b0,ChannelType.WS2812,0,mWidth * mHeight,"GRB"); 

  // set Processing's color mode for whatever you find convenient. You 
  // can even change colorMode at any time.  ExpanderVerse and Processing
  // will handle any required conversion.
  colorMode(HSB,1);
  
  // Limit the brightness to keep  power supplies happy.  If you know
  // your LEDs can run brighter, by all means, turn it up.
  leds.setGlobalBrightness(0.25);
  
  // Use the setMatrixMap() helper function to create an mWidth * mHeight mapping for our
  // pixels, with 'zigzag' wiring.  You could also do this manually using
  // leds.setMapCoordinates(index,x,y,z) to set your map in setup(). For simple matrices though,
  // setMatrixMap is a quick, handy shortcut.
  // parameters are: setMatrixMap(startIndex,width,height,zigzag flag);
    leds.setMatrixMap(0,mWidth,mHeight,true);   
}

int frameCount = 0;
void draw() {
 
  // This example draws directly to the LEDs by using a Pixelblaze-like method.  It
  // Loops through every pixel in the display at and renders it based on its mapped
  // coordinates.  Both the original (world coordinate) map and a version normalized   
  // to range 0..1 are available for use at render time.  The example switches between
  // two patterns, each of which uses a different map.
  
  int timer = (millis() / 1000) % 10;
  if (timer < 5) {
    // this pattern uses the normalized map
    lineDancer();
  } else {
    // this pattern uses the world coordinate map as supplied by the user
    axisCalibration();
  }
   
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
