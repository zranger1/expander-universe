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
	
	public PApplet getApplet() {
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
	public void addBoard(PBXBoard b) {
		boards.add(b);
	}
	
	public void enableGammaCorrection() {
		for (PBXBoard b : boards) { b.enableGammaCorrection(); }			
	}
	
	public void disableGammaCorrection() {
		for (PBXBoard b : boards) { b.disableGammaCorrection(); }			
	}	
	
	public void setGlobalBrightness(float bri) {
		for (PBXBoard b : boards) { b.setBrightness(bri); }			
	}
		
	/**
	 * Gets the total number of pixels associated with this output expander 
	 * @return pixel count
	 */
	public int getPixelCount() {
		int n = 0;
		for (PBXBoard b : boards) { n += b.getPixelCount(); }		
		return n;
	}	
/* THE OLD WAY		
	public synchronized void requestSerialIO(int cmd) {
		// if the serial comms thread is busy, wait for it to finish,
		// then issue the next command.
		while (writeCommand != NO_COMMAND) {
            try { 
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt(); 
            }	
		}
		writeCommand = cmd;
		notifyAll();
	}
	
	public synchronized void ioThreadRunner() {
		// keep this thread blocked as much as possible while waiting for
		// a command
		while (writeCommand == NO_COMMAND) {
			try {
				wait();
			} catch (InterruptedException e)  {
				Thread.currentThread().interrupt(); 
			}						
		}

		// when a write request arrives, the main thread will block 'till we're
		// done sending.
		switch(writeCommand) {
		case CMD_SEND_DATA:
			for (PBXBoard b : boards) { b.send(); }
			writeCommand = NO_COMMAND;
			break;
		case CMD_DRAW_ALL:
			draw_cmd.send();
			writeCommand = NO_COMMAND;
			break;					
		}
		notifyAll();		
	}
	
*/	
	
	public void requestSerialIO(int cmd) {
		// This busy wait is a couple of fps faster than the more "correct" locking
		// version. If the serial comms thread is busy, spin while waiting for it to finish
		// before issuing the next command.
		while (writeCommand.get() != NO_COMMAND) {
	      Thread.yield();
		}
		writeCommand.set(cmd);;
	}
		
	public void sendPixelData() {
        requestSerialIO(CMD_SEND_DATA);
	}
	
	public void sendDrawAll() {
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