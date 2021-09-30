/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 Color Correction  - This example uses the setColorCorrection() API and
 DrawMode.ENHANCED to match whites between two different strips of different types.
 
 The demo was originally set up with (Channel 1) a 16x16 BTF Lighting WS2812 
 matrix and (Channel 2) a 200 LED strand of Brizlabs RGB Fairy Lights, which 
 are not really WS2812s, but speak that protocol. These LEDs have *very* 
 different color characteristics, and make a nice test case. 
 
 You'll have to configure it in setup() for your own lights, of course. 
 
 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 two supported (by the Output Expander) strips, strands or other assemblies of
 of addressable LEDs.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details 
 
 9/20/21 JEM (ZRanger1)
*/

import processing.serial.*;
import pbxuniverse.*;

// 'Handles' for the objects we'll be creating.  
ExpanderVerse leds;
PBXSerial port;
PBXBoard b0;
PBXDataChannel ch1,ch2;

// color correction factors for our two channels
float r1,g1,b1;
float r2,g2,b2;

void setup() {
  size(640,480);
  
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
  
  // create the first channel with 256 LEDs. On my setup, this is a
  // 16x16 BTF Lighting matrix. It's on board channel 0.
  ch1 = leds.addChannel(b0,ChannelType.WS2812,0,256,"GRB");
  
  // create the second channel, with 200 LEDs. This is a string of
  // Brizlab fairy lights. (Using board channel 2 because the wire I have
  // soldered to channel 1 on this expander board is going bad.)   
  ch2 = leds.addChannel(b0,ChannelType.WS2812,2,200,"RGB");
  
  // initialize color correction factors 
  r1 = g1 = b1 = 1.0;
  r2 = g2 = b2 = 1.0;
  b2 = 0.8;
   
  // Limit the brightness to keep  power supplies happy.  If you know
  // your LEDs can run brighter, by all means, turn it up.
  leds.setGlobalBrightness(0.5);  
  
  // Turn system gamma correction off.   
  leds.setGammaCorrection(1);
  
  // set enhanced drawing mode to enable color correction.
  leds.setDrawMode(DrawMode.ENHANCED); 
  ch2.setColorCorrection(r2,g2,b2);
}


int frameCount = 0;
void draw() {
  background(0);

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

// handle pattern switching keys for demo
void keyPressed() {
  switch (key) {
    case '+':
      break;
    case '-':
      break;
  }
}
