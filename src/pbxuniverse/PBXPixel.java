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

	// get pixel color from the backing buffer
	public int getColor() {
		return this.pixel;
	}

	public void commit() {
		channel.setPixel(dataOffset,pixel);
	}

}