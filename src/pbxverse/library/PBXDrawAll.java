package pbxverse.library;

//Command packet - DRAW_ALL. Render all pixels that have been
//sent to channels on this port.  You only need one of these per port regardless
//of how many boards or channels it has.
public class PBXDrawAll extends PBXChannel {
	static final int header_size = 6;  

	PBXDrawAll(PBXSerial port) {
		super(port,(byte) 0xFF,CH_DRAW_ALL);
		// fixed size and content -- this is all we need,
		initOutputBuffer(header_size);   
	}    
}