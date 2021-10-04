package pbxuniverse;

/**
 * Holds settings APA102 clock channels
 * 
   APA102 LEDs require that one output channel on the expander board be set up as
   a dedicated clock channel. If you have multiple APA102 strips attached to a board,
   you can connect them all to the same clock channel.  Valid clock speeds range from
   250khz to 20Mhz
 */
public class PBXChannelAPAClock extends PBXDataChannel {

	PBXChannelAPAClock(PBXBoard brd, byte ch_number,int freq) {
		super(brd, ch_number,CH_APA102_CLOCK);  

		header_size = 10;
		offs_frequency = 6;
		initOutputBuffer(header_size);  
		setChannelFrequency(freq);
	} 

	/**
	 * Output clock frequency for APA102 channels. Per-channel frequency settings
	 * may override this setting, and if you have both APA and WS2812 channels present
	 * on a single expansion board, the clock frequency will be fixed at the WS2812's
	 * communication speed - 800khz.
	 * @param f frequency in hz.  Valid range is 250000 (250khz) to
	 * 20000000 (20Mhz).
	 */
	void setChannelFrequency(int f) {
		frequency = f;
		packInt(offs_frequency,f);
	}  
}