package pbxuniverse;

import processing.core.*;

/**
 * ExpanderVerse pixel data type. Holds color, coordinate mapping
 * and hardware interface information for each pixel.
 */
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
	
	/**
	 * Sets a pixel's world map coordinates.  In ExpanderVerse,
	 * all maps are 3D.  If you're not using one or more coordinates,
	 * just set them to zero.
	 * @param v - world coordinate vector
	 */
	public void setMapCoordinates(PVector v) {
		map.set(v);
	}
	
	/**
	 * Gets a 3-element vector containing the pixel's current world map coordinates.
	 * @param v - existing vector object into which to place results
	 */
	public void getMapCoordinates(PVector v) {
	   v.set(map);
	}
	
	/**
	 * Sets a pixel's normalized coordinates. Unless you're creating a
	 * specialized normalization, it is not normally necessary to call this
	 * method. The normalized coordinate map should be more-or-less automatically
	 * maintained, or you can renormalize the entire display from the
	 * ExpanderVerse object if you need to. <p>
	 * In ExpanderVerse, all maps are 3D.  If you're not using one or more coordinates,
	 * just set them to zero.
	 * @param v - normalized coordinate vector
	 */	
	public void setNormalizedCoordinates(PVector v) {
		nMap.set(v);
	}	
	
	/**
	 * Gets a 3-element vector containing the pixel's current normalized map coordinates.
	 * @param v - existing vector object into which to place results
	 */	
	public void getNormalizedCoordinates(PVector v) {
		v.set(nMap);
	}
	
	/**
	   The map index tells which pixel in a PImage of appropriate size corresponds to this LED
	 * 
	 * @param n
	 */
	public void setIndex(int n) {
		index = n;
	}

	/**
	   The map index tells which pixel in a PImage of appropriate size corresponds to this LED
	 * 
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	   Sets color of a pixel in the backing buffer. 
	 * 
	 * @param c - a Processing color() object
	 */
	public void setColor(int c) {
		this.pixel = c;
	}
	
	/**
	 * Gets the color of a pixel, in Processing color() object format, from the backing buffer
	 * @return = color() object containing the pixel's color.
	 */
	public int getColor() {
		return this.pixel;
	}

	// commit function - transfers all pixel colors to the channel
	// output buffers so they can be transmitted to the LEDs
	// TODO -- how can we make this go faster?
	void commit() {
		channel.setPixel(dataOffset,pixel);
	}

}