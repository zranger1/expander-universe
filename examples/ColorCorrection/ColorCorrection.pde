/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 Color Correction  - This example uses the setColorCorrection() API and
 DrawMode.ENHANCED to match whites between two different strips of different types.
 
 The demo was originally set up with (Channel 1) a 16x16 BTF Lighting WS2812 
 matrix and (Channel 2) a 200 LED strand of Brizlabs RGB Fairy Lights, which 
 are not really WS2812s, but speak that protocol. These LEDs have *very* 
 different color characteristics, and make a nice test case. 
 
 You'll have to configure it in setup() for your own lights, of course. 

 Software:
 Requires the ControlP5 contributed library for UI.  Install through
 the Sketch/Import Library... menu if you don't already have it.
  
 Hardware:
 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 two supported (by the Output Expander) strips, strands or other assemblies of
 of addressable LEDs.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details 
 
 9/20/21 JEM (ZRanger1)
*/

import processing.serial.*;
import controlP5.*;
import pbxuniverse.*;

// 'Handles' for the objects we'll be creating.
ControlP5 ui;
ExpanderVerse leds;
PBXSerial port;
PBXBoard b0;
PBXDataChannel ch1,ch2;

// color correction factors for our two channels
float r1,g1,b1;
float r2,g2,b2;
float lastc1,lastc2;

void setup() {
  size(640,480);
  
  // initialize color correction factors 
  r1 = g1 = b1 = 1.0;
  r2 = g2 = b2 = 1.0;
  lastc1 = r1 + b1 + g1;
  lastc2 = r2 + b2 + g2;
  
  // set up UI sliders
  configureUI();
 
  // Create an ExpanderVerse object to manage this display
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
  
  // NOTE:  You will need to change the channel configurations for your
  // own setup. 
  //
  // Create the first channel with 256 LEDs. (On my test setup, this is a
  // 16x16 BTF Lighting matrix. It's on board channel 0.)
  ch1 = leds.addChannel(b0,ChannelType.WS2812,0,256,"GRB");
  
  // Create the second channel, with 200 LEDs. (This is a string of
  // Brizlab fairy lights on physical board channel 2 because the wire I have
  // soldered to channel 1 doesn't fit the strand's connector well.)   
  ch2 = leds.addChannel(b0,ChannelType.WS2812,2,200,"RGB");
   
  // Limit the brightness to keep  power supplies happy.  If you know
  // your LEDs can run brighter, by all means, turn it up.
  leds.setGlobalBrightness(0.5);  
  
  // Turn system gamma correction off.   
  leds.setGammaCorrection(1);
  
  // set enhanced drawing mode to enable color correction.
  leds.setDrawMode(DrawMode.ENHANCED); 
}

void draw() {
   fill(getAdjustedColor(r1,g1,b1));
   rect(0,0,width/2,height);
   
   fill(getAdjustedColor(r2,g2,b2));
   rect(width/2,0,width/2,height);  

   // see if the correction values have changed since last frame
   float c = changeHash(r1,g1,b1);
   if (c != lastc1) {
     ch1.setColorCorrection(r1,g1,b1);
     lastc1 = c;
   }
   
   c = changeHash(r2,g2,b2);
   if (c != lastc2) {
     ch2.setColorCorrection(r2,g2,b2);
     lastc2 = c;
   }  

  // set the first 16 pixels of both strips to pure white
  // we limit the number of pixels here to control power usage,
  // avoid melting hot glue, etc.  
  for (int i = 0; i < 16; i++ ) {
    leds.setPixel(i,color(255,255,255));
    leds.setPixel(i+ch1.getPixelCount(),color(255,255,255));   
  }
     
  // call this to send data to the LEDs 
  leds.draw();
}

// get adjusted color
color getAdjustedColor(float r, float g, float b) {
  return color(r * 255, g * 255, b * 255);  
}

// compute a value that will be different on any r,g,b slider
// change
float changeHash(float r,float g, float b) {
  return r + (10 * g) + (100 * b);
}

// set up RGB adjustment sliders for our two channels
void configureUI() {
  int xpos,ypos,spacing,xstart; 
  float colorMin = 0;

  ui = new ControlP5(this);
  xstart = 80;
  ypos = 80;  
  spacing = 60;  
  
  // controls for first channel  
  xpos = xstart;  
  ui.addSlider("r1")
  .setPosition(xpos,ypos)
  .setSize(30,200)
  .setRange(colorMin,1)
  .setValue(r1)
  .setColorBackground(color(100,0,0))
  .setColorForeground(color(200,0,0))
  .setColorActive(color(255,0,0))  
  .setLabelVisible(false)  
  ;
  
  xpos += spacing;
  ui.addSlider("g1")
  .setPosition(xpos,ypos)
  .setSize(30,200)
  .setRange(colorMin,1)
  .setValue(g1)
  .setColorBackground(color(0,100,0))
  .setColorForeground(color(0,200,0)) 
  .setColorActive(color(0,255,0))
  .setLabelVisible(false)  
  ;  
  
  xpos += spacing;
  ui.addSlider("b1")
  .setPosition(xpos,ypos)
  .setSize(30,200)
  .setRange(colorMin,1)
  .setValue(b1)
  .setColorBackground(color(0,0,100))   
  .setColorForeground(color(0,0,180))  
  .setColorActive(color(0,0,255))
  .setLabelVisible(false)
  ;  

  // controls for secod channel;
  xpos = xstart + width / 2;    
  ui.addSlider("r2")
  .setPosition(xpos,ypos)
  .setSize(30,200)
  .setRange(colorMin,1)
  .setValue(r2)
  .setColorBackground(color(100,0,0))
  .setColorForeground(color(200,0,0))
  .setColorActive(color(255,0,0))   
  .setLabelVisible(false)  
  ;
  
  xpos += spacing;
  ui.addSlider("g2")
  .setPosition(xpos,ypos)
  .setSize(30,200)
  .setRange(colorMin,1)
  .setValue(g2)
  .setColorBackground(color(0,100,0))
  .setColorForeground(color(0,200,0)) 
  .setColorActive(color(0,255,0))
  .setLabelVisible(false)  
  ;  
  
  xpos += spacing;
  ui.addSlider("b2")
  .setPosition(xpos,ypos)
  .setSize(30,200)
  .setRange(colorMin,1)
  .setValue(b2)
  .setColorBackground(color(0,0,100))   
  .setColorForeground(color(0,0,180))  
  .setColorActive(color(0,0,255))      
  .setLabelVisible(false)    
  ;     
}
