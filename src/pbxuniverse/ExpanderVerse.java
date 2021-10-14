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
  - Disable/enable channels??      
  - keep looking for ways to simplify and automate the display creation process
  - write more examples! Fonts, shapes, noise, shaders, color correction, text...
  - optimize getPixelCount() - it doesn't need to actually recheck unless config has changed  
  - add coordinate translate, scale, rotate
  - (MOSTLY DONE) how best to integrate the mapping functions?  When to autogen the normalized map
  - Power usage estimation and management
  - FINISH COLOR CORRECTION!!!!!!
  - default HDR color on for APAs, but allow user to set.
  - document default image index behavior
  - should we autonumber boards when adding them to a port?    
 */	        

/**
 * Control addressable LED displays from your
 * computer.  Requires a compatible USB to Serial adapter, and one
 * or more the Pixelblaze Output Expander boards.
<p>
 To build a display:
 <p>
 <li>create an ExpanderVerse object
 <li>open your serial ports with OpenPort <p>
 <li>use AddOutputExpander(PBXSerial) to add output expanders to the serial ports
 <li>use the appropriate AddChannel(PBXBoard) variant to add output channels.
 
<p>
 Pixel order is determined by the order in which you add Output Expander boards
 and channels. As you create channels, ExpanderVerse maintains a default 1D (x,0,0) mapping
 so for 1D strip setups, you can use the map for rendering without any further work.
 If you have a more complex layout, you can set a custom (x,y,z) mapping after all
 channels have been added. 
 <p>
 To draw to your LEDs:<p>
 <li>use the SetPixel(i,color) or pixels[i].setColor(color) functions to set your LEDs
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
	 * Given the name of a serial port ("COM5", "/dev/ttyUSBsomething"), returns
	 * the port object associated with that name if it exists. The port names should
	 * match exactly, except for case, which is ignored. Return null if the specified 
	 * port is not found.
	 * @param portName
	 * @return Serial port object
	 */
	public PBXSerial getPort(String portName) {
		PBXSerial prt = null;
		for (PBXSerial p : ports) {
			if (portName.equalsIgnoreCase(p.getPortName())) {
				prt = p;
				break;
			}
		}
		return prt;			
	}

	/**
	 * Gets the list of open serial port objects. The list
	 * may be empty.
	 * @return port list
	 */
	public LinkedList<PBXSerial> getPortList() {
		return ports;
	}

	/**
	 * Given the "index" of an open serial port returns the associated port object. 
	 * ExpanderVerse stores open ports in the order they are opened, so the first port
	 * opened will be at index 0, the second at index 1, and so on.  <p>
	 * Returns null if the specified port is not found.
	 * @param portName
	 * @return Serial port object
	 */
	public PBXSerial getPort(int n) {

		// make sure the index is in the range of valid
		// open ports
		if ((n < 0) || (ports.size() <= n)) return null;

		return ports.get(n);
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
	public PBXDataChannel addChannelAPAClock(PBXBoard board,int chNumber,int frequency) {     
		PBXDataChannel ch = board.addChannelAPAClock((byte) chNumber,frequency);
		return ch;
	}

	/**
	   Helper function for channel creation: Creates a new channel of specified type and adds it to a board 
	 * 
	 * @param board previously created board object
	 * @param type channel type/protocol - WS2812, APA102, or APACLOCK
	 * @param chNumber channel number (on board) to create
	 * @param pixelCount number of pixels attached to this channel
	 * @param frequency desired output clock frequency (in hz) for this channel, e.g 2000000, 800000
	 * @param colorString string describing color order, e.g "RGB", "GRBW"
	 * @return channel object
	 */	
	public PBXDataChannel addChannel(PBXBoard board,ChannelType type, int chNumber,int pixelCount, int frequency,String colorString) {
		PBXDataChannel ch = null;
		switch (type) {
		case WS2812:
			ch =  addChannelWS2812(board,chNumber,pixelCount,colorString); 
			break;
		case APA102:
			ch =  addChannelAPA102(board,chNumber,pixelCount,frequency,colorString);			
			break;
		case APACLOCK:
			ch = addChannelAPAClock(board,chNumber,frequency);
			break;
		default:
			throw new IllegalArgumentException("Invalid channel type specified.");
		}		

		return ch;
	}	

	/**
	   Helper function for channel creation. 
	   Creates a new channel of specified type and adds it to a board. (If an APA102
	   or APACLOCK channel is created using this method, it's clock frequency will 
	   default to 2000000bps. 
	 * 
	 * @param board previously created board object
	 * @param type channel type/protocol - WS2812, APA102, or APACLOCK
	 * @param chNumber channel number (on board) to create
	 * @param pixelCount number of pixels attached to this channel
	 * @param colorString string describing color order, e.g "RGB", "GRBW"
	 * @return channel object
	 */	
	public PBXDataChannel addChannel(PBXBoard board,ChannelType type, int chNumber,int pixelCount,String colorString) {
		return addChannel(board,type,chNumber,pixelCount,2000000,colorString);
	}	
	
	/**
	 * Helper function for channel creation: 
	   Creates a new channel of specified type and adds it to a board. This prototype, which
	   doesn't include pixelCount, is aimed at creating APA102 clock channels.
	 * 
	 * @param board previously created board object
	 * @param type channel type/protocol - WS2812, APA102, or APACLOCK
	 * @param chNumber channel number (on board) to create
	 * @param Frequency - clock frequency
	 * @return channel object
	 */	
	public PBXDataChannel addChannel(PBXBoard board,ChannelType type, int chNumber,int Frequency) {
		return addChannel(board,type,chNumber,1,Frequency,"RGB");
	}	
	
	/**
	   Helper function for channel creation. Creates a new channel of specified type
	   and adds it to a board. This 'helper' prototype is aimed at quick creation of
	   APA102 Clock channels.  Frequency will default to 2000000 bits/sec.
	 * 
	 * @param board previously created board object
	 * @param type channel type/protocol - WS2812, APA102, or APACLOCK
	 * @param chNumber channel number (on board) to create
	 * @return channel object
	 */	
	public PBXDataChannel addChannel(PBXBoard board,ChannelType type, int chNumber) {
		return addChannel(board,type,chNumber,1,2000000,"RGB");
	}		

	/**
	 * Returns the board object specified by id on the serial port
	 * port.
	 * @param port - serial port object
	 * @param id - board id number to retrieve
	 * @return PBXBoard object if found, null otherwise
	 */
	public PBXBoard getBoard(PBXSerial port,int id) {				
		return port.getBoard(id);
	}

	/**
	 * Gets the channel object specified by id on board brd
	 * @param brd - board to query for channel 
	 * @param id - number of channel to retrieve
	 * @return PBXDataChannel object if found, null otherwise
	 */	
	public PBXDataChannel getChannel(PBXBoard brd, int id) {
		return brd.getChannel(id);		
	}


	// create block of internal pixel objects for a newly addded channel. 
	// The mapping and image index values are set sequentially for new pixels
	// so that we can use setPixelsFromImage() and do map-related rendering
	// immediately. <p> 
	// Everything in ExpanderVerse is 3D.  If the user does not specify a mapping,
	// the LEDs will be assumed to be in a sequential strip, of dimension [n,1,1].
	//
	protected void createPixelBlock(PBXDataChannel ch, int pixelCount) {
		PVector m = new PVector();
		int imageIndex = getNextAvailableImageIndex();

		// add and map new pixels
		for (int i = 0; i < pixelCount; i++) {
			PBXPixel pix = new PBXPixel(pApp.color(0), ch, i);
			m.set((float) imageIndex,0,0);
			pix.setMapCoordinates(m);
			pix.setIndex(imageIndex++);
			pixels.add(pix);      
		}

		// after the new block is added, rebuild the normalized coordinate map
		buildNormalizedMap();
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
	 * Sets brightness for all ports, boards and channels.  Settings made at the global
	 * level will overwrite previous settings for individual ports boards and channels.
	 *   
	 * @param b global brightness level (0..1)
	 */
	public void setGlobalBrightness(float b) {
		globalBrightness = PApplet.constrain(b,(float) 0.0,(float) 1.0);
		for (PBXSerial p : ports) { p.setBrightness(b); }				
	}

	/**
	 * Get the current global brightness setting (0..1)
	 */	
	public float getGlobalBrightness() {
		return globalBrightness;
	}

	/**
	 * Sets gamma correction factor (power curve exponenent) for all ports, boards and channels.
	 * Settings made at the global level will overwrite previous settings for individual ports boards and channels.   
	 * @param b global gamma factor (0..1)
	 */
	public void setGammaCorrection(float g) {
		gammaCorrection = Math.max(0,g);
		for (PBXSerial p : ports) { p.setGammaCorrection(g); }			
	}

	/**
	 * Sets r,g and b color correction factors - values in the range 0..1 that will be multiplied
	 * with pixel colors to produce the particular 'white' you want to match. Settings made at the 
	 * global level will overwrite previous settings for individual ports boards and channels.    
	 */			
	public void setColorCorrection(float r,float g, float b) {
		for (PBXSerial p : ports) { p.setColorCorrection(r,g,b); }  
	}		

	/**
	 * Sets pixel drawing mode, either DrawMode.FAST (brightness & gamma adjustment only) or
	 * DrawMode.ENHANCED (brightness, gamma, color correction, color depth expansion if supported by LEDs).
	 * DrawMode can be changed at any time.
	 * Settings made at this level will override previous settings for all ports, boards and channels.  
	 */	
	public void setDrawMode(DrawMode dm) {
		for (PBXSerial p : ports) { p.setDrawMode(dm); }  		
	}		

	/**
	 * Sets the pixel at the specified index to a color.
	 * <p>
	 * Note: The color parameter 'c' is shown as an int here. It's actually a processing
	 * color() type, which is really just an int encoded by Processing's internal preprocessor.
	 * This convenience is not available to Java libraries.  From Processing though, you can call it
	 * like this:<p>
	 * setPixel(0,color(r,g,b))<p>
	 * or, depending on your colorMode()<p>
	 * setPixel(0,color(h,s,v))<p>
	 * and the right thing will happen.
	 * 
	 */
	public void setPixel(int index,int c) {
		pixels.get(index).setColor(c);
	}

	/**
	 * Sets all pixels associated with this ExpanderVerse object to
	 * a single color.
	 * @param color
	 */
	public void setAllPixels(int color) {
		for (PBXPixel pix : pixels) { pix.setColor(color); }
	}
	
	/**
	 * Sets all pixels on the specified channel to 
	 * a single color.
	 * @param color
	 */
	public void setAllPixels(PBXDataChannel ch, int color) {
		for (PBXPixel pix : pixels) {
			if (pix.channel == ch) pix.setColor(color);
		}
	}		

	/**
	 * Sets all pixels associated with this ExpanderVerse object to
	 * black (off)
	 */	
	public void clearPixels() {
		setAllPixels(0);
	}
	
	/**
	 * Sets all pixels on the specified channel to 
	 * black (off)
	 */		
	public void clearPixels(PBXDataChannel ch) {
		setAllPixels(ch,0);
	}	

	/**  
	 * Render all pixels to the LEDs on all ports, adapters and channels.
	 */
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

	// return the next available image index value in the pixel list. This will be
	// either 0 if the list is empty, or 1+the highest existing value. Called
	// when setting up new blocks of pixels.
	int getNextAvailableImageIndex() {
		int nMax = 0;
		// make a pass through the entire map to find min/max coords
		for (PBXPixel p : pixels) {
			if (p.index > nMax) nMax = p.index;
		}
		return (nMax == 0) ? 0 : nMax + 1;
	}		

	/** The image index tells which pixel in a PImage of 
	 * appropriate size corresponds to this LED
	 * 
	 * @param pix index of the LED in ExpanderVerse's pixel list.  
	 * @param imageIndex the index in a PImage's pixel array that this pixel should use
	 * when transferring
	 */
	public void setImageIndex(int pix,int imageIndex) {
		pixels.get(pix).setIndex(imageIndex);
	}

	/**
	 *  Sets 3D world coordinates for the specified pixel
	 * @param i index of LED 
	 * @param m 3D vector of world coordinates. If you're not using a 
	 * coordinate dimension, it should be set to 0.
	 */
	public void setMapCoordinates(int i,PVector m) {
		pixels.get(i).setMapCoordinates(m);
	}	

	// set normalized 3D world coordinates of pixel
	public void setNormalizedCoordinates(int i,PVector m) {
		pixels.get(i).setNormalizedCoordinates(m);
	}	

	/**
	 *  Build normalized coordinate map from the world coordinate map over the 
	 *  specific range of pixels.  Note that using this method, it is possible to
	 *  have several independent (0..1) normalized coordinated maps over the space
	 *  of a given display.  This makes it easier to divide the display into 
	 *  independent "segments" and render those segments each with its own normalized
	 *  coordinate space.<p>  
     *
     *  The world coordinate map must be set to valid values over the given range
     *  before calling this function or the resulting normalized map will be useless.
	 *  @param start index of starting pixel
	 *  @param count number of pixels in range
	 */
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
	 *  Build a normalized map from the world coordinate map.
	 *  <p>
	 *  World coordinate
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
	 * Read a Pixelblaze compatible 2d/3d JSON map into the current ExpanderVerse's map, starting
	 * at the specified index. Since all maps in ExpanderVerse are 3D, if you import a 2D map, the
	 * z coordinate will be automatically set to zero. <p>
	 * @param fileName Name of file to read
	 * @param index starting destination index for map 
	 * @param scale Coordinate multiplier for scaling output
	 */
	public void importPixelblazeMap(String fileName,int index, float scale) {
		PVector m = new PVector();
		JSONArray json = pApp.loadJSONArray(fileName);
		int n = json.size();

		for (int i = 0; i < n; i++) {
			JSONArray mapEntry = json.getJSONArray(i);

			float [] coords = mapEntry.getFloatArray();

			m.set(coords[0],coords[1],(coords.length == 3) ? coords[2] : 0);
			m.mult(scale);
			setMapCoordinates(index,m);
			index++;
		}
		buildNormalizedMap();
	}

	/**
	 * Converts the current coordinate map to a Pixelblaze compatible JSON pixel map and
	 * write it to the specified file.
	 * @param fileName Name of file to write  
	 * @return true if successful, false otherwise
	 */			
	public boolean exportPixelblazeMap(String fileName) {
		JSONArray json,mapEntry;
		PVector vec = new PVector();

		json = new JSONArray();

		for (PBXPixel p : pixels) {
			p.getMapCoordinates(vec);
			mapEntry = new JSONArray();
			mapEntry.append(vec.x);
			mapEntry.append(vec.y);
			mapEntry.append(vec.z);    

			json.append(mapEntry);
		}  
		return pApp.saveJSONArray(json,fileName);  
	}
}

