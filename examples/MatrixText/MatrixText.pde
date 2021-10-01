/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 MatrixText example - This example shows how to set up a matrix display and
 draw characters and scrolling text.
 
 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 a supported (by the Output Expander) addressable LED matrix.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details.
 
 9/20/21 JEM (ZRanger1)
*/
import processing.serial.*;
import pbxuniverse.*;

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

String demoText;
int demoTextWidth;
int strPos;
int szFont;
int textTimer;
int patternTimer; 

// Display a string, one character at a time
void drawSomeText() {
  textAlign(CENTER,CENTER); 
  
  // set font color based on position in string
  fill((float) strPos/demoText.length(),1,1);
  
  // draw antialiased character
  text(demoText.charAt(strPos),mWidth/2,-2+mHeight/2);
  
  PImage img = get(0,0,mWidth,mHeight);
  img.loadPixels();
  
// setPixelsFromImage() transfers PImage data to your LED pixels. This technique
// allows you to use all of Processing's graphical power and its ability to offload
// graphics computation to the GPU via OpenGl.  
  leds.setPixelsFromImage(0,pixelCount,img.pixels);  
  
  // rate control -- 1 char every 250 ms.
  if ((millis() - textTimer) > 250) {
    strPos = (strPos + 1) % demoText.length();
    textTimer = millis();
  } 
}

// Scroll a text string on our LED display
void scrollSomeText() {
  textAlign(LEFT,CENTER); 
  
  // gold/orange - should be easily visisble on most backgrounds
  fill(.075,1,1);
  
  // draw string
  text(demoText,0,-2+mHeight/2);
  
  PImage img = get(strPos,0,mWidth,mHeight);
  img.loadPixels();
  
// setPixelsFromImage() transfers PImage data to your LED pixels. This technique
// allows you to use all of Processing's graphical power and its ability to offload
// graphics computation to the GPU via OpenGl.  
  leds.setPixelsFromImage(0,pixelCount,img.pixels);
  
  // rate control -- move scroll pos 
  if ((millis() - textTimer) > 80) {
    strPos = (strPos + 1) % demoTextWidth;
    textTimer = millis();
  }   
}

void setup() {
  size(640,480);
  smooth(8);  // may as well use *all* the anti-aliasing
  
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
  ch1 = leds.addChannel(b0,ChannelType.WS2812,0,mHeight * mWidth,"GRB"); 

  // set Processing's color mode for whatever you find convenient. You 
  // can even change colorMode at any time.  ExpanderVerse and Processing
  // will handle any required conversion.
  colorMode(HSB,1);
    
  // Limit the brightness to keep the LED power supply happy.  If you know
  // your LEDs can run brighter, by all means, turn it up.
  leds.setGlobalBrightness(0.3);
  
  // Use the setMatrixMap() helper function to create an mWidth * mHeight mapping for our
  // pixels, with 'zigzag' wiring.  You could also do this manually using
  // leds.setMapCoordinates(index,x,y,z), but for simple matrices this is a handy shortcut
  // parameters are: setMatrixMap(startIndex,width,height,zigzag flag);
   leds.setMatrixMap(0,mWidth,mHeight,true); 
  
  // after all boards and channels have been added, save the pixel count so we
  // can use it for calculation while drawing.  
  pixelCount = leds.getPixelCount();
  
  // set some values for our text demo
  demoText = "Hi There! We are glad to see you. 12345.;+= ";
  strPos = 0;
  szFont = mHeight - 2;  // may need to adjust this for your display
  
  textSize(szFont);
  
  // textWidth is ...strange... on high DPI displays.  You may
  // have to play with it a little to get the right width
  demoTextWidth = (int) textWidth(demoText) / displayDensity();   
  textTimer = patternTimer = millis();  
}

int patternSwitch = 0;
void draw() {
  // slowly change background colors to show anti-aliasing vs. background
  float bgHue = ((float) (millis() % 20000)) / 20000.0;
  background(bgHue,1,0.075);

  // Both pattern variants draw text by rendering it to the Processing canvas,
  // then settingthe LED pixels from the resulting PImage.  This allows OpenGL to do 
  // all the hard work of pixel calculation, antialiasing, etc.
  if (patternSwitch == 0 ) {
    scrollSomeText();
  }
  else {
    drawSomeText();
  }
  
  // send data to the LEDs
  leds.draw();
  
  // switch from scrolling to single char drawing periodically
  if ((millis() - patternTimer) > 20000) {
    patternSwitch = (patternSwitch + 1) % 2;
    strPos = 0;    
    patternTimer = millis();
  }  
}
