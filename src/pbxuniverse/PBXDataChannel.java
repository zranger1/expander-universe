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

	PBXBoard board;	  

	// support for per-channel brightness & gamma correction
	// per channel defaults to 100%, modified by global brightness which we'll
	// start a *lot* lower to avoid the possibility of damaging equipment.
	float   brightness = 1;   

	// gamma defaults to linear -- whatever the LEDs are gonna do
	float gammaCorrection = 1;
	byte[] levelTable = new byte[256];		  

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

		// Default channel brightness will be (globalBrightness * 1); 
		setBrightness(1);  // default channel output will be globalBrightness * 1;
	}

	// per pixel data offset (0-3) of each color in pixel data
	public void setColorOrder(int r, int g, int b, int w) {
		color_order = (byte) (r | (g << 2) | (b << 4) | (w << 6));  
		outgoing[offs_color_order] = color_order;
	}

	public void setColorOrder(int r,int g,int b) {
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

	public void setPixelCount(int n) {
		pixel_count = n;
		packShort(offs_pixel_count,n);
	}     

	// Utility - Returns the offset of the 0th pixel in the channel's
	// packet buffer. This gives us a quick path for setting a whole
	// channel's pixels in one shot with as few function calls and as
	// little memory copying as we can manage.
	public int getPixelBufferOffset() {
		return header_size;
	} 

	// must be overridden by child classes
	public void setPixel(int index,int c) {
		//	    println("Bogus call to theoretically virtual object. Just.. no!");
	}	    

    /**	
	  Sets brightness for this channel.
	 * 
	 * @param b (0..1) brightness
	 */
	public void setBrightness(float b) {
		brightness = PApplet.constrain(b,0,1); // store this channel's brightness
		float bri = brightness * board.getGlobalBrightness();  // scale w/global brightness

		// build brightness/gamma translation table
		// Variable gamma configuration is done by power curve, which can 
		// actually be fairly accurate, depending on the power you choose and your LEDs.		
		for (int i = 0; i < levelTable.length;i++) {
			float val = ((float) i)/levelTable.length;
			val = (float) Math.pow(val,gammaCorrection);
			levelTable[i] = (byte) Math.floor(val * bri*255);
		}    
	}

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
		setBrightness(brightness);  //rebuild byte table
	}

	public float getGlobalBrightness() {
		return board.getGlobalBrightness();
	}		

}  