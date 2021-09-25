import processing.serial.*;
import pbxuniverse.*;

// Simple Strip Example Use the ExpanderVerse library to send data to LED!
// This example displays a very simple pattern on an display composed of
// a single addressable LED strip.
//
// requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
// a supported (by the Output Expander) strip or strand of addressable LEDs.
//
// NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
// SKETCH. See setup() below for details

ExpanderVerse leds;
PBXSerial outPort;  // Create object from Serial class
PBXBoard b0;
PBXDataChannel ch1;

int timer;

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
  ch1 = leds.addChannelWS2812(b0,0,200,"RGB"); 

  // set Processing's color mode for whatever you find convenient. ExpanderVerse
  // will handle any required conversion.
  colorMode(HSB,1);
  
  // Limit brightness of our LEDs so we don't kill our power supply
  leds.setGlobalBrightness(0.3);  
}

void draw() {
  background(0);
  // generate a sawtooth waveform
  float t1 = float (millis() % 2000)/ 2000;
   
  // set the hue of each pixel according to its position in the
  // display, animated by our sawtooth wave 
  for (PBXPixel p : leds.getPixelList()) {
    PVector n = new PVector();
    float hue;
    p.getNormalizedCoordinates(n);
    hue = 0.5+(sin(n.x+TWO_PI * t1)*0.5);  
    p.setColor(color(hue,1,1));
  }  

  // send color data to the LEDs
  leds.draw();
}
