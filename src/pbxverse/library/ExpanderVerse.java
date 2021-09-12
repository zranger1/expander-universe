package pbxverse.library;

import processing.core.*;
import java.util.*;

// TODO -- rename, refactor
// TODO -- channel brightness, global brightness and how they interact
// TODO -- same with gamma.
// WARNING -- channel brightness is 1 -- MUST fix global brightness.

/**
 BASIC OPERATION - to build a display:
 create ExpanderVerse object
 open your serial ports with OpenPort
 use AddOutputExpander(PBXSerial) to add output expanders to the serial ports
 use AddChannel(PBXBoard);
 by default pixel number, order and offsets will be determined by the order in which you 
 add the boards.  You can add x,y,z mapping after all the channels are created.
 use the SetPixel-ish functions to set your LEDs
 call Constellation.draw() to render all the pixels when set.

 TODO - easy matrix mode?  
      - PImage/PGraphics transfer
      - Disable/enable channels??
      
	  - put serial transmisison in its own thread so we can get right back to rendering
      - what do we need to do to enable multithread pixel rendering?
      - enable GPU rendering w/matrix/PImage thing        
      
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
	 
	 
	ExpanderVerse(PApplet parent) {
		pApp = parent;

		//TODO - setGlobalBrightness(0.3);
		pixels = new ArrayList<PBXPixel>();
		boards = new LinkedList<PBXBoard>();  
		ports = new LinkedList<PBXSerial>();
	}

	// Helper function - return a list of all the names of serial
	// ports attached to this computer
	public String[] ListSerialPorts() {
		return PBXSerial.list();
	}

	// attempt to open specified serial port and return
	// object handle.  This handle can be passed to expansion board
	// managment functions.
	public PBXSerial OpenPort(String portName) {
		PBXSerial p = new PBXSerial(pApp, portName);
		ports.add(p);
		return p;
	}

	// create boards first, get all hw config stuff set there,
	// then add to display.  This fn can generate board GUID and 
	// figure out pixel offsets by taking each board and channel in 
	// order.  (offset can bet set to OFFSET_AUTO);
	public PBXBoard AddOutputExpander(PBXSerial port, int boardNo) {
		PBXBoard b = new PBXBoard(this,port,boardNo);
		boards.add(b);
		return b;
	}

	public PBXBoard AddOutputExpander(PBXSerial port) {
		return AddOutputExpander(port,0);
	}

	// Create channel for RGB or RGBW WS2812/Neopixel class LEDs
	public PBXDataChannel AddChannelWS2812(PBXBoard board,int chNumber,int pixelCount,String colorString) { 
		byte channelId = (byte) (board.GetBoardId() | chNumber);
		PBXDataChannel ch = new PBXChannelWS2812(board.GetSerialPort(),channelId, pixelCount,colorString);
		board.AddChannel(ch);
		createPixelBlock(ch,pixelCount);    
		return ch;
	}

	// Create data channel for APA102/Dotstar LEDs
	public PBXDataChannel AddChannelAPA102(PBXBoard board,int chNumber,int pixelCount,int frequency,String colorString) {
		byte channelId = (byte) (board.GetBoardId() | chNumber);       
		PBXDataChannel ch = new PBXChannelAPA102(board.GetSerialPort(),channelId,pixelCount,frequency,colorString);
		board.AddChannel(ch);
		createPixelBlock(ch,pixelCount);
		return ch;
	}

	// Create clock channel for APA102/Dotstar LEDs
	// if you use APA102s, you'll need to dedicate one clock channel.
	// TODO - should I just set this up w/the first apa data channel so the user doesn't have
	// to worry about it?  Probably.
	public PBXDataChannel AddChannelAPAClock(PBXBoard board,int chNumber,int frequency) {   
		byte channelId = (byte) (board.GetBoardId() | chNumber);       
		PBXDataChannel ch = new PBXChannelAPAClock(board.GetSerialPort(),channelId,frequency);
		board.AddChannel(ch);
		return ch;
	}

	public void createPixelBlock(PBXDataChannel ch, int pixelCount) {
		for (int i = 0; i < pixelCount; i++) {
			PBXPixel pix = new PBXPixel(pApp.color(0), ch, i);
			pixels.add(pix);      
		}
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
		for (PBXPixel pix : pixels) { pix.Commit(); }

		// send pixel data to expansion boards
		for (PBXBoard b : boards) { b.Send(); }

		// send a DRAW_ALL command to each port to write the data
		// to the LEDs.
		for (PBXSerial p : ports) {
			p.DrawAll();
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

