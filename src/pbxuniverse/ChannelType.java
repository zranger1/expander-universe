package pbxuniverse;

/**
 * Channel types which can be used with addChannel to create LED channels.
 * <p>
 * These types reflect the underlying protocol rather than the physical LED model. At
 * this writing, most addressable LEDs use either the WS2812 (3-wire, no clock) or the
 * APA102 (4-wire, clock required) protocol.  <p>
 * If using any APA102-type LEDs, you must create a clock channel.  If you don't specify
 * a clock frequency, the frequency will default to 2000000 bits/sec. 
 */
public enum ChannelType {
	WS2812,
	APA102,
	APACLOCK
}
