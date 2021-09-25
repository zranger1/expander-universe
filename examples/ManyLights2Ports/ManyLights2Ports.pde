import processing.serial.*;
import pbxuniverse.*;
/* 
 Many Lights, Two Ports Example - displays a simple pattern on an display composed of
 multiple strands/strips of different types attached to a two Pixelblaze Output Expander
 boards, connected to two serial ports.  
    
 Requires at least two Pixelblaze Output Expander boards, compatible USB->Serial adapters and
 supported (by the Output Expander) addressable LED strips or strands. 
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details.   

*/

ExpanderVerse leds;
PBXSerial port1,port2;  // Create object from Serial class
PBXBoard b0,b1;
PBXDataChannel ch1,ch2,ch3,ch4,ch5,ch6,ch7;

int timer;
int pixelCount;
int frameCount = 0;

// roughly equivalent to Pixelblaze time()
float sawtooth(float ms) {
  return  ((float) millis() % ms)/ ms;  
}

void setup() {
  size(640,480);
  
  // First create an ExpanderVerse object to manage this display
  leds = new ExpanderVerse(this);
  
  // Get a list of available serial ports.  On my machine,
  // USB->Serial connectors are the only serial ports available. I'll
  // just take the first one...
 
  // Open the serial port. If the port isn't available, or doesn't support
  // the required datarate, openPort() will throw an exception.
  //
  // Use leds.listSerialPorts() to get a list of available ports on your machine
  // My two USB->Serial converters are always on COM4 and COM5.
  port1 = leds.openPort("COM4");
  port2 = leds.openPort("COM5");
  
  // add an expansion board to our serial port
  b0 = leds.addOutputExpander(port1,0);
  b1 = leds.addOutputExpander(port2,0);
  
  // This is every spare LED I had that wasn't attached to another project!
  // Add, delete, modify according to your setup.
  // We keep the channel handles around to test per-channel control features.
  // You don't need them just to run patterns.
  ch1 = leds.addChannelWS2812(b0,0,200,"RGB"); 
  ch2 = leds.addChannelWS2812(b0,1,200,"RGB"); 
  ch3 = leds.addChannelWS2812(b0,2,200,"RGB"); 
  ch4 = leds.addChannelAPA102(b1,0,300,2000000,"BGR");
  ch5 = leds.addChannelAPAClock(b1,1,200000);  
  ch6 = leds.addChannelWS2812(b1,2,256,"GRB"); 
  ch7 = leds.addChannelWS2812(b1,3,576,"GRBW");   

  // set Processing's color mode for whatever you find convenient. ExpanderVerse
  // will handle any required conversion.
  colorMode(HSB,1);
  
  // limit the brightness of our LEDs so we don't kill our power supply
  leds.setGlobalBrightness(0.3);
  
  timer = millis();
  pixelCount = leds.getPixelCount();
  println("pixelCount = ",pixelCount);
}

void draw() {
  float h,b;
  background(0);

  // generate a a waveform to move a colored band along our display
  float t1 = sawtooth(2000);   
   
  // set the hue of each pixel according to its position in the display,
  // and animate it with our sawtooth wave 
  for (PBXPixel p : leds.getPixelList()) {
    PVector n = new PVector();
    p.getNormalizedCoordinates(n);
    
    h = (t1+n.x) % 1;  
    b = abs(t1 - n.x); b = (b <= 0.05) ? 1 : 0;    

    p.setColor(color(h,1,b));
  }
  
  // periodically display the frame rate in the Processing console 
  // Processing's frame rate is normally capped at 60fps, and 30 when it's
  // running in the background.  You can change this with a call to the
  // frameRate() function although this is usually not necessary.   
  //

  frameCount++;  
  if (frameCount >= 100) {
//    println(frameRate);
    timer = millis();
    frameCount = 0;
  }

  leds.draw();
}
