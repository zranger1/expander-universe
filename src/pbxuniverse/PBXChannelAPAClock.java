package pbxuniverse;

//Sets up dedicated clock channel for any APA102 style LEDS attached to this port
//if you have APAs, you need a clock channel. TODO - set this up automatically when
//the first APA channel is created. (The channel gets physically hooked up to the clock input
//on the APA102 LEDs attached to this board.)
//
public class PBXChannelAPAClock extends PBXDataChannel {

	PBXChannelAPAClock(PBXBoard brd, byte ch_number,int freq) {
		super(brd, ch_number,CH_APA102_CLOCK);  

		header_size = 10;
		offs_frequency = 6;
		initOutputBuffer(header_size);  
		setChannelFrequency(freq);
	} 

	// output clock frequency - for APA channels only 
	void setChannelFrequency(int f) {
		frequency = f;
		packInt(offs_frequency,f);
	}  
}