package pbxuniverse;

import processing.core.*;
import processing.serial.*;
import java.util.concurrent.*;

// serial port with the ability to send a "DRAW ALL" command, because
// you only need to send the command once per port per frame.
public class PBXSerial extends Serial {
	static final int DATA_RATE = 2000000;
	String name;
	boolean isRunning;
	PBXPortThread pThread;
	PBXDrawAll draw_cmd;
	SynchronousQueue<byte[]> q;

	PBXSerial(PApplet pApp, String portName) {
		super(pApp,portName,DATA_RATE);
		this.name = portName;
		this.isRunning = false;
		pThread = new PBXPortThread(this);
		draw_cmd = new PBXDrawAll(this);
		q = new SynchronousQueue<byte[]>();
		pThread.start();
	}    

	public String getPortName() {
		return name;
	}

	public void drawAll() {
		draw_cmd.send();
	}
	
	public void threadedWrite(byte[] buf) {
	   // queue write request for the port's thread, blocking
	   // if there's already a request pending.
	   try {
		  q.put(buf);
	   } catch (InterruptedException e) {
 
		  e.printStackTrace();
	   }
	}
	
	// How we're gonna multithread serial writes!
	class PBXPortThread extends Thread {
		PBXSerial port;
		boolean locked;
		
		PBXPortThread(PBXSerial port) {
			this.port = port;
		}
		
		// running portion of thread... waits for write requests
		public void run() {
			byte[] pkt = null;

			while (isRunning) {
				try {
					pkt = port.q.take();
					port.write(pkt);					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	    // start port thread.  Automatically called by PBXSerial constructor
	    // once port is successfully opened.
		public void start() {
			System.out.println("PBXPortThread started for port "+name);  
			isRunning = true;
			super.start();
		}	    
	    
	}	
}