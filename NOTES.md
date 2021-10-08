## ExpanderVerse NOTES

#### What's New
#####b0.3.0 
- fixes and improvements to automatic map building and coordinate normalization
- added PBXTools static utility class, with helpful things like triangle(), wave() and square().  More
to come!
- added drag and drop installer for Processing 4


#####b0.2.0 
- extended dynamic range for APA102-class LEDs. is now in and enabled by default.
- per channel color correction API - lets you (at least try to) match whites across multiple
LED types. 
- minor refactoring of channel creation for simplicity and consistency.  

#### A Word of Caution
ExpanderVerse is a power tool. Coding is required. It is not meant for absolute beginners. To use it successfully, you have to be comfortable taking on at least a little Processing/Java programming, and know enough about electricity to wire all your lights together without setting things on fire.  

### Wiring

I have my development setup wired as described below under the 5v systems heading.

**General Notes**

If you choose to power the Output Expander from a USB port, be sure you **DO NOT TRY TO POWER LEDS FROM THE EXPANDER. Be super careful about this. You could damage your computer by drawing too much power.** 

Instead, hook the LEDs up to their own power supply andbe sure it has a common ground with the Output Expander so data transmission will work.  If you're powering the Expander via USB **do not connect the +5v from your LED power supply to the Output expander in any way, shape or form.**

With the "normal" Pixelblaze Output Expander, you can only run enough power through the board to handle a few LEDs anyway.  The "pro" version is a different matter. If you have one of these, and want to route power through the expander to keep wiring simple, I'd suggest **not** powering the expander board from USB. You can then use your main power supply to power the Output Expander and the LEDs can safely draw their power from that.


**For 5v Systems**

- Connect the power and ground wires from the USB interface to the Output Expander.  Make sure the USB->Serial board is set up for 5v if it is switchable.
- Connect the USB Interface's Tx line to the Output Expander's Rx line.
- When hooking up LEDs, connect only the data and clock lines to the Output Expander.
- Connect all ground lines to a common ground line.  (At this point, USB and Output Expander grounds are connected, so you need to 
connect grounds from the power supplies, LEDs, and the Output Expander to the common ground bus. )
= Connect the LED 

**For 12v and 24v systems**

Data and clock wiring is the same as in a 5v system - You'll need to connect and ground the data and clock lines, which run at a 5v logic level to the Output Expander.

But take care that higher voltage power only run to the places that can handle it -- from the power supplies to the LED's power wires. 

### USB line length
USB is a low voltage serial protocol. It suffers from voltage drop and signal degradation just like everything else. You can get at least 3 meters out of ordinary USB extension cables.  After that, you'll need to use active cables, which regenerate the signal along the way.  I've used 30-foot versions for VR, and seen 50-foot versions for sale.  If you need more, you can extend to over 100 meters with a USB->Ethernet bridge. 

### USB Hubs
Hub implementations vary.  I have no idea if a given USB hub will or won't work.  No reason why a well designed hub shouldn't though.  Even in the USB 2.0 spec, there's plenty of bandwidth to run several serial channels.

### Roadmap
Coming soon:
- Power analysis - will tell you the peak and average wattage used during a run.
- Mapping tools:  multiple maps, more shape tools for mapping, more examples.
- Way more documentation.  Javadocs, tutorials, everything...