## ExpanderVerse ##

(Beta 0.1.0) - Things will be changing frequently 'till the 1.0 release!

ExpanderVerse lets you drive addressable LEDs with your computer, a USB->Serial adapter, and a [Pixelblaze Output Expander](https://www.bhencke.com/serial-led-driver) board.  Potentially *many* LEDs. 

#### Introduction
I'm a big fan of the Pixelblaze. It's the fastest way to get to the fun, creative part of an LED project. However, I've been working on a large scale, stationary art project, in a situation where reliable wifi was not going to be possible.  I needed wired control over thousands of LEDs at a reasonably high frame rate, and very high reliability.  The Pixelblaze Output Expander board provided a great way to drive all those LEDs, but for the multimedia, interactive bits, I was going to need a heftier controller.

So, I wrote ExpanderVerse.  If...

- You're comfortable with power and data wiring for LEDs.
- You're familiar with [Processing](www.processing.org), or willing to dive in and learn.
- You have plenty of A/C power at your site, or at least very long extension cords
- You want to smoothly animate an installation with several thousand LEDs.
- You want the reliability and security of a hardwired interface.
- You'd like a centralized point of control and syncrhonization for your project.
- You need to interact with or sync to the internet or other media, or to use the vast array of interface devices
which communicate via PC.

ExpanderVerse and a whole bunch of Output Expander boards might be exactly what you need.

#### Features
- Supports multiple serial ports (as many as your computer can handle) and multiple output expander boards (up to 8) per port.
Serial I/O is multithreaded to maximize bandwidth for large installations.
- Each expander board channel can drive up to 800 RGB WS2812s, or 600 RGBW LEDs or APA102s. 
- Mix and match LED types, then adjust them to match brightness and white balance. ExpanderVerse supports brightness and gamma correction control from the global level down to the individual channel, and (optional, and at this point experimental) per-channel color balance adjustment.
- Supports Pixelblaze-style 2D and 3D coordinate mapping, and can import JSON Pixelblaze maps.  Retains both world and normalized maps for use in your patterns.
- Render in [Processing](www.processing.org), then send to your LEDs!  Supports "index mapping" to allow easy, one step transfer of 2D image sections from Processing's PImage and PGraphics objects. 
- Supports use of the GPU (via OpenGL fragment shaders) for massively parallel rendering of LED data. You can write LED shaders! A few tens of thousands of LED pixels are easy work for a graphics card used to pumping out 4k video at 60fps or better.

#### Required Hardware
- A computer that will run [Processing](www.processing.org) at whatever speed you require, equipped with one or more USB ports (USB 2 minimum)
- A USB->Serial adapter that will run at 2,000,000 baud.  Not all of them will work, particularly those that use "clone" chipsets.  Look for one with a genuine FTDI chip.  Here's an Amazon link to one that's known to work: [USB to TTL Adapter, USB to Serial Converter for Development Projects](https://www.amazon.com/Adapter-Serial-Converter-Development-Projects/dp/B075N82CDL)
- One or more [Pixelblaze Output Expander](https://www.bhencke.com/serial-led-driver) boards, either regular or Pro. 

#### What does it NOT do?
You WILL have to write code to use ExpanderVerse. It's a toolkit for artists and creators with intermediate to 
advanced hardware and software experience.  Processing is great place to learn graphics programming, and quite a 
few examples are provided, but there isn't a library of read-to-run patterns.  What gets displayed on your ten thousand
LED sculpture is mostly up to you.

You should also be familiar with the LED hardware you'll be using, how to wire it and importantly, how to power it properly.  







