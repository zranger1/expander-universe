package pbxverse.library;

import processing.core.*;

//optimized for fast traversal, definitely not for memory size
class PBXPixel {
	int pixel;      // 4 byte RGBA color, A not used
	PVector map;      // real world 2d/3d coords of LED (normalized?)
	PVector nMap;     // normalized map
	PBXDataChannel channel;  //  pointer to channel object
	int dataOffset;   // offset of pixel in channel's packet buffer

	PBXPixel(int c,PBXDataChannel chan,int offset) {
		this.pixel = c;
		this.channel = chan;
		this.dataOffset = offset;   
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

	public void Commit() {
		channel.setPixel(dataOffset,pixel);
	}

}