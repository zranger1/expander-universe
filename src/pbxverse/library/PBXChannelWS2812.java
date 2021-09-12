package pbxverse.library;

public class PBXChannelWS2812 extends PBXDataChannel {

	  // full constructor -- all channel parameters
	  // TODO - we still don't really handle W, or the -W orders correctly
	  PBXChannelWS2812(PBXSerial port,byte ch_number,int pixelCount, String colorString) {
	    super(port,ch_number,CH_WS2812_DATA);

	    // set data offsets in packet for this type
	    header_size = 10; 
	    offs_bpp = 6;
	    offs_color_order = 7;
	    offs_pixel_count = 8;    
	    
	    // decode color string to get color order and bytes per pixel  
	    int r,g,b,w;
	    byte bpp = 3;
	    
	    // figure out how many bytes per pixel. 4 if there's a white or -white channel, 3 otherwise
	    w = parseColorString("-W",colorString);
	    if (w >= 0) {
	      w = 0;
	      bpp = 4;
	    } else {  
	      w = parseColorString("W",colorString); 
	      if (w >= 0) bpp = 4;
	    }
	    
	    r = parseColorString("R",colorString);
	    g = parseColorString("G",colorString);
	    b = parseColorString("B",colorString);    
	    
	    initOutputBuffer(header_size+(pixelCount * (int)bpp));
	    
	    setBytesPerPixel(bpp);
	    setColorOrder(r,g,b,w);
	    setPixelCount(pixelCount);
	  }

	  // 3 channels, RGB color order.  
	  PBXChannelWS2812(PBXSerial port,byte ch_number, int pixelCount) {
	    this(port, ch_number,pixelCount,"RGB");
	  }
	    
	  // bytes per pixel - for WS2812 channels only
	  public void setBytesPerPixel(byte n) {
	    bytes_per_pixel = n;
	    outgoing[offs_bpp] = bytes_per_pixel;
	  }  
	     
	  // 4-byte RGBW setPixel for WS2812    
	  // ignore white for now -- we'll figure it out later
	  public void setPixel(int index,int c) {
	    index = header_size + (index * bytes_per_pixel);
	    outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
	    outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
	    outgoing[index++] = levelTable[c & 0xFF];          // B
	    if (bytes_per_pixel == 4) outgoing[index] = (byte) 0;   // no white
	  }
	 
	  // WS2812 - Copy sequential pixels from an array of color data to our outgoing 
	  // data packet buffer.  Handles both 3 and 4 byte cases.
	  // The source array must have enough pixels to fill the request or 
	  // mayhem will ensue. 
	  public void copyFromImagePixels(int[] pixels,int offset) {
	      
	     int pktPixel = getPixelBufferOffset();   
	     int lastPktPixel = pktPixel + (pixel_count * bytes_per_pixel);
	     int extra = (bytes_per_pixel != 4) ? 1 : 2;
	     
	     while (pktPixel < lastPktPixel) {
	       outgoing[pktPixel++] = (byte) ((pixels[offset] >> 16) & 0xFF); //R
	       outgoing[pktPixel++] = (byte) ((pixels[offset] >> 8) & 0xFF); //G
	       outgoing[pktPixel] = (byte) (pixels[offset] & 0xFF); //B
	       
	       // move to next pixel, skipping white byte if necessary
	       // TODO -- figure out how to generate good white data for strips
	       // that need this.
	       pktPixel += extra;   // skip white byte if necessary.
	       offset++;            // move to next PImage pixel       
	     }  
	  }   
	}