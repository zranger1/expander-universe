package pbxverse.library;

import processing.core.*;
import java.util.*;

public class PBXBoard {
	static final int DATA_RATE = 2000000;
    ExpanderVerse parent;
	PBXSerial outPort;
	int board_id;
	LinkedList<PBXDataChannel> channels;

	// allows up to 8 boards per channel
	PBXBoard(ExpanderVerse parent, PBXSerial port,int boardId) {
		this.parent = parent;
		board_id =  (boardId << 3);
		outPort = port;  
		channels = new LinkedList<PBXDataChannel>();
	}

	public PBXSerial GetSerialPort() {
		return outPort;
	}

	public int GetBoardId() {
		return board_id;
	}

	public void AddChannel(PBXDataChannel ch) {
		ch.setBoard(this);
		channels.add(ch);
	}
	
	public float getGlobalBrightness() {
	  return parent.getGlobalBrightness();
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

	// transmit all active channels to the expansion board.
	public void Send() {
		for (PBXChannel ch : channels) {
			ch.send();
		}    
	}

}