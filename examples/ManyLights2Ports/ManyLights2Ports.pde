import processing.serial.*;
import pbxuniverse.*;

// Use the ExpanderVerse library to send data to LED!
// This example displays a simple pattern on several strands/strips

ExpanderVerse leds;
PBXSerial port1,port2;  // Create object from Serial class
PBXBoard b0,b1;
PBXDataChannel ch1,ch2,ch3,ch4,ch5;

int timer;
int pixelCount;
int frameCount = 0;

void setup() {
  size(1000,1000);
  
  // First create an ExpanderVerse object to manage this display
  leds = new ExpanderVerse(this);
  
  // Get a list of available serial ports.  On my machine,
  // USB->Serial connectors are the only serial ports available. I'll
  // just take the first one...
//  String portName = leds.listSerialPorts()[0];
 
  // Open the serial port. If the port isn't available, or doesn't support
  // the required datarate, openPort() will throw an exception
//  outPort = leds.openPort(portName);
  port1 = leds.openPort("COM5");
//  port2 = leds.openPort("COM5");
  
  // add an expansion board to our serial port
  b0 = leds.addOutputExpander(port1,0);
//  b1 = leds.addOutputExpander(port2,0);
  
  
  // add three channels of 200 LEDs each to the expansion board we just created
//  ch1 = leds.addChannelWS2812(b0,0,200,"RGB"); 
//  ch2 = leds.addChannelWS2812(b0,1,200,"RGB"); 
//  ch3 = leds.addChannelWS2812(b0,2,200,"RGB"); 
  ch4 = leds.addChannelAPA102(b0,0,300,2000000,"GBR");
  ch5 = leds.addChannelAPAClock(b0,1,200000);  

  // set Processing's color mode for whatever you find convenient. ExpanderVerse
  // will handle any required conversion.
  colorMode(HSB,1);
  
  // I'm using this sketch as a test to see how many LEDs I can successfully run.
  // limit the brightness of our LEDs so we don't kill our power supply
  leds.setGlobalBrightness(0.3);
  
  // timer for LED animation
  timer = millis();
  pixelCount = leds.getPixelCount();
  println("pixelCount = ",pixelCount);


}


void draw() {

  // generate a 4 second, 0-1 range sawtooth waveform
  float t1 = float (millis() % 4000)/ 4000.;
   
  // set the hue of each pixel to a shade of cyan/blue/purple according to
  // its position in the display, animated by our sawtooth wave 
  for (int i = 0; i < pixelCount; i++) {
    float pct = (float(i) / pixelCount) + t1;
    pct = 0.3 * (sin(TWO_PI * pct)*0.5);  // sine wave offset to add to hue
    pct = 0.6667 + pct;  // 0.6667 is pure blue in HSB
    
    leds.setPixel(i,color(pct,1,1));
  }  
  
  // periodically display the frame rate in the Processing console 
  // Processing's frame rate is normally capped at 60fps, and 30 when it's
  // running in the background.  You can change this with a call to the
  // frameRate() function although this is usually not necessary.   
  //
  frameCount++;  
  if (frameCount >= 60) {
    println((millis() - timer) / frameCount);
    timer = millis();
    frameCount = 0;
  }
   
  leds.draw();
}
