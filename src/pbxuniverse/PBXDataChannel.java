package pbxuniverse;

import processing.core.PApplet;

public class PBXDataChannel extends PBXChannel {
	// offsets of data elements in the output packet
	// how we build a packed structure in a language that doesn't
	// support packed structures...
	int header_size = 10; 
	int offs_bpp;
	int offs_color_order;
	int offs_pixel_count;
	int offs_frequency;

	byte bytes_per_pixel;
	byte color_order;
	int pixel_count; 
	int frequency;
	
	DrawMode drawMode;
	_pixelSetter ps;

	PBXBoard board;	  

	// support for per-channel brightness & gamma correction
	// per channel defaults to 100%, modified by global brightness which we'll
	// start a *lot* lower to avoid the possibility of damaging equipment.
	float   brightness = 1;   

	// gamma defaults to linear -- whatever the LEDs are gonna do
	float gammaCorrection = 1;
	
	// per-channel color correction factors and tables to balance colors between strips of different
	// correction values are floating point, in the range (0-1)
	float cfR = 1; 
	float cfG = 1;
	float cfB = 1;
	byte[] rCorrect = new byte[256];
	byte[] gCorrect = new byte[256];
	byte[] bCorrect = new byte[256];	
	
	byte[] levelTable = new byte[256];		  
	
	// generic pixel setting interface. LED configuration to be
	// set at channel creation time
	protected interface _pixelSetter {
		      void setPixel(int index, int c);
	}	

	PBXDataChannel(PBXBoard brd, byte ch_number,byte ch_type) {
		super(brd.getSerialPort(),ch_number,ch_type);

		// construct composite board/channel id so the output expander knows where to send
		// the data.
		this.board = brd;	      
		channel_number = (byte) (0xFF & ((board.getBoardId() << 3) | (int)channel_number));

		bytes_per_pixel = 3;
		color_order = 0;
		pixel_count = 0;
		frequency = 0;
		
		setDrawMode(DrawMode.FAST);

		// Default channel brightness will be (globalBrightness * 1); 
		setBrightness(1);  // default channel output will be globalBrightness * 1;
	}

	// per pixel data offset (0-3) of each color in pixel data
	void setColorOrder(int r, int g, int b, int w) {
		color_order = (byte) (r | (g << 2) | (b << 4) | (w << 6));  
		outgoing[offs_color_order] = color_order;
	}

	void setColorOrder(int r,int g,int b) {
		setColorOrder(r,g,b,0);
	}   

	/**
	 *  Returns total number of pixels attached to this channel
	 * 
	 * @return
	 */
	public int getPixelCount() {
		return pixel_count;	  
	}

	void setPixelCount(int n) {
		pixel_count = n;
		packShort(offs_pixel_count,n);
	}     

	// Internal - Returns the offset of the 0th pixel in the channel's
	// packet buffer. This gives us a quick path for setting a whole
	// channel's pixels in one shot with as few function calls and as
	// little memory copying as we can manage.
	int getPixelBufferOffset() {
		return header_size;
	} 

    // Stub to allow iteration over all channel types. 
	// Should be overridden with a real implementation by child classes that actually have
	// pixels to set.
	void setPixel(int index,int c) {
       ;
	}

    // Stub to allow iteration over all channel types. 
	// Should be overridden with a real implementation by child classes that care about 
	// the various drawing modes (clock channels, for example don't).
	/**
	 * Sets pixel drawing mode, either DrawMode.FAST (brightness & gamma adjustment only) or
	 * DrawMode.ENHANCED (brightness, gamma, color correction, color depth expansion if supported by LEDs).
	 * DrawMode can be changed at any time. 
	 */	
	public void setDrawMode(DrawMode dm) {
		drawMode = dm;
	}
   	
	// build brightness/gamma table and color correction tables 
	// Variable gamma correction is done by power curve, which gives the user control
	// over the shape of the curve and can actually be fairly accurate, depending on the
	// value you choose and your LEDs.		
	void buildColorTables()  {
		// everything (except white balance) scales with global brightness
		// (and white balance probably should eventually as well...)
		float bri = brightness * board.getGlobalBrightness(); 		
		
		for (int i = 0; i < levelTable.length;i++) {
			float fi = (float) i; // just to make sure the right thing happens;
			
            // precalculate white corrected values for R,G and B, so we can just look them
			// up at pixel setting time.  This correction is first in the chain
			// of color processing at commit() time, so it is done at the original
			// pixel's brightness, before global brightness and gamma correction
			// are applied.
			rCorrect[i] = (byte) Math.floor(fi * cfR);
			gCorrect[i] = (byte) Math.floor(fi * cfG);
			bCorrect[i] = (byte) Math.floor(fi * cfB);
						
			// linear brightness value...
			float val = fi/levelTable.length;
					
			// calculate gamma corrected brightness
			val = bri * (float) Math.pow(val,gammaCorrection);
			levelTable[i] = (byte) Math.floor(val*255);		
		}    	
	}

    /**	
	  Sets brightness for this channel.
	 * 
	 * @param b (0..1) brightness
	 */
	public void setBrightness(float b) {
		brightness = PApplet.constrain(b,0,1); // store this channel's brightness
        buildColorTables();
	}

    /**	
	  Gets brightness value (range 0..1) for this channel.
	 * 
	 */
	public float getBrightness() {
		return brightness;
	}	  

	/**
	 * Sets gamma correction factor (power curve exponenent) for this channel.
	 *   
	 * @param g global gamma factor (0..1)
	 */		
	public void setGammaCorrection(float g) {
		gammaCorrection = Math.max(0, g);
        buildColorTables();
	}
	
	// Extended/Experimental pixel color refinement.  User specified color  
	// will be white balanced and possibly extended to HDR if the 
	// LEDs on the channel support it.  Correction tables must be
	// built (with a call to buildColorTables()) before calling this function
	// or nothing will be displayed.  But buildColorTables() should be called
	// by all data channel constructors automatically.
	//
	// Takes an RGB color, returns the 'corrected' version of that color. 
    int correctColor(int c) {
		int r,g,b;

		// get white balanced values for each color component 
		b = Byte.toUnsignedInt(bCorrect[c & 0xFF]);        // blue
		g = Byte.toUnsignedInt(gCorrect[(c >> 8) & 0xFF]); // green
		r = Byte.toUnsignedInt(rCorrect[(c >> 16) & 0xFF]);// red
		
		// build output color value
		return b | (g << 8) | (r << 16);		
	}
	
	/**
	 * Sets r,g and b color correction factors -- per color component "multipliers" in the 
	 * range (0..1) that will be multplied with pixel colors to produce, at full brightness,
	 * the particular 'white' you want to match.  
	 */			
	public void setColorCorrection(float r,float g, float b) {
		// validate and store correction factors, then rebuild tables
		// TODO - should we normalize the correction factors to keep brightness
		// as high as possible?  Or will that make it a great PITA to adjust?
		
		cfR = Math.min(1,Math.max(0,r));
		cfG = Math.min(1,Math.max(0,g));
		cfB = Math.min(1,Math.max(0,b));
		buildColorTables();
	}	

	// Internal: Ask the next guy above us for the global brightness
	// value.  Eventually, somebody up there will know...
	float getGlobalBrightness() {
		return board.getGlobalBrightness();
	}		
}  