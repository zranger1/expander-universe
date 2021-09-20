import processing.serial.*;
import pbxuniverse.*;

// Use the ExpanderVerse library to send data to LED!
// This example displays a simple pattern on several strands/strips

ExpanderVerse leds;
PBXSerial port1,port2;  // Create object from Serial class
PBXBoard b0,b1;
PBXDataChannel ch1,ch2,ch3,ch4,ch5,ch6,ch7;

int timer;
int pixelCount;
int frameCount = 0;

// roughly equivalent to Pixelblaze time()
float Sawtooth(float ms) {
  return  ((float) millis() % ms)/ ms;  
}

void setup() {
  size(1000,1000);
  
  // First create an ExpanderVerse object to manage this display
  leds = new ExpanderVerse(this);
  
  // Get a list of available serial ports.  On my machine,
  // USB->Serial connectors are the only serial ports available. I'll
  // just take the first one...
 
  // Open the serial port. If the port isn't available, or doesn't support
  // the required datarate, openPort() will throw an exception

  port1 = leds.openPort("COM4");
  port2 = leds.openPort("COM5");
  
  // add an expansion board to our serial port
  b0 = leds.addOutputExpander(port1,0);
  b1 = leds.addOutputExpander(port2,0);
  
  
  // Add every LED we can find that's not currently attached to something...
  // (we keep the handles around to test per-channel features.  You don't
  // really have to keep them)
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
  
  // I'm using this sketch as a test to see how many LEDs I can successfully run.
  // limit the brightness of our LEDs so we don't kill our power supply
  leds.setGlobalBrightness(0.3);
  
  
  timer = millis();
  pixelCount = leds.getPixelCount();
  println("pixelCount = ",pixelCount);
}

float h1 = 0;
void draw() {
  float h,b;

  // generate a couple of sawtooth waveforms at various frequencies
  float t1 = Sawtooth(4000);  // to move band along display 
   
  // set the hue of each pixel to a shade of cyan/blue/purple according to
  // its position in the display, animated by our sawtooth wave 
  for (int i = 0; i < pixelCount; i++) {
    float pct = (float(i) / pixelCount);
    h = (t1+pct) % 1;  
    b = abs(t1 - pct); b = (b <= 0.061) ? 1-b : 0;
    
    leds.setPixel(i,color(h,1,b));
//    leds.setPixel(i,color(h1,1,1));
  }  
  
  // periodically display the frame rate in the Processing console 
  // Processing's frame rate is normally capped at 60fps, and 30 when it's
  // running in the background.  You can change this with a call to the
  // frameRate() function although this is usually not necessary.   
  //

  frameCount++;  
  if (frameCount >= 100) {
    h1 = (h1 + 0.618) % 1;
    println(frameRate);
    timer = millis();
    frameCount = 0;
  }

  leds.draw();
}
