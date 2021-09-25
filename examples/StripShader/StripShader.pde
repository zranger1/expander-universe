/*
 Expanderverse - Processing->USB->Pixelblaze Output Expander->Many LEDs!
 
 StripShader example - This example shows how to to use OpenGL shaders to draw
 on an LED strip. It also demonstrates the use of an offscreen drawing surface.

 Requires a Pixelblaze Output Expander board, a compatible USB->Serial adapter and
 a supported (by the Output Expander) strip or strand of addressable LEDs.
 
 NOTE: YOU WILL HAVE TO CONFIGURE THIS SKETCH FOR YOUR LED SETUP BEFORE RUNNING THIS
 SKETCH. See setup() below for details 

 9/20/21 JEM (ZRanger1)
*/
import processing.serial.*;
import pbxuniverse.*;

// Expanderverse - use Pixelblaze Output Expand to drive addressable LEDs!
// This example shows how to set up a matrix display.  It requires a 

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
  shaders[0] = loadShader("threestripes.glsl");
  shaders[0].set("resolution",(float)pixelCount,(float)1);    
  shaderText[0] = new String("Three Stripes, red, green and blue.");
  gamma[0] = 4;
  
  shaders[1] = loadShader("backdraft.glsl");
  shaders[1].set("resolution",(float)pixelCount,(float)1);    
  shaderText[1] = new String("Backdraft - Volumetric, ray marched 1D Fire. Total overkill!");  
  gamma[1] = 3;
  
  shaders[2] = loadShader("phasespace.glsl");
  shaders[2].set("resolution",(float)pixelCount,(float)1);    
  shaderText[2] = new String("Colorful sin/cos pattern");  
  gamma[2] = 2;
    
  shaders[3] = loadShader("newoasis.glsl");
  shaders[3].set("resolution",(float)pixelCount,(float)1);    
  shaderText[3] = new String("Oasis on your GPU!");  
  gamma[3] = 1;
  
  shaders[4] = loadShader("colormixing.glsl");
  shaders[4].set("resolution",(float)pixelCount,(float)1);    
  shaderText[4] = new String("RGB Color Blending");  
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

  sfx.set("time", millis() / 1000.0);
  
  // Set up our offscreen drawing surface and render our illuminated
  // cube into it
  pgLed.beginDraw();
  pgLed.shader(sfx);
  pgLed.rect(0,0,pixelCount,1);

  pgLed.endDraw();
  pgLed.loadPixels();
   
  // setPixelsFromImage() transfers PImage data to your LED pixels. This technique
  // allows you to use all of Processing's graphical power and its ability to offload
  // graphics computation to the GPU via OpenGl.  
  leds.setPixelsFromImage(0,pixelCount,pgLed.pixels);   
}

void setup() {
  size(640,480,P3D);
  noSmooth();
  
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
  
  // create a single channel (on to board channel 1) with 300 APA102 LEDs 
  // using color order BGR and running at 4Mhz, and attach it to our 
  // previously created expander board.
  // We don't actually need to keep the channel object (ch1) around, but
  // if we do, we can use it to control per channel brightness and gamma
  // correction.  (The same is true at the expander board level.)
  ch1 = leds.addChannelAPA102(b0,0,300,4000000,"BGR"); 
  
  // (to use WS2812-protocol LEDs instead, substitute:
  // ch1 = leds.addChannelWS2812(b0,0,300,"BGR");    
  
  // if using APA102-style LEDs, you also need to dedicate one channel on the
  // expander to the APA's clock signal.  We'll create a clock channel on
  // board channel 2.  The clock channel should be set to the same speed as
  // the LED channel, although the output expander will have the last word.
  // If WS28xx LEDs are connected to the expander board, all APA102s will run
  // at the WS's 800khz clock speed.
  leds.addChannelAPAClock(b0,1,4000000);
    
  // limit the maximum brightness to keep the power supply happy.  If you know
  // your LEDs can run brighter, by all means, turn it up.
  leds.setGlobalBrightness(0.75);
  leds.setGammaCorrection(4);
   
  // after all boards and channels have been added, save the pixel count so we
  // can use it for calculation while drawing.  
  pixelCount = leds.getPixelCount();
  
  // Create our offscreen graphics object and set it up for rendering.  Either
  // P2D or P3D will work here since we're only using it as a canvas for the 
  // shader.
  pgLed = createGraphics(pixelCount,1,P2D);
  pgLed.noSmooth();  
  pgLed.noStroke();  
  
  // load and intialize all the shaders we'll be using

  shaderSetup();
  timer = millis();  
}

void draw() {
  background(0);
  
  // display help text
  int yPos = height / 4;
  background(0);
  textSize(14*displayDensity());
  text("Use '+' and '-' keys to change shaders",10,yPos);
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
