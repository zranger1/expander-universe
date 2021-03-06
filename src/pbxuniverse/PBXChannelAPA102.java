package pbxuniverse;
/**
 * Holds settings and data for APA102-type channels
 * 
   Dotstar/SK9822/APA102 LEDs require that one output channel on the expander board be set up as
   a dedicated clock channel. If you have multiple APA102 strips attached to a board,
   you can connect them all to the same clock channel.  You can specify a desired clock frequency
   when creating an APA102 channel. Valid clock speeds range from 250khz to 20Mhz.  The default
   frequency if none is specified, is 2000000 (2Mhz).  
 */	
public class PBXChannelAPA102 extends PBXDataChannel {
	int offs_filler;
	
	// APA protocol is always 4 bytes w/RGB in some order.  The [0]th byte contains 3 bits of flag,
	// then 5 bits of global brightness.
	// Note that this structure is not actually packed. There's a filler byte after the color order byte to maintain
	// alignement.  The Pixelblaze always sends '0xFF' in this position.
	PBXChannelAPA102(PBXBoard brd,byte ch_number,int pixelCount,int freq, String colorString) {
		super(brd, ch_number,CH_APA102_DATA);

		// set data offsets in packet for this type
		header_size = 14; 
		offs_frequency = 6;
		offs_color_order = 10;
		offs_filler = 11;
		offs_pixel_count = 12;        

		int r,g,b;

		r = parseColorString("R",colorString);
		g = parseColorString("G",colorString);
		b = parseColorString("B",colorString);    

		initOutputBuffer(header_size+(pixelCount * 4));

		setColorOrder(r,g,b);
		outgoing[offs_filler] = (byte) 0xff;  // replicate Pixelblaze's behavior
		setFrequency(freq);       
		setPixelCount(pixelCount); 

		setDrawMode(DrawMode.ENHANCED); 
	}

	// RGB color order.  
	PBXChannelAPA102(PBXBoard brd, byte ch_number, int pixelCount) {
		this(brd, ch_number,2000000,pixelCount,"RGB");
	}

	// Frequency - APA channels only
	void setFrequency(int f) {
		frequency = f;
		packInt(offs_frequency,frequency);
	}  
	
	/**
	 * Returns the ChannelType enum for this channel. 
	 */
	public ChannelType getChannelType() {
	   return ChannelType.APA102;
	}		

	// default (fast) RGB pixel setter for APA 102 - sets extended brightness
	// to maximum. (essentially ignoring it);
	protected class _defaultSetter implements _pixelSetter {

		public void setPixel(int index,int c) {
			index = header_size + (index * 4);	
			outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
			outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
			outgoing[index++] = levelTable[c & 0xff];          // B
			outgoing[index] = (byte) 0xff;	    
		} 
	}

	// HD RGB setPixel for APA 102 
	// Supports color correction and expands dynamic range of incoming RGB data to
	// use extended brightness bits.  This version stretches Processing's 8-bpp color
	// over the APA's wider range.
	// TODO - works on my APA 102 clones.  Does it work for everybody?
	//
	// Based on detailed APA protocol/behavior info at:
	// https://cpldcpu.wordpress.com/2014/08/27/apa102/	  
	protected class _hdSetter implements _pixelSetter {
		public void setPixel(int index,int c) {
			index = header_size + (index * 4);
			c = correctColor(c);
			c = RGB2APAColor(c);
			outgoing[index++] = (byte) ((c >> 16) & 0xFF); // R
			outgoing[index++] = (byte) ((c >> 8) & 0xFF);  // G
			outgoing[index++] = (byte) (c & 0xFF);         // B
			outgoing[index] = (byte) ((c >> 24) & 0xFF); // Flags & Brightness			
		}
	}
	
	/**
	 * Sets pixel drawing mode for this channel, either DrawMode.FAST (brightness & gamma adjustment only) or
	 * DrawMode.ENHANCED (brightness, gamma, color correction, color depth expansion if supported by LEDs).<p>
	 * DrawMode can be changed at any time. 
	 */	
	public void setDrawMode(DrawMode dm) {
		super.setDrawMode(dm);
		switch (dm) {
		case FAST:
			ps = new _defaultSetter(); 
			break;
		case ENHANCED:
			ps = new _hdSetter(); 
			break;
		}
	}

	// set pixel according to channel LED format
	void setPixel(int index,int c) {
		ps.setPixel(index, c);
	}

	// "stretches" a 24-bit RGB color to an appropriate extended range
	// apa102 color.  Returns the new 4 byte pixel, with the APA flags and
	// brightness in the most significant byte.
	//
	// TODO - Uses the scaling algorithm from FastLED - given that we have more
	// computing power available, look into doing something that drops even
	// more resolution down into the lower brightness levels.
	int RGB2APAColor(int rgb) {
		int r,g,b;
		int apaBri;

		// extract components and perform gamma and color correction
		r = Byte.toUnsignedInt(levelTable[(rgb >> 16) & 0xFF]);
		g = Byte.toUnsignedInt(levelTable[(rgb >> 8) & 0xFF]);
		b = Byte.toUnsignedInt(levelTable[rgb & 0xFF]);

		// calculate best APA pixel brightness level (1..31)
		apaBri = (((Math.max(Math.max(r,g),b) + 1) * 0x1F - 1) / 256) + 1;

		// adjust rgb values
		r = (0x1F * r + (apaBri >> 1)) / apaBri;
		g = (0x1F * g + (apaBri >> 1)) / apaBri;
		b = (0x1F * b + (apaBri >> 1)) / apaBri;

		// build final 4-byte color
		rgb = b | (g << 8) | (r << 16) | ((0xE0 | apaBri) << 24);

		return rgb;	  
	}		
}



