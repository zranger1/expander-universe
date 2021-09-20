## NOTES
This is a power tool. Coding is required. It is not meant for absolute beginners. It gives you great power and the attendant great responsibility. To use it successfully, you have to be comfortable with at least a little Processing programming, and know enough about electricity to wire all your lights together without setting things on fire.  

### Wiring

I have my computer->LED system conficured up as described below.  I have chosen to power the Output Expander from the USB port.  If you do this,
**DO NOT TRY TO POWER LEDS FROM THE EXPANDER. Be f'ckin careful about this. You could kill your PC.**  Instead, hook the LEDs up to their own
power supply and just be sure it has a common ground with the Output Expander so data transmission will work.  If you're
powering the Expander via USB **do not connect the +5v from the power supply to the Output expander in any way, shape or form.**

**(5v systems only)**

With the "normal" Output Expander, you can't run enough power through the board to drive very many LEDs anyway.  The pro version
is a different matter.  If you have one of these, know what you're doing, and want to route power through the expander to keep
wiring simple, I'd suggest leaving the power wire from the USB interface unconnected.  You can then use your main power supply to power the Output Expander and the LEDs will get power from it.

- Connect the power and ground wires from the USB interface to the Output Expander.  Make sure the USB->Serial board is set up for 5v if it is switchable.
- Connect the USB Interface's Tx line to the Output Expander's Rx line.
- When hooking up LEDs, connect only the data and clock lines to the Output Expander.
- Connect all ground lines to a common ground line.  (At this point, USB and Output Expander grounds are connected, so you need to 
connect grounds from the power supplies, LEDs, and the Output Expander to the common ground bus. )
= Connect the LED 

**(12v and 24v systems)**
If you're using a 12v or 24v system, you'll need to connect and ground the data lines, which run at a 5v logic level to the Output
Expander.  But the higher voltage power must only run to the places that can handle it -- from the power supplies to the LED's power wires.

### USB line length
USB is a low voltage serial protocol. It suffers from voltage drop and signal degradation just like everything else. You can get at least 3 meters out of ordinary USB extension cables.  After that, you'll need to use active cables, which regenerate the signal along the way.  I've used 30-foot versions for VR, and seen 50-foot versions for sale.   

### USB Hubs
Hub implementations vary.  I have no idea if a given USB hub will or won't work.  No reason why a well designed hub shouldn't though.  Even in the USB 2.0 spec, there's plenty of bandwidth to run several serial channels.

### Compatible computers
If it can run Processing and display graphic at a reasonable (30+ fps) frame rate, it'll probably work.  I've tested successfully with Windows 10, Ubuntu 18.04LTS and Debian 10.

Macs ought to be ok too, on the "if it can run Processing..." theory.  Again though, I don't really know. I haven't owned a Mac for a decade (and I wound up installing Linux on that one!) and I don't know what the USB->serial driver situation is.  

It *might* work on the more powerful models of Raspberry Pi, but you may need to install drivers for the USB->Serial device.  




Add to readme:
RGBW, -W support notes