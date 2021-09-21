/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 MatrixVideo example - This example shows how to set up a matrix display and
 display a portion of a video on it.  Requires the processing.video library. 
 If you need it, you can install from the menu: 'Sketch/Import Library.../Add Library'
 
 9/20/21 JEM (ZRanger1)
*/
import processing.serial.*;
import pbxuniverse.*;
import processing.video.*;

// Expanderverse - use Pixelblaze Output Expand to drive addressable LEDs!
// This example shows how to set up a matrix display.  It requires a 

// Set the dimensions of your matrix here
// NOTE: 'height' and 'width' are reserved words in Processing
int mHeight = 16;
int mWidth = 16;

// 'Handles' for the objects we'll be creating.  
ExpanderVerse leds;
PBXSerial port;
PBXBoard b0;
PBXDataChannel ch1;

// global values used for drawing
int pixelCount;

Movie bigscreen;
int timer;

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
  
  // create a single channel (channel 0), of WS2812 LEDs with color
  // order GRB and attach it to our previously created expander board.
  // We don't actually need to keep the channel object (ch1) around, but
  // if we do, we can use it to control per channel brightness and gamma
  // correction.  (The same is true at the expander board level.)
  ch1 = leds.addChannelWS2812(b0,0,mHeight * mWidth,"GRB"); 
    
  // It's a a good idea to limit the brightness to keep your power supply happy 
  // while testing new things. Video though, does tend to look better with a 
  // little more brightness. If you know your power supply can handle it, by all
  // means, turn it up a little.
  leds.setGlobalBrightness(0.75);
  leds.enableGammaCorrection();
  
  // Use the setMatrixMap() helper function to create an mWidth * mHeight mapping for our
  // pixels, with 'zigzag' wiring.  You could also do this manually using
  // leds.setMapCoordinates(index,x,y,z), but for simple matrices this is a handy shortcut
  // parameters are: setMatrixMap(startIndex,width,height,zigzag flag);
    leds.setMatrixMap(0,mWidth,mHeight,true); 
  
  // after all boards and channels have been added, save the pixel count so we
  // can use it for calculation while drawing.  
  pixelCount = leds.getPixelCount();
  
  // Create our movie player.

 
  // open movie files and start playback
  bigscreen = new Movie(this,"dinosaur.mp4");
  bigscreen.loop();
}

void draw() {
  // render the current movie frame(s) to the on-screen window
  image(bigscreen,0,0,width,height);
  
  // Here, I use Processing's image() method downscale the frame to the size of the 
  // LED display. It's fast and surprisingly, it's not terrible, although the demo video is
  // pretty low-res to begin with.
  image(bigscreen,0,0,mWidth,mHeight);
  
  // get pixels from the movie display   
  PImage img = get(0,0,mWidth,mHeight);
  img.loadPixels();

  // setPixelsFromImage() transfers PImage data to your LED pixels. This technique
  // allows you to use all of Processing's graphical power and its ability to offload
  // graphics computation to the GPU via OpenGl.  
  leds.setPixelsFromImage(0,pixelCount,img.pixels);
  leds.draw();
}

// load incoming movie frames as they're decoded by GStreamer
void movieEvent(Movie m) {
  m.read();
}
