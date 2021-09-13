package pbxuniverse;

import processing.core.*;
import processing.serial.*;

// serial port with the ability to send a "DRAW ALL" command, because
// you only need to send the command once per port per frame.
public class PBXSerial extends Serial {
	static final int DATA_RATE = 2000000;
	String name;
	PBXDrawAll draw_cmd;

	PBXSerial(PApplet pApp, String portName) {
		super(pApp,portName,DATA_RATE);
		this.name = portName;
		draw_cmd = new PBXDrawAll(this);
	}    

	public String getPortName() {
		return name;
	}

	public void drawAll() {
		draw_cmd.send();
	}
}