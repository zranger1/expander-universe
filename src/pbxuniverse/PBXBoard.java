package pbxuniverse;

import java.util.*;

import processing.core.PApplet;

public class PBXBoard {
	static final int DATA_RATE = 2000000;
	PBXSerial port;
	int board_id;
	LinkedList<PBXDataChannel> channels;

	// allows up to 8 boards per channel
	PBXBoard(PBXSerial port,int boardId) {
		this.port = port;  		
		board_id =  (boardId << 3);
		channels = new LinkedList<PBXDataChannel>();
	}

	/**
	 *  Returns the serial port object to which this board is 
	 *  attached.
	 */
	public PBXSerial getSerialPort() {
		return port;
	}

	/**
	 * Returns the board id (0..7) of this board
	 */
	public int getBoardId() {
		return board_id;
	}

	// Create channel for RGB or RGBW WS2812/Neopixel class LEDs
	public PBXDataChannel addChannelWS2812(int chNumber,int pixelCount,String colorString) { 
		PBXDataChannel ch = new PBXChannelWS2812(this,(byte) chNumber, pixelCount,colorString);
		addChannel(ch);

		return ch;
	}

	// Create data channel for APA102/Dotstar LEDs
	public PBXDataChannel addChannelAPA102(int chNumber,int pixelCount,int frequency,String colorString) {   
		PBXDataChannel ch = new PBXChannelAPA102(this,(byte) chNumber,pixelCount,frequency,colorString);
		addChannel(ch);

		return ch;
	}

	// Create clock channel for APA102/Dotstar LEDs
	// if you use APA102s, you'll need to dedicate one clock channel.
	public PBXDataChannel addChannelAPAClock(int chNumber,int frequency) {     
		PBXDataChannel ch = new PBXChannelAPAClock(this,(byte) chNumber,frequency);
		addChannel(ch);
		return ch;
	}

	// add channel to this board's channel list
	void addChannel(PBXDataChannel ch) {
		channels.add(ch);
	}

	/**
	 * Gets the channel object specified by id 
	 * @param id - number of channel to retrieve
	 * @return PBXDataChannel object if found, null otherwise
	 */	
	public PBXDataChannel getChannel(int id) {
		PBXDataChannel result = null;

		for (PBXDataChannel ch : channels) {
			if (ch.getChannelNumber() == id) {
				result = ch;
				break;				
			};
		}  			

		return result;
	}

	/**
	 * Gets the list of channels associated with this board. The list
	 * may be empty.
	 * @return channel list
	 */
	public LinkedList<PBXDataChannel> getChannelList() {
		return channels;
	}

	public float getGlobalBrightness() {
		return port.parent.getGlobalBrightness();
	}

	/**
	 * Sets gamma correction factor (power curve exponenent) for all
	 * channels on the specified board. Setting at the board level will
	 * override all previous settings for channels attached to the board.
	 *   
	 * @param g global gamma factor (0..1)
	 */
	public void setGammaCorrection(float g) {

		for (PBXDataChannel ch : channels) {
			ch.setGammaCorrection(g);
		}  				
	}

	/**
	 *  Returns total number of pixels attached to this board
	 * 
	 * @return
	 */
	public int getPixelCount() {
		int n = 0;
		for (PBXDataChannel ch : channels) {
			n += ch.getPixelCount();
		}  		
		return n;
	}

	// transmit all active channels to the expansion board.
	void send() {
		for (PBXChannel ch : channels) {
			ch.send();
		}    
	}

	/**	
	 * Sets brightness for all channels connected to this board.  Setting
	 *  brightness at the board level will override all previous settings
	 *  for attached channels.
	 * 
	 * @param b (0..1) brightness
	 */
	public void setBrightness(float b) {
		b = PApplet.constrain(b,(float) 0.0,(float) 1.0);
		for (PBXDataChannel ch : channels) { ch.setBrightness(b); }    		
	}
	
	/**
	 * Sets r,g and b color correction factors - values in the range 0..1 that will be multiplied
	 * with pixel colors to produce the particular 'white' you want to match.
	 * Settings made at the board level will override previous settings for attached channels.  
	 */			
	public void setColorCorrection(float r,float g, float b) {
		for (PBXDataChannel ch : channels) { ch.setColorCorrection(r,g,b); }  
	}	
	
	/**
	 * Sets pixel drawing mode, either DrawMode.FAST (brightness & gamma adjustment only) or
	 * DrawMode.ENHANCED (brightness, gamma, color correction, color depth expansion if supported by LEDs).
	 * DrawMode can be changed at any time.
	 * Settings made at the board level will override previous settings for attached channels.  
	 */	
	public void setDrawMode(DrawMode dm) {
		for (PBXDataChannel ch : channels) { ch.setDrawMode(dm); }  		
	}
	
}