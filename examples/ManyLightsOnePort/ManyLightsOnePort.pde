import processing.serial.*;
import pbxuniverse.*;

/* 
  Many Lights, One Port Example - displays a simple pattern on an display composed of
  several strands/strips attached to a single Pixelblaze Output Expander board
  on a single serial port.
  
 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 one or more supported (by the Output Expander) addressable LED strips or strands.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details.   

*/

ExpanderVerse leds;
PBXSerial outPort;  // Create object from Serial class
PBXBoard b0;
PBXDataChannel ch1,ch2,ch3;

int timer;
int pixelCount;
int frameCount = 0;

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
  
  // add three channels of 200 LEDs each to the expansion board we just created
  // (tested w/3 strands of the "strange" brzlab fairy lights.)
  ch1 = leds.addChannel(b0,ChannelType.WS2812,0,200,"RGB"); 
  ch2 = leds.addChannel(b0,ChannelType.WS2812,1,200,"RGB"); 
  ch3 = leds.addChannel(b0,ChannelType.WS2812,2,200,"RGB");   

  // set Processing's color mode for whatever you find convenient. ExpanderVerse
  // will handle any required conversion.
  colorMode(HSB,1);
  
  // Limit brightness of our LEDs so we don't kill our power supply
  leds.setGlobalBrightness(0.3);
  
  // You can also control brightness and gamma correction per channel. Gamma correction
  // is linear by default. Let's change it on a channel, and set that channel a little
  // darker than the others.
  // dimmer than the others.
  ch2.setBrightness(0.1);
  ch2.setGammaCorrection(2);  
  
  // timer for LED animation
  timer = millis();
  pixelCount = leds.getPixelCount();
  println("pixelCount = ",pixelCount);
}

void draw() {
  background(0);
  // generate a 4 second, 0-1 range sawtooth waveform
  float t1 = float (millis() % 4000)/ 4000;
   
  // set the hue of each pixel to a shade of cyan/blue/purple according to
  // its position in the display, animated by our sawtooth wave 
  for (PBXPixel p : leds.getPixelList()) {
    PVector n = new PVector();
    float hue;
    p.getNormalizedCoordinates(n);
    
    hue = 0.3 * (sin(TWO_PI * (n.x+t1))*0.5);  // sine wave offset to add to hue
    hue = 0.6667 + hue;  // 0.6667 is pure blue in HSB

    p.setColor(color(hue,1,1));
  }  
  
  // periodically display the frame rate in the Processing console 
  // Processing's frame rate is normally capped at 60fps, and 30 when it's
  // running in the background.  You can change this with a call to the
  // frameRate() function although this is usually not necessary.   
  //
  frameCount++;  
  if (frameCount >= 60) {
//    println((millis() - timer) / frameCount);
    timer = millis();
    frameCount = 0;
  }
   
  leds.draw();
}
