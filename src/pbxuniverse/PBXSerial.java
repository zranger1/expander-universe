package pbxuniverse;

import processing.core.*;
import processing.serial.*;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

// serial port with the ability to send a "DRAW ALL" command, because
// you only need to send the command once per port per frame.
public class PBXSerial extends Serial {
	static final int DATA_RATE = 2000000;
	static final int MAX_CHANNELS = 65;  // max channels per serial port
	static final int NO_COMMAND = 0;	
	static final int CMD_SEND_DATA = 1;
	static final int CMD_DRAW_ALL = 2;
	
	String name;
	ExpanderVerse parent;
	boolean isRunning;
	PBXPortThread pThread;
	PBXDrawAll draw_cmd;
	LinkedList<PBXBoard> boards;  // boards attached to this port
	AtomicInteger writeCommand;

	PBXSerial(ExpanderVerse parent, String portName) {
		super(parent.pApp,portName,DATA_RATE);
		this.parent = parent;
		this.name = portName;
		this.isRunning = false;
		pThread = new PBXPortThread(this);
		draw_cmd = new PBXDrawAll(this);
		boards = new LinkedList<PBXBoard>();
		writeCommand = new AtomicInteger();
		writeCommand.set(NO_COMMAND);;
		pThread.start();
	}    
	
	PApplet getApplet() {
		return parent.pApp;
	}

	public String getPortName() {
		return name;
	}
	
	/**
 	 * Create a new board, attach it to this port and return the object.  Board 
  	 * number is the hardwired board id on the bus. Setting a board
	 * to an ID other than zero requires physically cutting a trace
	 * on the board.
	 * @param boardNo - id of the board, 0-7
	 * @return new PBXBoard object 
	 */
	public PBXBoard addOutputExpander(int boardNo) {
		PBXBoard b = new PBXBoard(this,boardNo);
		this.addBoard(b);
		return b;
	}

	/**
       Internal - add an expander board to the port's linked list.
	 */	
	void addBoard(PBXBoard b) {
		boards.add(b);
	}
	
	/**
	 * Gets the board object specified by id 
	 * @param id - board id number to retrieve
	 * @return PBXBoard object if found, null otherwise
	 */
	public PBXBoard getBoard(int id) {
		PBXBoard brd = null;
		
		for (PBXBoard b : boards) { 
			if (b.getBoardId() == id) {
				brd = b;
				break;
			}
		}
		
		return brd;
	}	
	
	/**
	 * Gets the list of output expander boards associated with this port. The list
	 * may be empty.
	 * @return board list
	 */
	public LinkedList<PBXBoard> getChannelList() {
		return boards;
	}	
		
	/**
	 * Sets gamma correction factor (power curve exponenent) for this port and and all
	 * boards and channels connected to it. Settings made at the port level will override
	 * previous settings for attached boards and channels. 
	 *   
	 * @param g global gamma factor (0..1)
	 */	
	public void setGammaCorrection(float g) {
		for (PBXBoard b : boards) { b.setGammaCorrection(g); }			
	}
	
    /**	
	 * Sets brightness for this port and all boards and channels connnected to it.
	 * Settings made at the port level will override previous settings for attached
	 * boards and channels. 
	 * @param b (0..1) brightness
	 */	
	public void setBrightness(float bri) {
		for (PBXBoard b : boards) { b.setBrightness(bri); }			
	}
	
	/**
	 * Sets r,g and b color correction factors - values in the range 0..1 that will be multiplied
	 * with pixel colors to produce the particular 'white' you want to match.
	 * Settings made at the port level will override previous settings for all attached boards and
	 * channels.  
	 */			
	public void setColorCorrection(float r,float g, float b) {
		for (PBXBoard brd : boards) { brd.setColorCorrection(r,g,b); }  
	}	
	
	/**
	 * Sets pixel drawing mode, either DrawMode.FAST (brightness & gamma adjustment only) or
	 * DrawMode.ENHANCED (brightness, gamma, color correction, color depth expansion if supported by LEDs).
	 * DrawMode can be changed at any time.
	 * Settings made at the port level will override previous settings for attached boards and channels.  
	 */	
	public void setDrawMode(DrawMode dm) {
		for (PBXBoard brd : boards) { brd.setDrawMode(dm); }  		
	}	
				
	/**
	 *  Returns total number of pixels attached to this output expander board
	 * 
	 * @return
	 */
	public int getPixelCount() {
		int n = 0;
		for (PBXBoard b : boards) { n += b.getPixelCount(); }		
		return n;
	}	

	void requestSerialIO(int cmd) {
		// This busy wait is a couple of fps faster than the more "correct" locking
		// version. If the serial comms thread is busy, spin while waiting for it to finish
		// before issuing the next command.
		while (writeCommand.get() != NO_COMMAND) {
	      Thread.yield();
		}
		writeCommand.set(cmd);;
	}
		
	void sendPixelData() {
        requestSerialIO(CMD_SEND_DATA);
	}
	
	void sendDrawAll() {
		requestSerialIO(CMD_DRAW_ALL);
	}

	// How we're gonna multithread serial writes!
	class PBXPortThread extends Thread {
		PBXSerial port;
		
		PBXPortThread(PBXSerial port) {
			this.port = port;
		}
		
		// running portion of thread... waits for write requests
		public void run() {
			while (isRunning == true) {
				// busy waiting version - faster than locking or synchronized()
				//
				while (writeCommand.get() == NO_COMMAND) {
		          ;// Thread.yield();						
				}
				// when a write request arrives, the main thread will spin 'till we're
				// done sending.
				switch(writeCommand.get()) {
				case CMD_SEND_DATA:
					for (PBXBoard b : boards) { b.send(); }		
					break;
				case CMD_DRAW_ALL:
					draw_cmd.send();		
					break;					
				}
				writeCommand.set(NO_COMMAND);
			}
		}

	    // start port thread.  Automatically called by PBXSerial constructor
	    // once port is successfully opened.
		public void start() {
			System.out.println("Starting ExpanderVerse serial thread on port "+name);  
			isRunning = true;
			super.start();
		}	   
		
		// left in in case we need it later.  Processing manages cleanup very nicely
		// without us.
		public void quit() {
			System.out.println("Stopping ExpanderVerse serial thread on port "+name); 
			isRunning = false;  
			interrupt();
		}	
	    
	}	
}