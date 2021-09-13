package pbxuniverse;

import processing.core.*;
import java.util.*;

/*
TODO - THE MASTER TODO LIST!
  - easy matrix mode?  
  - PImage/PGraphics transfer
  - Disable/enable channels??      
  - put serial transmisison in its own thread so we can get right back to rendering
  - what do we need to do to enable multithread/multicore pixel rendering?
  - enable GPU-based rendering w/matrix/PImage thing
  - simplify and automate the display creation process
  - write more damn examples!
  - optimize getPixelCount() - it doesn't need to actually recheck unless config has changed  
*/	        

/**
 * Expander Universe - ExpanderVerse Object:
<p>
 To build a display:
 <p>
 <li>create an ExpanderVerse object
 <li>open your serial ports with OpenPort <p>
 <li>use AddOutputExpander(PBXSerial) to add output expanders to the serial ports
 <li>use the appropriate AddChannel(PBXBoard) variant to add output channels.

 <p>
 Pixel index, order and offsets will be determined by the order in which you 
 add the boards.  You can add x,y,z mapping after all the channels are created.
 
 <li>use the SetPixel-ish functions to set your LEDs
 <li>call Expanderverse.draw() to render all the pixels when set.    
      
 */

public class ExpanderVerse {
	PApplet pApp;
	int pixelCount;
	ArrayList<PBXPixel> pixels;
	LinkedList<PBXBoard> boards;
	LinkedList<PBXSerial> ports;
	
	// SYSTEMWIDE Global Variables/
	// TODO - get bloody rid of these things.

	 public final static String VERSION = "##library.prettyVersion##";
	 float globalBrightness = (float) 0.2;
	 boolean gammaCorrection = false;
	 byte[] levelTable = new byte[256];	
	 
	 
	public ExpanderVerse(PApplet parent) {
		pApp = parent;

		//TODO - setGlobalBrightness(0.3);
		pixels = new ArrayList<PBXPixel>();
		boards = new LinkedList<PBXBoard>();  
		ports = new LinkedList<PBXSerial>();
		welcome();
	}

	// Helper function - return a list of all the names of serial
	// ports attached to this computer
	public String[] listSerialPorts() {
		return PBXSerial.list();
	}

	// attempt to open specified serial port and return
	// object handle.  This handle can be passed to expansion board
	// managment functions.
	public PBXSerial openPort(String portName) {
		PBXSerial p = new PBXSerial(pApp, portName);
		ports.add(p);
		return p;
	}

	// create boards first, get all hw config stuff set there,
	// then add to display.  This fn can generate board GUID and 
	// figure out pixel offsets by taking each board and channel in 
	// order.  (offset can bet set to OFFSET_AUTO);
	public PBXBoard addOutputExpander(PBXSerial port, int boardNo) {
		PBXBoard b = new PBXBoard(this,port,boardNo);
		boards.add(b);
		return b;
	}

	public PBXBoard addOutputExpander(PBXSerial port) {
		return addOutputExpander(port,0);
	}

	// Create channel for RGB or RGBW WS2812/Neopixel class LEDs
	public PBXDataChannel addChannelWS2812(PBXBoard board,int chNumber,int pixelCount,String colorString) { 
		PBXDataChannel ch = new PBXChannelWS2812(board,(byte) chNumber, pixelCount,colorString);
		createPixelBlock(ch,pixelCount); 		
		board.addChannel(ch);

		return ch;
	}

	// Create data channel for APA102/Dotstar LEDs
	public PBXDataChannel addChannelAPA102(PBXBoard board,int chNumber,int pixelCount,int frequency,String colorString) {   
		PBXDataChannel ch = new PBXChannelAPA102(board,(byte) chNumber,pixelCount,frequency,colorString);
		createPixelBlock(ch,pixelCount);		
		board.addChannel(ch);

		return ch;
	}

	// Create clock channel for APA102/Dotstar LEDs
	// if you use APA102s, you'll need to dedicate one clock channel.
	// TODO - should I just set this up w/the first apa data channel so the user doesn't have
	// to worry about it?  Probably.
	public PBXDataChannel addChannelAPAClock(PBXBoard board,int chNumber,int frequency) {     
		PBXDataChannel ch = new PBXChannelAPAClock(board,(byte) chNumber,frequency);
		board.addChannel(ch);
		return ch;
	}

	public void createPixelBlock(PBXDataChannel ch, int pixelCount) {
		for (int i = 0; i < pixelCount; i++) {
			PBXPixel pix = new PBXPixel(pApp.color(0), ch, i);
			pixels.add(pix);      
		}
	}
	
	/**
	 * Gets the total number of pixels associated with this ExpanderVerse object 
	 * @return pixel count
	 */
	public int getPixelCount() {
		int n = 0;
		for (PBXBoard b : boards) { n += b.getPixelCount(); }
		
		return n;
	}

	// set global brightness and rebuilt byte translation table
	// for brightness and gamma.  (Gamma correction by cubing the
	// original 
	public void setGlobalBrightness(float b) {
		globalBrightness = PApplet.constrain(b,(float) 0.0,(float) 1.0);
	}

	public float getGlobalBrightness() {
		return globalBrightness;
	}
	
	public void enableGammaCorrection() {
		for (PBXBoard b : boards) { b.enableGammaCorrection(); }			
	}
	
	public void disableGammaCorrection() {
		for (PBXBoard b : boards) { b.disableGammaCorrection(); }			
	}
	
	public void setPixel(int index,int c) {
		pixels.get(index).setColor(c);
	}

	// used after all channels have been added to reorder
	// a section of the overall display into zigzag-wired 2D matrix
	// This is the most common wiring setup used for matrix displays, and 
	// reordering allows you to do a linear dump of a PImage taken from a 
	// 2D region into the LED buffer without having to worry about coordinate
	// translation. 
	public void setMatrixIndexOrder(int xSize, int ySize) {
		// TODO - not yet implemented
	}

	// Finally!  Actually send something to the LEDs.
	public void draw() {

		// commit backing buffer to the outgoing serial packets
		for (PBXPixel pix : pixels) { pix.commit(); }

		// send pixel data to expansion boards
		for (PBXBoard b : boards) { b.send(); }

		// send a DRAW_ALL command to each port to write the data
		// to the LEDs.
		for (PBXSerial p : ports) {
			p.drawAll();
		}
	}  	

	private void welcome() {
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}

	/**
	 * return the version of the Library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

}

