/*
 ExpanderVerse Library for Processing

 Drive addressable WS28xx and APA102 addressable LEDs with your computer, a USB->Serial adapter,
 and a Pixelblaze Output Expander ( https://www.bhencke.com/serial-led-driver ) board.
 Potentially *many* LEDs.   

 Copyright 2021 JEM (ZRanger1)

 Permission is hereby granted, free of charge, to any person obtaining a copy of this
 software and associated documentation files (the "Software"), to deal in the Software
 without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or
 substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 Version  Date         Author Comment
 v0.0.1   09/19/2021   JEM(ZRanger1)    Created
 */
package pbxuniverse;

import processing.core.*;
import processing.data.JSONArray;
import java.util.*;

/*
TODO - THE MASTER TODO LIST!
  - PImage/PGraphics transfer
  - Disable/enable channels??      
  - what do we need to do to enable multithread/multicore pixel rendering?   Anything?
  - keep looking for ways to simplify and automate the display creation process
  - write more damn examples! Fonts, shapes, noise, shaders...
  - optimize getPixelCount() - it doesn't need to actually recheck unless config has changed  
  - add coordinate translate, scale, rotate
  - how best to integrate the mapping functions?  When to autogen the normalized map
  - Power usage estimation and management
  - allow addChannel() to use board index as well as board object pointer
  - document new gamma behavior and change in all examples
  - document default image index behavior
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
	public final static String VERSION = "##library.prettyVersion##";

	PApplet pApp;
	ArrayList<PBXPixel> pixels;
	LinkedList<PBXSerial> ports;
	float globalBrightness = (float) 0.2;
	float gammaCorrection = 1;
	byte[] levelTable = new byte[256];	

	public ExpanderVerse(PApplet parent) {
		pApp = parent;
		pixels = new ArrayList<PBXPixel>();
		ports = new LinkedList<PBXSerial>();
		welcome();
	}

	////////////////////////////////////////////////////////////////////////////////
	// Library Utilities
	////////////////////////////////////////////////////////////////////////////////

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

	////////////////////////////////////////////////////////////////////////////////
	// Display Management - Serial Port, Board and Channel
	////////////////////////////////////////////////////////////////////////////////	

	/**
	 * @return list of names of serial ports attached
	 * to this computer. Name format will be OS specific.
	 */
	public String[] listSerialPorts() {
		return PBXSerial.list();
	}

	/** 
	 * Attempt to open specified serial port and return its object handle.
	 * @param portName - name of serial port
	 * @return port object if successful
	 */
	public PBXSerial openPort(String portName) {
		PBXSerial p = new PBXSerial(this, portName);
		ports.add(p);
		return p;
	}

	/**
	 * Create a new board, attach it to the specified port and return the object.  Board 
	 * number is the hardwired board id on the bus. Setting a board
	 * to an ID other than zero requires physically cutting a trace
	 * on the board.
	 * @param port - open port object
	 * @param boardNo - id of the board, 0-7
	 * @return new board object 
	 */
	public PBXBoard addOutputExpander(PBXSerial port, int boardNo) {
		PBXBoard b = new PBXBoard(port,boardNo);
		port.addBoard(b);
		return b;
	}

	/**
	 * Create a new board, attach it to the specified port as board 0 and return the object.  
	 * Setting a board to an ID other than zero requires physically cutting a trace
	 * on the board.  See Pixelblaze website (www.electromage.com) for more information
	 * @param port - open port object
	 * @return new board object 
	 */
	public PBXBoard addOutputExpander(PBXSerial port) {
		return addOutputExpander(port,0);
	}

	/**
	   Create channel for RGB or RGBW WS2812/Neopixel class LEDs
	 * 
	 * @param board previously created board object
	 * @param chNumber channel number (on board) to create
	 * @param pixelCount number of pixels attached to this channel
	 * @param colorString string describing color order, e.g "RGB", "GRBW"
	 * @return channel object
	 */
	public PBXDataChannel addChannelWS2812(PBXBoard board,int chNumber,int pixelCount,String colorString) { 
		PBXDataChannel ch = board.addChannelWS2812((byte) chNumber, pixelCount,colorString);
		createPixelBlock(ch,pixelCount); 		
		return ch;
	}

	/**
	   Create data channel for APA102/Dotstar LEDs
	 * 
	 * @param board previously created board object
	 * @param chNumber channel number (on board) to create
	 * @param pixelCount number of pixels attached to this channel
	 * @param frequency desired output clock frequency (in hz) for this channel, e.g 2000000, 800000
	 * @param colorString string describing color order, e.g "RGB", "GRBW"
	 * @return channel object
	 */	
	public PBXDataChannel addChannelAPA102(PBXBoard board,int chNumber,int pixelCount,int frequency,String colorString) {   
		PBXDataChannel ch = board.addChannelAPA102((byte) chNumber,pixelCount,frequency,colorString);
		createPixelBlock(ch,pixelCount);		
		return ch;
	}

	/**
	 * Create clock channel for APA102/Dotstar LEDs <p>
	 * If you use APA102s, you will need to dedicate one channel on the output expander to
	 * the APA clock signal. 
	 * 
	 * @param board previously created board object
	 * @param chNumber channel number (on board) to create
	 * @param frequency desired output clock frequency (in hz) for this channel, e.g 2000000, 800000
	 * @return channel object
	 */
	// TODO - should I just set this up w/the first apa data channel so the user doesn't have
	// to worry about it?  Probably.
	
	public PBXDataChannel addChannelAPAClock(PBXBoard board,int chNumber,int frequency) {     
		PBXDataChannel ch = board.addChannelAPAClock((byte) chNumber,frequency);
		return ch;
	}

	// create block of internal pixel objects for a newly addded channel. 
	// The image index value is set sequentially for new pixels so that we
	// can use setPixelsFromImage() immediately on 1D strips even if the user
	// has not yet created a coordinate map.
	protected void createPixelBlock(PBXDataChannel ch, int pixelCount) {
		int imageIndex = getImageIndexMax();
		
		for (int i = 0; i < pixelCount; i++) {
			PBXPixel pix = new PBXPixel(pApp.color(0), ch, i);
			pix.setIndex(imageIndex++);
			pixels.add(pix);      
		}
	}

	/**
	 * Gets the total number of pixels associated with this ExpanderVerse object
	 * (all attached ports, boards and channels) 
	 * @return total number of pixels
	 */
	public int getPixelCount() {
		int n = 0;
		for (PBXSerial p : ports) { n += p.getPixelCount(); }

		return n;
	}

	/**
	 * @return ArrayList containing all pixels associated with this ExpanderVerse object
	 */
	public ArrayList<PBXPixel> getPixelList() {
		return pixels;		
	}

	/**
	 * Sets brightness for all ports, boards and channels.
	 *   
	 * @param b global brightness level (0..1)
	 */
	public void setGlobalBrightness(float b) {
		globalBrightness = PApplet.constrain(b,(float) 0.0,(float) 1.0);
		for (PBXSerial p : ports) { p.setBrightness(b); }				
	}

	public float getGlobalBrightness() {
		return globalBrightness;
	}

	/**
	 * Sets gamma correction factor (power curve exponenent) for all ports, boards and channels.
	 *   
	 * @param b global gamma factor (0..1)
	 */
	public void setGammaCorrection(float g) {
		gammaCorrection = Math.max(0,g);
		for (PBXSerial p : ports) { p.setGammaCorrection(g); }			
	}

	public void setPixel(int index,int c) {
		pixels.get(index).setColor(c);
	}

	// Finally!  Actually send something to the LEDs.
	public void draw() {

		// commit backing buffer to the outgoing serial packets
		for (PBXPixel pix : pixels) { pix.commit(); }

		// send pixel data to expansion boards.  Will block this (the main) thread if
		// reentered, which allows us to finish all writes to all boards before
		// sending the DRAW ALL command to everybody. This will keep the
		// LEDs in the tightest possible sync.
		//
		// It does limit total throughput by the time it takes to write data to the 
		// slowest board (usually the one with the most LEDs), so if you think speed
		// will be a problem in your setup, balance the LEDs on your expander boards
		// accordingly.
		for (PBXSerial p : ports) { p.sendPixelData(); }

		// send a DRAW_ALL command to each port to write the data
		// to the LEDs.  Again, will block if reentered, but these are tiny packets
		// so send time is negligible.
		for (PBXSerial p : ports) { p.sendDrawAll(); }
	}  	

	//////////////////////////////////////////////////////////////////////////////// 
	// Coordinate mapping and related functions
	////////////////////////////////////////////////////////////////////////////////
	
	// return the highest image index value in the pixel list.  Called
	// when setting up new blocks of pixels.
	int getImageIndexMax() {
	  int nMax = 0;
	// make a pass through the entire map to find min/max coords
	  for (PBXPixel p : pixels) {
	  	if (p.index > nMax) nMax = p.index;
	  }
	  return nMax;
	}		

	// The image index tells which pixel in a PImage of 
	// appropriate size corresponds to this LED
	public void setImageIndex(int pix,int imageIndex) {
		pixels.get(pix).setIndex(imageIndex);
	}

	// set 3D world coordinates of pixel
	public void setMapCoordinates(int i,PVector m) {
		pixels.get(i).setMapCoordinates(m);
	}	

	// set normalized 3D world coordinates of pixel
	public void setNormalizedCoordinates(int i,PVector m) {
		pixels.get(i).setNormalizedCoordinates(m);
	}	

	// normalize <count> map entries, starting at index <start>
	public void normalizeMapSegment(int start,int count) {
		// init max and min values.  If anybody's using world coordinates outside
		// the two hundred million range, they're welcome to the bug they'll find.
		PVector vMax = new PVector(-200000000,-200000000,-200000000);   
		PVector vMin = new PVector(200000000,200000000,200000000);

		// make a pass through the entire map to find min/max coords
		for (PBXPixel p : pixels) {
			if (p.map.x > vMax.x) vMax.x = p.map.x;
			if (p.map.y > vMax.y) vMax.y = p.map.y;
			if (p.map.z > vMax.z) vMax.z = p.map.z;

			if (p.map.x < vMin.x) vMin.x = p.map.x;
			if (p.map.y < vMin.y) vMin.y = p.map.y;
			if (p.map.z < vMin.z) vMin.z = p.map.z;						
		}

		// put range in vMax. (This whole thing will throw a divide-by-zero exception eventually if
		// the range is zero.)
		vMax.sub(vMin); 

		// now make a pass through the specified region and build the normalized map
		for (int i = 0; i < count; i++) {
			PBXPixel p = pixels.get(i+start);
			
			p.nMap.set(p.map);   
			p.nMap.sub(vMin);
			// weird.. there are per-element add/subtract PVector menthods in Java, but no
			// multiply or divide
			p.nMap.x /= vMax.x;
			p.nMap.y /= vMax.y;
			p.nMap.z /= vMax.z;
		}		
	}
	
	/**
	 *  Build a normalized map from the world coordinate map. World coordinate
	 *  map must be set before calling this method or mayhem will ensue.
	 */
	public void buildNormalizedMap() {
		normalizeMapSegment(0,pixels.size());
	}


	/**
	 * Index map must be set before calling this (as by setMatrixMap), or 
	 * the displayed result will not be predictable.
	 * <p>
	 * Will throw an exception if the specified image does not contain enough
	 * pixels to fill the request, or if one of the index map coordinates is
	 * out of bounds. 
	 * 
	 * @param index of starting pixel in display
	 * @param count number of pixels to set
	 * @param image array of image pixels
	 */
	public void setPixelsFromImage(int index,int count,int[] image) {
		count = index + count;

		for (int i = index; i < count; i++) {
			PBXPixel p = pixels.get(i);
			p.setColor(image[p.getIndex()]);   				
		}
	}		

	/**
	 * Builds an index map for a section of the display beginning with
	 * pixel 'pix', that orders the pixels as though they were a wired
	 * 2D matrix, with either zigzag or linear wiring.
	 * a section of the overall display into zigzag-wired 2D matrix.
	 * This map allows for simple, rapid copying of pixels from a rectangular portion
	 * of any Processing PImage into the LED buffer without having to worry about coordinate
	 * translation. 
	 * TODO - no parameter validation. Will throw an exception if there aren't enough pixels
	 * TODO - allow setting Z coordinate to a single plane (other than 0)
	 * 
	 * @param index - index of starting pixel
	 * @param xSize - x dimension of matrix
	 * @param ySize - y dimension of matrix
	 * @param zigzag - true for zigzag wiring
	 */
	public void setMatrixMap(int index, int xSize, int ySize, boolean zigzag) {
		processing.core.PVector m = new PVector();
		int x,y,n;
		float xLast,yLast,nx;

		// last row/col index, which we'll use to calculate normalized
		// coordinates.
		xLast = xSize - 1; yLast = ySize - 1;

		// loop through matrix coordinates, storing pixel index, x,y coords,
		// and normalized coords for each.
		for (y = 0; y < ySize; y++) {
			for (x = 0; x < xSize; x++) {
				n = (y * xSize);

				// handle the zigzag wiring thing...
				if (zigzag == true && (y % 2) == 1) {
					nx = x;
				} 
				else {
					nx = (ySize - 1 - x);
				}
				n += nx;

				// world coordinates
				m.set(nx,y,0);
				setMapCoordinates(index,m);

				// normalized coordinates
				m.set(nx/xLast,((float) y)/yLast,0);
				setNormalizedCoordinates(index,m);

				// index of display pixel in processing PImage pixels,
				// accounting for zigzag wiring.
				setImageIndex(index,n);
				index++;
			}			
		}
	}



	/**
	 * Read a Pixelblaze compatible 2d/3d JSON map into the current ExpanderVerse's map
	 * @param fileName Name of file to read 
	 * @param scale Coordinate multiplier for scaling output
	 * @return true if successful, false if unable to load the specified file 
	 */
	public boolean importPixelblazeMap(String fileName,float scale) {

		JSONArray json = pApp.loadJSONArray(fileName);

		for (int i = 0; i < json.size(); i++) {
			float x,y,z;

			JSONArray mapEntry = json.getJSONArray(i);
			float [] coords = mapEntry.getFloatArray();  

			x = scale * coords[0];
			y = scale * coords[1];
			z = scale * ((coords.length == 3) ? z = coords[2] : 0);

			//			ScreenLED led = new ScreenLED(this,scale * x,scale * y,scale * z);
			//			led.setIndex(i);
			//			object.add(led);
		}

		return true;
	}

	/* TODO - decide if we need export and how to handle it	

 // comparator for sorting.  Used by exportPixeblazeMap()
	class compareLEDIndex implements Comparator<ScreenLED> {

		@Override
		public int compare(ScreenLED p1, ScreenLED p2) {
			return (p1.index < p2.index) ? -1 : 1;
		}
	}


	 * Convert a list of ScreenLEDs to a Pixelblaze compatible JSON pixel map and
	 * write it to the specified file.
	 * @param obj Linked list of ScreenLEDs representing a displayable object
	 * @param fileName Name of file to write 
	 * @param scale Coordinate multiplier for scaling final output 
	 * @param is3D true for 3D (xyz), false for 2D (xy) 
	 * @return true if successful, false otherwise
	public boolean exportPixelblazeMap(LinkedList<ScreenLED> obj,String fileName,float scale, boolean is3D) {
		JSONArray json,mapEntry;

		//sort object by pixel index for export. 
		@SuppressWarnings("unchecked")
		LinkedList<ScreenLED> sortedCopy = (LinkedList<ScreenLED>) obj.clone();
		Collections.sort(sortedCopy,new compareLEDIndex());

		json = new JSONArray();
		for (ScreenLED led : sortedCopy) {
			mapEntry = new JSONArray();
			mapEntry.append(scale * led.x);
			mapEntry.append(scale * led.y);
			if (is3D) mapEntry.append(scale * led.z);    

			json.append(mapEntry);
		}  
		return app.saveJSONArray(json,fileName);  
	}
END - ExportPixelblazeMap */

}

