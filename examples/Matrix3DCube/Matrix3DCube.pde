/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 Matrix3DCube example - This example shows how to set up a matrix display and
 draw an illuminated 3D object using Processing's 3D graphics API. 
 
 It also demonstrates the use of an offscreen drawing surface, ideal for this purpose
 because the number of LEDs you're driving will usually be far below the number
 of pixels on even a small screen region. 
 
 9/20/21 JEM (ZRanger1)
*/
import processing.serial.*;
import pbxuniverse.*;

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
int frameCount = 0;
float theta = 0;
float theta2 = 0;

PGraphics pgLed;
int timer;

// draws a 3D cube on the LED display.  The cube is white.  
// Color comes from 3 different types of Processing lights.
void drawLitCube() {
  
  // Set up our offscreen drawing surface and render our illuminated
  // cube into it
  pgLed.beginDraw();
  pgLed.colorMode(HSB,1);     

  pgLed.background(0);
  pgLed.noStroke();  

  pgLed.translate(mWidth / 2,mHeight / 2,-mWidth+2);     

  // Lighting...
  // Red point light on the right
  pgLed.pointLight(0, 1, 1, mWidth+4, 1, 8);
  
  // Blue directional light from the left
  pgLed.directionalLight(0.6667, 1, 1, 1, 0, 0); 

  // Green spotlight from the front
  pgLed.spotLight(0.333, 1, 1, 0, 4, 200, 0, 0, -1,PI / 2, 200); 

  // The cube is rotating. Of course it is. They always do!
  pgLed.rotateX(-theta2);  
  pgLed.rotateY(theta);
  pgLed.rotateZ((theta+theta2)/2);

  pgLed.shininess(10.0);
  pgLed.box(14);

  pgLed.endDraw();
  
  // get pixels from the offscreen surface 
  PImage img = pgLed.get();
  img.loadPixels();

  // setPixelsFromImage() transfers PImage data to your LED pixels. This technique
  // allows you to use all of Processing's graphical power and its ability to offload
  // graphics computation to the GPU via OpenGl.  
  leds.setPixelsFromImage(0,pixelCount,img.pixels);

  // update rotation angles
  theta = (theta + 0.02) % TWO_PI;
  theta2 = (theta2 + 0.0141) % TWO_PI;     
}

void setup() {
  size(640,480,P3D);
  
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

  // set Processing's color mode for whatever you find convenient. You 
  // can even change colorMode at any time.  ExpanderVerse and Processing
  // will handle any required conversion.
  colorMode(HSB,1);
    
  // I have no idea how much power is connected to your LEDs, so let's
  // limit the brightness to keep the power supply happy.  If you know
  // your LEDs can run brighter, by all means, turn it up.
  leds.setGlobalBrightness(0.5);
  leds.enableGammaCorrection();
  
  // Use the setMatrixMap() helper function to create an mWidth * mHeight mapping for our
  // pixels, with 'zigzag' wiring.  You could also do this manually using
  // leds.setMapCoordinates(index,x,y,z), but for simple matrices this is a handy shortcut
  // parameters are: setMatrixMap(startIndex,width,height,zigzag flag);
    leds.setMatrixMap(0,mWidth,mHeight,true); 
  
  // after all boards and channels have been added, save the pixel count so we
  // can use it for calculation while drawing.  
  pixelCount = leds.getPixelCount();
  
  // Create our offscreen graphics object and set it up for 3D rendering
  pgLed = createGraphics(mWidth,mHeight,P3D); 
  
  // The LED output looks much better if you use the highest available smoothing
  // on very low-res surfaces.  Rendering a few thousand pixels is so little work 
  // for a modern GPU that there's no fps penalty for doing so.
  pgLed.smooth(8);  
  timer = millis();
}

void draw() {
  background(0);
 
  // This pattern works by using Processing's 3D API to draw to an offscreen 
  // surface.  It then captures those pixels as a PImage and sends them to
  // the LED display. 
  drawLitCube();

  // Periodically print the frame rate to the Processing console. 
  // Processing's frame rate is normally capped at 60fps, and 30 when it's
  // running in the background.
  // You can change this with a call to the frameRate() function, although
  // it isn't usually necessary.  
  // The maximum output framerate is limited by the speed of communication with
  // the expander board.   
  frameCount++;  
  if (frameCount >= 100) {
    println(frameRate);
    frameCount = 0;
  }
   
  leds.draw();
}
