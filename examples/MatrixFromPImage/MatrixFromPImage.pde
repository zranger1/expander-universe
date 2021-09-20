/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 MatrixFromPImage example - This example shows how to set up a matrix display and
 draw to it by transferring pixel data from a Processing image object (PIMage).  With
 this technique, you draw on a canvas with Processing, as usual, then use Processing's
 get() method to get pixel data from a portion of that canvas.
 
 A single method call then transfers that data to your LED pixels. Using this technique
 means that you can use all of Processing's graphical power and its ability to offload some computation
 to the GPU via OpenGl, to render to your LED display. Geometry, fonts, video - whatever
 Processing can draw - is available for your use.
 
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
int pixelCount;

int frameCount = 0;

String demoText = "Hi There! We are glad to see you. 12345.;+= ";
int strPos = 0;
int szFont = mHeight - 2;  // may need to adjust this for your display
int timer = millis();

// Display a string, one character at a time
void drawSomeText() {

  // set font color based on position in string
  fill((float) strPos/demoText.length(),1,1);
  
  // draw antialiased character
  text(demoText.charAt(strPos),mWidth/2,-2+mHeight/2);
  
  // rate control -- 1 char every 250 ms.
  if ((millis() - timer) > 250) {
    strPos = (strPos + 1) % demoText.length();
    timer = millis();
  } 
}

void setup() {
  size(640,480);
  textSize(szFont);
  textAlign(CENTER,CENTER);  
  
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
  leds.setGlobalBrightness(0.25);
  
  // Use the setMatrixMap() helper function to create an mWidth * mHeight mapping for our
  // pixels, with 'zigzag' wiring.  You could also do this manually using
  // leds.setMapCoordinates(index,x,y,z), but for simple matrices this is a handy shortcut
  // parameters are: setMatrixMap(startIndex,width,height,zigzag flag);
    leds.setMatrixMap(0,mWidth,mHeight,true); 
  
  // after all boards and channels have been added, save the pixel count so we
  // can use it for calculation while drawing.  
  pixelCount = leds.getPixelCount();
   
}

void draw() {
  background(0.6667,1,0.1);
 
  // there are two ways to use mapped coordinates in ExpanderVerse.  The first
  // is the Pixelblaze-like method of looping through every pixel in the display
  // and rendering it according its mapped coordinates.
  
  //int timer = (millis() / 1000) % 10;

  
  // The other rendering method is to draw whatever you want on a Processing
  // canvas, then set the Pixels directly from a PImage.  This allows you to 
  // use your computer's GPU to do much of the work of pixel calculation, and
  // is a great fit for matrix displays.
   
  drawSomeText();
  PImage img = get(0,0,mWidth,mHeight);
  img.loadPixels();
  leds.setPixelsFromImage(0,pixelCount,img.pixels);


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
