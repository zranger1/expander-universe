import processing.serial.*;
import pbxuniverse.*;

// ExpanderVerse Tests:
// ColorOrder - test red, green and blue color channels to make sure that color
// order has been set correctly when setting up LEDs
//
// Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
// a supported (by the Output Expander) strip or strand of addressable LEDs.
//
// NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
// SKETCH. See setup() below for details

ExpanderVerse leds;
PBXSerial outPort;  // Create object from Serial class
PBXBoard b0;
PBXDataChannel ch1;

float hue;

void setup() {
  size(640,480);
  
  // First create an ExpanderVerse object to manage this display
  leds = new ExpanderVerse(this);
  
  // Get a list of available serial ports.  On my machine,
  // USB->Serial connectors are the only serial ports available. I'll
  // just take the first one...
  String portName = leds.listSerialPorts()[0];

  // Open the serial port. If the port isn't available, or doesn't support
  // the required datarate, openPort() will throw an exception
  outPort = leds.openPort(portName);
  
  // add an expansion board to our serial port
  b0 = leds.addOutputExpander(outPort,0);
  
  // add a 200 LED WS2812 strip to the expansion board we just created
  ch1 = leds.addChannel(b0,ChannelType.WS2812,0,200,"RGB"); 

  // set Processing's color mode for whatever you find convenient. ExpanderVerse
  // will handle any required conversion.
  colorMode(HSB,1);
  
  // Limit brightness of our LEDs so we don't kill our power supply
  leds.setGlobalBrightness(0.3);  
  hue = 0;
}

void draw() {
  background(color(hue,1,0.75));
  
  textSize(24*displayDensity());
  text("Press R,G or B to test color channel.",10,40);  

  leds.setAllPixels(color(hue,1,1));
   
  // send color data to the LEDs
  leds.draw();
}

// handle shader switching keys for demo
void keyPressed() {
  switch (key) {
    case 'r':
    case 'R':
      hue = 0;
      break;
    case 'g':
    case 'G':
      hue = 0.3333;
      break;
    case 'b':
      hue = 0.66667;
    case 'B':
      break;      
  }
}
