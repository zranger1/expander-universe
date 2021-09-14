package pbxuniverse;

public class PBXChannelAPA102 extends PBXDataChannel {
      int offs_filler;
	 
	  // can specify all channel parameters in the constructor
	  // APA protocol is always 4 bytes w/RGB in some order.  The [0]th byte contains 3 bits of flag,
	  // then 5 bits of global brightness.
      // Note that this structure is not actually packed. There's a filler byte after the color order byte to maintain
      // alignement.  The Pixelblaze always sends '0xFF' in this position.
	  // TODO - we're just maxing the extra APA brightness bits for now -- should do something interesting with it.
	  PBXChannelAPA102(PBXBoard brd,byte ch_number,int pixelCount,int freq, String colorString) {
	    super(brd, ch_number,CH_APA102_DATA);
	    
	    // set data offsets in packet for this type
	    header_size = 14; 
	    offs_frequency = 6;
	    offs_color_order = 10;
	    offs_filler = 11;
	    offs_pixel_count = 12;        
	    
	    int r,g,b;
	      
	    r = parseColorString("R",colorString);
	    g = parseColorString("G",colorString);
	    b = parseColorString("B",colorString);    
	        
	    initOutputBuffer(header_size+(pixelCount * 4));
	    
	    setColorOrder(r,g,b);
	    outgoing[offs_filler] = (byte) 0xff;  // replicate Pixelblaze's behavior
	    setFrequency(freq);       
	    setPixelCount(pixelCount); 
	  }

	  // RGB color order.  
	  PBXChannelAPA102(PBXBoard brd, byte ch_number, int pixelCount) {
	    this(brd, ch_number,2000000,pixelCount,"RGB");
	  }
	 
	  // Frequency - APA channels only
	  public void setFrequency(int f) {
	    frequency = f;
	    packInt(offs_frequency,frequency);
	  }  
	  
	  // RGB setPixel for APA 102 - sets extended brightness
	  // to maximum. (essentially ignoring it);
	  // TODO - support full dynamic range w/extended brightness bits
	  public void setPixel(int index,int c) {
	    index = header_size + (index * 4);
	    outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
	    outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
	    outgoing[index++] = levelTable[c & 0xff];          // B
	    outgoing[index++] = (byte) 0xff;	    
	  }  
	  
	  // APA102 - Copy sequential pixels from an array of color data to our outgoing 
	  // data packet buffer.  
	  // The source array must have enough pixels to fill the request or 
	  // mayhem will ensue. 
	  public void copyFromImagePixels(int[] pixels,int offset) {
	      
	     int pktPixel = getPixelBufferOffset();   
	     int lastPktPixel = pktPixel + (pixel_count * 4);
	     while (pktPixel < lastPktPixel) {
	       outgoing[pktPixel++] = (byte) ((pixels[offset] >> 16) & 0xFF); //R
	       outgoing[pktPixel++] = (byte) ((pixels[offset] >> 8) & 0xFF); //G
	       outgoing[pktPixel++] = (byte) (pixels[offset] & 0xFF); //B
	       outgoing[pktPixel++] = (byte) 0xff;  // extended brightness & flags in msb        
	     }  
	  }  
	}