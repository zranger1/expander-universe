import processing.serial.*;
import pbxuniverse.*;
import java.util.*;

// ExpanderVerse Tests:
// Channels - sequentially light up all the pixels on each connected channel
//
// Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
// a supported (by the Output Expander) strip or strand of addressable LEDs.
//
// NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
// SKETCH. See setup() below for details

ExpanderVerse leds;
PBXSerial outPort;  // Create object from Serial class
PBXBoard b0;

int timer;
int index;
int pixelCount;
float holdTime;
float hue;
LinkedList<PBXDataChannel> chList;

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
  
  // add some channels to test
  leds.addChannel(b0,ChannelType.WS2812,0,200,"RGB"); 
  leds.addChannel(b0,ChannelType.WS2812,1,200,"RGB");
  leds.addChannel(b0,ChannelType.WS2812,2,200,"RGB");  

  // set Processing's color mode for whatever you find convenient. ExpanderVerse
  // will handle any required conversion.
  colorMode(HSB,1);
  
  // Limit brightness of our LEDs so we don't kill our power supply
  leds.setGlobalBrightness(0.3);  
  timer = millis();
  index = 0;
  hue = 0;
  chList = b0.getChannelList();
  
  // how long to keep each channel lit, in milliseconds
  holdTime = 250;
}

void draw() {
  background(0);

  PBXDataChannel ch = chList.get(index);
  
  if ((millis() - timer) > holdTime) {
   leds.clearPixels(ch);
   index = index + 1;
   
   // when we reach the end of the strip, return to the start
   // and change color
   if (index >= chList.size()) {
     index = 0;
     hue = (hue + 0.618) % 1;
   }
   timer = millis();
  }
  else {
    leds.setAllPixels(ch,color(hue,1,1));
  }
   
  // send color data to the LEDs
  leds.draw();
}
