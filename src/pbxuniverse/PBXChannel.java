package pbxuniverse;

import java.util.zip.CRC32;

/**
 * Top level container class for all Pixelblaze Output Expander (PBX) channels.
 * 
 * Implements data and utilities common to and used by all channels. Under normal 
 * circumstances, this class should not be used in in programs.  For a general purpose
 * channel object container class, use PBXDataChannel in your sketches instead. 
 */
public class PBXChannel {
	final static int MAX_PIXELS_PER_CHANNEL = 800;
	final static int CHANNEL_BUFFER_SIZE = 16+(MAX_PIXELS_PER_CHANNEL * 4);
	final	static int CRC_SIZE = 4;

	// Channel packet types
	final static byte CH_WS2812_DATA = 1;
	final	static byte CH_DRAW_ALL = 2;
	final static byte CH_APA102_DATA = 3;
	final static byte CH_APA102_CLOCK = 4;	

	PBXSerial outPort;

	byte channel_number;
	byte channel_type;
	CRC32 crc;
	int crc_offset;
	byte [] outgoing;

	// create and initialize a channel command packet
	PBXChannel(PBXSerial port, byte ch_number, byte ch_type) {

		outPort = port;  // open serial port object

		// data for the more-or-less invariant packet header
		channel_number = ch_number;
		channel_type = ch_type;
		crc = new CRC32();
		crc_offset = -1;

		// defer creation of output buffer 'till we
		// know how big our packet is going to be.
		outgoing = null;   
	}

	// create packet-type specific output buffer and initialize the invariant portion
	// of the outgoing packet.
	void initOutputBuffer(int bufferSize) {
		outgoing = new byte[bufferSize+CRC_SIZE];
		crc_offset = bufferSize;

		int index = 0;
		outgoing[index++] = 'U'; 
		outgoing[index++] = 'P';
		outgoing[index++] = 'X';
		outgoing[index++] = 'L';   
		outgoing[index++] = channel_number;
		outgoing[index] = channel_type;    
	}

	// Utility - pack 16 bit value into output buffer at the specified
	// index. 
	void packShort(int index,int val) {
		outgoing[index++] = (byte) (val & 0xFF); val = val >> 8;
		outgoing[index] = (byte) (val & 0xFF);
	}

	// Utility - pack 32 bit value into output buffer at specified index
	void packInt(int index,int val) {
		outgoing[index++] = (byte) (val & 0xFF); val = val >> 8;
		outgoing[index++] = (byte) (val & 0xFF); val = val >> 8;
		outgoing[index++] = (byte) (val & 0xFF); val = val >> 8;    
		outgoing[index] = (byte) (val & 0xFF);
	}

	// calculate CRC value for a filled output buffer
	long calc_crc() {
		crc.reset();
		crc.update(outgoing,0,outgoing.length - CRC_SIZE);
		return crc.getValue() & 0xFFFFFFFF;    
	}  

	// Send channel packet over the wire, followed by its CRC value
	void send() {
		packInt(crc_offset,(int) calc_crc());
		outPort.write(outgoing);
	}  

	/**
	  Returns the composite board id/channel number as an int
	 */
	public int getChannelNumber() {
		return Byte.toUnsignedInt(channel_number);
	}
	
	/**
	 * Returns the channel type enum for this channel. Should be overridden
	 * to return the correct type for all data and clock channels. For other
	 * channel types, always returns ChannelType.SYSTEM.
	 */
	public ChannelType getChannelType() {
	   return ChannelType.SYSTEM;
	}	

	// given a color initial "R","G","B","W", and a string specifying
	// the order of colors for an LED channel, return the index of the
	// target color in the string.
	int parseColorString(String target, String cs) throws IllegalArgumentException {
		int result;

		// abort if too long or too short
		if (cs.length() < 3 || cs.length() > 5) {
			throw new IllegalArgumentException("Warning:  Invalid color order string. Invalid length."+cs);
		}

		target = target.toUpperCase();
		cs = cs.toUpperCase();

		if (target.equals("R")) {
			result = cs.indexOf("R");
			if (result < 0) {
				throw new IllegalArgumentException("Error: Red not found in color order string");
			}
		} else if (target.equals("G")) {
			result = cs.indexOf("G");
			if (result < 0) {
				throw new IllegalArgumentException("Error: Green not found in color order string");
			}
		} else if (target.equals("B")) {
			result = cs.indexOf("B");
			if (result < 0) {
				throw new IllegalArgumentException("Error: Blue not found in color order string");
			}
		} else if (target.equals("W")) {
			result = cs.indexOf("W");
		} else if (target.equals("-W")) {
			result = cs.indexOf("-W");
		} else {
			throw new IllegalArgumentException("Error:  Invalid color order specifier. "+cs);
		}
		return result;
	} 
}
