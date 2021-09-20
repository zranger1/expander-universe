package pbxuniverse;

import java.util.*;

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

	public PBXSerial getSerialPort() {
		return port;
	}

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
	// TODO - should I just set this up w/the first apa data channel so the user doesn't have
	// to worry about it?  Probably.
	public PBXDataChannel addChannelAPAClock(int chNumber,int frequency) {     
		PBXDataChannel ch = new PBXChannelAPAClock(this,(byte) chNumber,frequency);
		addChannel(ch);
		return ch;
	}
	
	public void addChannel(PBXDataChannel ch) {
		channels.add(ch);
	}
	
	public float getGlobalBrightness() {
	  return port.parent.getGlobalBrightness();
	}
	
	// enable gamma correction on all this board's channels
	public void enableGammaCorrection() {
		for (PBXDataChannel ch : channels) {
			ch.enableGammaCorrection();
		}  				
	}
	
	// disable gamma correction on this board's channels
	public void disableGammaCorrection() {
		for (PBXDataChannel ch : channels) {
			ch.disableGammaCorrection();
		}  				
	}
	
	// return total number of pixels attached to this board
	public int getPixelCount() {
		int n = 0;
		for (PBXDataChannel ch : channels) {
			n += ch.getPixelCount();
		}  		
		return n;
	}
	
	// transmit all active channels to the expansion board.
	public void send() {
		for (PBXChannel ch : channels) {
			ch.send();
		}    
	}

	// set brightness for all channels attached to this board
	public void setBrightness(float b) {
		for (PBXDataChannel ch : channels) { ch.setBrightness(b); }    		
	}
}