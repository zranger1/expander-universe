package pbxuniverse;

import processing.core.*;

//optimized for fast traversal, definitely not for memory size
public class PBXPixel {
	protected int pixel;      // 4 byte RGBA color, ('A' byte not used at the moment)
	protected int index;      // which pixel in a PImage of appropriate size corresponds to this LED.
	protected PVector map;    // real world 2d/3d coords of LED (normalized?)
	protected PVector nMap;   // normalized map
	protected PBXDataChannel channel;  //  pointer to channel object
	protected int dataOffset;   // offset of pixel in channel's packet buffer

	public PBXPixel(int c,PBXDataChannel chan,int offset) {
		this.pixel = c;
		this.index = 0;
		this.channel = chan;
		this.dataOffset = offset;
		this.map = new PVector();
		this.nMap = new PVector();
	}
	
	// All ExpanderVerse maps are effectively 3D
	public void setMapCoordinates(PVector v) {
		map.set(v);
	}
	
	public void getMapCoordinates(PVector v) {
	   v.set(map);
	}
	
	public void setNormalizedCoordinates(PVector v) {
		nMap.set(v);
	}	
	
	public void getNormalizedCoordinates(PVector v) {
		v.set(nMap);
	}
	
	// index tells which pixel in a PImage of appropriate size corresponds to this LED
	public void setIndex(int n) {
		index = n;
	}
	
	public int getIndex() {
		return index;
	}
	
	// set rgb color of a pixel in the backing buffer. (Doesn't
	// set the actual outgoing packet data 'till draw time.)
	public void setColor(int c) {
		this.pixel = c;
	}
	
	// Extended/Experimental pixel color refinement.  User specified color  
	// will be white balanced and possibly extended to HDR if the 
	// LEDs on the channel support it.
	public int correctPixelColor(int c) {
		int r,g,b;
		
		// get white balanced values for each color component 
		b = channel.bCorrect[c & 0xFF];        // blue
		g = channel.gCorrect[(c >> 8) & 0xFF]; // green
		r = channel.rCorrect[(c >> 16) & 0xFF];// red
		
		// TODO - expand to HDR color if supported...

		// build output color value
		return b & (g << 8) * (r << 16);		
	}

	// get pixel color from the backing buffer
	public int getColor() {
		return this.pixel;
	}

	public void commit() {
		// eventually, this should be
		//channel.setPixel(dataOffset,correctPixelColor(pixel));		
		
		channel.setPixel(dataOffset,pixel);
	}
	
	// write pixels with white balanced, possibly HDR enhanced color
	// called by ExpanderVerse.drawEx();
	public void commitEx() {
		channel.setPixel(dataOffset,correctPixelColor(pixel));			
	}

}