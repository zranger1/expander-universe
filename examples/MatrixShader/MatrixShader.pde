/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 MatrixShader example - This example shows how to set up a matrix display and
 use an OpenGL fragment shader to draw on your LED display.  It also demonstrates
 the use of an offscreen drawing surface.
 
 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 a supported (by the Output Expander) addressable LED matrix.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details.  
 
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
PGraphics pgLed;

int nShaders = 5;
int shaderIndex = 0;
PShader sfx = null;
PShader[] shaders;
String[] shaderText;
float gamma[];
int timer;

// load shader files so the user can use + and - to select them
void shaderSetup() {
  shaders = new PShader[nShaders];
  shaderText = new String[nShaders];
  gamma = new float[nShaders];
   
  // each shader has an associated gamma correction level.
  // which will be set when the shader is selected
  shaders[0] = loadShader("junglegym.glsl");
  shaderText[0] = new String("Jungle Gym");
  gamma[0] = 4;
  
  shaders[1] = loadShader("backdraft.glsl");
  shaderText[1] = new String("Backdraft");  
  gamma[1] = 2;
  
  shaders[2] = loadShader("raytracingdemo.glsl");
  shaderText[2] = new String("Ray Tracing Demo");  
  gamma[2] = 3;
   
  shaders[3] = loadShader("simplexnoisenot.glsl");
  shaderText[3] = new String("Non-Simplex Noise Demo");  
  gamma[3] = 5;
  
  shaders[4] = loadShader("colormixing.glsl");
  shaderText[4] = new String("Color Mixing");  
  gamma[4] = 3;
} 

// Sends the output of a GLSL fragment shader to the LED display.
void doShader() {
  // change shaders if necessary
  if (sfx != shaders[shaderIndex]) {
    sfx = shaders[shaderIndex];
    leds.setGammaCorrection(gamma[shaderIndex]);
  }
  
  // set shader parameters
  sfx.set("resolution",(float)mWidth,(float)mHeight);  
  sfx.set("time", millis() / 1000.0);
  
  // Set up our offscreen drawing surface and render our illuminated
  // cube into it
  pgLed.beginDraw();
  pgLed.noStroke();
  pgLed.shader(sfx);
  pgLed.rect(0,0,mWidth,mHeight);

  pgLed.endDraw();
  pgLed.loadPixels();
   
  // setPixelsFromImage() transfers PImage data to your LED pixels. This technique
  // allows you to use all of Processing's graphical power and its ability to offload
  // graphics computation to the GPU via OpenGl.  
  leds.setPixelsFromImage(0,pixelCount,pgLed.pixels);   
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
    
  // limit the maximum brightness to keep the power supply happy.  If you know
  // your LEDs can run brighter, by all means, turn it up.
  leds.setGlobalBrightness(0.5);
  leds.setGammaCorrection(4);
  
  // Use the setMatrixMap() helper function to create an mWidth * mHeight mapping for our
  // pixels, with 'zigzag' wiring.  You could also do this manually using
  // leds.setMapCoordinates(index,x,y,z), but for simple matrices this is a handy shortcut
  // parameters are: setMatrixMap(startIndex,width,height,zigzag flag);
    leds.setMatrixMap(0,mWidth,mHeight,true); 
  
  // after all boards and channels have been added, save the pixel count so we
  // can use it for calculation while drawing.  
  pixelCount = leds.getPixelCount();
  
  // Create our offscreen graphics object and set it up for rendering.  Either
  // P2D or P3D will work here since we're only using it as a canvas for the 
  // shader.
  pgLed = createGraphics(mWidth,mHeight,P2D);
  
  // load and intialize all the shaders we'll be using
  shaderSetup();
  
  timer = millis();
}

void draw() {
  background(0);
  
  int yPos = height / 4;
  background(0);
  textSize(14*displayDensity());
  text("Use '+' and '-' keys to change patterns",10,yPos);
  yPos += 20 * displayDensity();
  textSize(10 * displayDensity());
  text(shaderText[shaderIndex],10,yPos);  
 
  // This pattern works by using an OpenGL fragment shader to draw to an offscreen 
  // surface.  It then captures those pixels as a PImage and sends them to
  // the LED display. 
  doShader();
  
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

// handle shader switching keys for demo
void keyPressed() {
  switch (key) {
    case '+':
      shaderIndex = (shaderIndex + 1) % nShaders;
      break;
    case '-':
      shaderIndex--;
      if (shaderIndex < 0) shaderIndex = nShaders - 1;
      break;
  }
}
