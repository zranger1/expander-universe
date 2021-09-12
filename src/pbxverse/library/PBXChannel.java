package pbxverse.library;

import java.util.zip.CRC32;

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
	PBXBoard board;
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
		board = null;  
	}

	// create packet-type specific output buffer and initialize the invariant portion
	// of the outgoing packet.
	public void initOutputBuffer(int bufferSize) {
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
	public void packShort(int index,int val) {
		outgoing[index++] = (byte) (val & 0xFF); val = val >> 8;
		outgoing[index] = (byte) (val & 0xFF);
	}

	// Utility - pack 32 bit value into output buffer at specified index
	public void packInt(int index,int val) {
		outgoing[index++] = (byte) (val & 0xFF); val = val >> 8;
		outgoing[index++] = (byte) (val & 0xFF); val = val >> 8;
		outgoing[index++] = (byte) (val & 0xFF); val = val >> 8;    
		outgoing[index] = (byte) (val & 0xFF);
	}

	// calculate CRC value for a filled output buffer
	public long calc_crc() {
		crc.reset();
		crc.update(outgoing,0,outgoing.length - CRC_SIZE);
		return crc.getValue() & 0xFFFFFFFF;    
	}  

	// Send channel packet over the wire, followed by its CRC value
	// "outgoing" packed buffer must be properly constructed or this will not work! 
	// TODO - I could test for this, but um... no. 
	public void send() {
		packInt(crc_offset,(int) calc_crc());
		outPort.write(outgoing);
	}  

	// Utility - helper for PBXBoard aggregator class
	// returns composite board id/channel number as int
	public int getChannelNumber() {
		return Byte.toUnsignedInt(channel_number);
	}

	public void setBoard(PBXBoard b) {
		this.board = b;
		channel_number = (byte) (0xFF & ((b.GetBoardId() << 3) | (int)channel_number));
	}
	
	public float getGlobalBrightness() {
		return board.getGlobalBrightness();
	}

	// given a color initial "R","G","B","W", and a string specifying
	// the order of colors for an LED channel, return the index of the
	// target color in the string.
	public int parseColorString(String target, String cs) throws IllegalArgumentException {
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
