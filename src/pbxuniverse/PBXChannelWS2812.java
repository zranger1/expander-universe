package pbxuniverse;


public class PBXChannelWS2812 extends PBXDataChannel {
	boolean isRGBW;

	// full constructor -- all channel parameters
	// TODO - we still don't really handle W, or the -W orders correctly
	PBXChannelWS2812(PBXBoard brd, byte ch_number,int pixelCount, String colorString) {
		super(brd, ch_number,CH_WS2812_DATA);

		// set data offsets in packet for this type
		header_size = 10; 
		offs_bpp = 6;
		offs_color_order = 7;
		offs_pixel_count = 8;    

		// decode color string to get color order and bytes per pixel  
		int r,g,b,w;
		byte bpp = 3;
		isRGBW = false;

		// figure out how many bytes per pixel. 4 if there's a white or -white channel, 3 otherwise
	
		// see if there's a white channel at all
		w = parseColorString("W",colorString);		
		if (w >= 0) {
			bpp = 4; 
					
            // check for a -W (white disabled) configuration
			w = parseColorString("-W",colorString);
			if (w >= 0) {
				isRGBW = false;
			}
			// if not, then we're a regular RGBW channel
			else {
				isRGBW = true;
			}					
		}
		// if no white channel, we're a 3 byte per pixel RGB channel.
		else {
			bpp = 3;
		}
		
		r = parseColorString("R",colorString);
		g = parseColorString("G",colorString);
		b = parseColorString("B",colorString);    

		initOutputBuffer(header_size+(pixelCount * (int)bpp));

		setBytesPerPixel(bpp);
		setColorOrder(r,g,b,w);
		setPixelCount(pixelCount);
		setDrawMode(DrawMode.FAST);
	}

	// 3 channels, RGB color order.  
	PBXChannelWS2812(PBXBoard brd, byte ch_number, int pixelCount) {
		this(brd, ch_number,pixelCount,"RGB");
	}

	// bytes per pixel - for WS2812 channels only
	void setBytesPerPixel(byte n) {
		bytes_per_pixel = n;
		outgoing[offs_bpp] = bytes_per_pixel;
	}  
	
	// Internal - sets DrawMode.FAST renderers as needed for 
	// pixel depth and configuration
	void setFastDrawMode() {
		if (bytes_per_pixel == 4) {
		  ps = (isRGBW == true) ?  new _RGBWSetter() : new _RGBxSetter();
		}
		else {
		  ps = new _RGBSetter();			
		}				
	}
	
	// Internal - sets DrawMode.ENHANCED renderers as needed for 
	// pixel depth and configuration.
	void setEnhancedDrawMode() {
		if (bytes_per_pixel == 4) {
			  ps = (isRGBW == true) ?  new _ERGBWSetter() : new _ERGBxSetter();
		}
		else {
			ps = new _ERGBSetter();	
		}
	}			
	
	/**
	 * Sets pixel drawing mode, either DrawMode.FAST (brightness & gamma adjustment only) or
	 * DrawMode.ENHANCED (brightness, gamma, color correction, color depth expansion if supported by LEDs).
	 * DrawMode can be changed at any time.
	 */
	public void setDrawMode(DrawMode dm) {
		super.setDrawMode(dm);
	
		switch (dm) {
		case FAST:
			setFastDrawMode();
			break;
		case ENHANCED:
			setEnhancedDrawMode();
			break;
		}
	}	
	
	// set pixel according to channel LED format
	void setPixel(int index,int c) {
		ps.setPixel(index, c);
	}
	
	// 3-byte RGB setPixel for WS2812    
	protected class _RGBSetter implements _pixelSetter {

		public void setPixel(int index,int c) {
			index = header_size + (index * bytes_per_pixel);
			outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
			outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
			outgoing[index++] = levelTable[c & 0xFF];          // B
		}					
	}
	
	// 4-byte RGB-W setPixel for WS2812    
	// ignores white data, if any
	protected class _RGBxSetter implements _pixelSetter {
		public void setPixel(int index,int c) {
			index = header_size + (index * bytes_per_pixel);
			outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
			outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
			outgoing[index++] = levelTable[c & 0xFF];          // B
			outgoing[index] = (byte) 0;   // no white
		}
	}
	
	// 4-byte RGBW setPixel for WS2812 
	protected class _RGBWSetter implements _pixelSetter {
		public void setPixel(int index,int c) {
			c = RGBtoRGBW(c);
			index = header_size + (index * bytes_per_pixel);
			outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
			outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
			outgoing[index++] = levelTable[c & 0xFF];          // B
			outgoing[index] = levelTable[(c >> 24) & 0xFF];    // W
		}	
	}
	
	// 3-byte enhanced RGB setPixel for WS2812    
	protected class _ERGBSetter implements _pixelSetter {

		public void setPixel(int index,int c) {
			index = header_size + (index * bytes_per_pixel);
			c = correctColor(c);            
			outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
			outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
			outgoing[index++] = levelTable[c & 0xFF];          // B
		}					
	}
	
	// 4-byte enhanced RGB-W setPixel for WS2812    
	// ignores white data, if any
	protected class _ERGBxSetter implements _pixelSetter {
		public void setPixel(int index,int c) {
			index = header_size + (index * bytes_per_pixel);
			c = correctColor(c);            
			outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
			outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
			outgoing[index++] = levelTable[c & 0xFF];          // B
			outgoing[index] = (byte) 0;   // no white
		}
	}
	
	// 4-byte enhanced RGBW setPixel for WS2812 
	protected class _ERGBWSetter implements _pixelSetter {
		public void setPixel(int index,int c) {
			c = correctColor(c);        
			c = RGBtoRGBW(c);
			index = header_size + (index * bytes_per_pixel);
			outgoing[index++] = levelTable[(c >> 16) & 0xFF];  // R
			outgoing[index++] = levelTable[(c >> 8) & 0xFF];   // G;
			outgoing[index++] = levelTable[c & 0xFF];          // B
			outgoing[index] = levelTable[(c >> 24) & 0xFF];    // W
		}	
	}	

	// do what the function name says.
	// TODO -- long term how to make this faster still??!
	// it looks a lot like W ends up being mostly the smallest component
	// of RGB.
	int RGBtoRGBW(int rgb) {
		float w,briMax,briMin;
		int tmp;
		int rgbw = tmp = 0;

		// convert to floating point
		float r = (float) (((rgb >> 16) & 0xFF));
		float g = (float) (((rgb >> 8) & 0xFF));
		float b = (float) ((rgb & 0xFF));

		// whiteness is inversely related to the range between min and max, so...     
		// find brightest component
		briMax = Math.max(r, Math.max(g, b));

		// early out if max is zero 
		if (briMax == 0) return rgbw;

		// find dimmest component
		briMin = Math.min(r,Math.min(g, b));

		// find and normalize the range
		w = 1-((briMax - briMin) / briMax);
		w = w * briMax;

		// calculate the new values for rgbw
		rgbw = (int) Math.floor(b - w);                          // blue
		tmp = (int) Math.floor(g-w); rgbw = rgbw + (tmp << 8);   // green
		tmp = (int) Math.floor(r - w); rgbw = rgbw + (tmp << 16);// red
		tmp = (int) Math.floor(w); rgbw = rgbw + (tmp << 24);    // white      

		// and return 32 bits of color 
		return rgbw;
	} 		
}