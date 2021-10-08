import processing.serial.*;
import pbxuniverse.*;

// ExpanderVerse Tests:
// SequentialPixels - light up every pixel in the display, in sequence.
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

int timer;
int index;
int pixelCount;
float holdTime;
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
  timer = millis();
  index = 0;
  hue = 0;
  pixelCount = leds.getPixelCount();
  
  // how long to hold each pixel, calculated from pixelCount and the
  // number of ms you want the test to take to traverse the entire strip.
  holdTime = 5000 / pixelCount; 
}

void draw() {
  background(0);

  PBXPixel p = leds.getPixelList().get(index);
  
  if ((millis() - timer) > holdTime) {
   p.setColor(0);
   index = index + 1;
   
   // when we reach the end of the strip, return to the start
   // and change color
   if (index >= pixelCount) {
     index = 0;
     hue = (hue + random(1)) % 1;
   }
   timer = millis();
  }
  else {
    p.setColor(color(hue,1,1));
  }
   
  // send color data to the LEDs
  leds.draw();
}
