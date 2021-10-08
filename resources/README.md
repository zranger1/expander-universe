## ExpanderVerse ##

ExpanderVerse lets you drive addressable LEDs with your computer, a USB->Serial adapter, and a [Pixelblaze Output Expander](https://www.bhencke.com/serial-led-driver) board.  Potentially *many* LEDs. 

(Beta 0.3.0) NOTE: Things will be changing frequently 'till the 1.0 release! See the [additional notes](./NOTES.md) for information on what's new in the current release.

#### Introduction
I'm a big fan of the Pixelblaze. It's the fastest way to get to the fun, creative part of an LED project. However, I've been working on a large scale, stationary art project, in a situation where reliable wifi was not going to be possible.  I needed wired control over thousands of LEDs at a reasonably high frame rate, with very high reliability.  The Pixelblaze Output Expander board provided a great way to drive all those LEDs, but for the multimedia, interactive bits, I was going to need a heftier controller.

So, instead of looking for another microcontroller, I took the USB->Serial interface code from PixelTeleporter and wrote ExpanderVerse. It lets you put a PC or Mac to work as an ludicrously overpowered LED controller. I've found it useful, and hope that you will too.  If...

- You're comfortable with power and data wiring for LEDs.
- You're familiar with [Processing](www.processing.org), or willing to dive in and learn.
- You have plenty of A/C power at your site, or at least very long extension cords
- You want to smoothly animate an installation with several thousand LEDs.
- You want the reliability and security of a hardwired interface.
- You'd like a centralized point of control and synchronization for your project.
- You need to interact with or sync to the internet or other media, or to use the vast array of interface devices
which communicate via PC.

ExpanderVerse and a whole bunch of Pixelblaze Output Expander boards might be exactly what you need.

#### Features
- Supports multiple serial ports (as many as your computer can handle) and multiple output expander boards (up to 8) per port.
Serial I/O is multithreaded to maximize bandwidth for large installations.
- Each expander board supports 8 output channels, each channel can drive up to 800 RGB WS2812s, or 600 RGBW LEDs or APA102s. 
- Supports extended dynamic range on APA102 and similar LEDs, as well as RGBW LEDs.
- Mix and match LED types, then adjust them to match brightness and white balance. ExpanderVerse supports brightness and gamma correction control from the global level down to the individual channel, and (optional, and at this point experimental) per-channel color balance adjustment.
- Supports Pixelblaze-style 2D and 3D coordinate mapping, and can import JSON Pixelblaze maps.  Retains both world and normalized maps for use in your patterns.
- Render in [Processing](www.processing.org), then send to your LEDs!  Supports "index mapping" to allow easy, one step transfer of 2D image sections from Processing's PImage and PGraphics objects. 
- Supports use of the GPU (via OpenGL fragment shaders) for massively parallel rendering of LED data. You can write LED shaders! A few tens of thousands of LED pixels are easy work for a graphics card used to pumping out 4k video at 60fps or better.

#### Required Hardware
- A computer that will run [Processing](www.processing.org) at whatever speed you require, equipped with one or more USB ports (USB 2.x minimum)
- A USB->Serial adapter that will run at 2,000,000 baud.  Not all of them will work, particularly those that use "clone" chipsets.  Look for one with a genuine FTDI chip.  Here's an Amazon link to one that's known to work: [USB to TTL Adapter, USB to Serial Converter for Development Projects](https://www.amazon.com/Adapter-Serial-Converter-Development-Projects/dp/B075N82CDL)
- One or more [Pixelblaze Output Expander](https://www.bhencke.com/serial-led-driver) boards, either regular or Pro. 

It doesn't take a killer gaming machine to run ExpanderVerse. My main dev machine died early in the project, and while waiting for a replacement, I built and tested all the demos on my backup system, which was awesome in 2012 when it was built, but is somewhat below average now. The larger Raspberry Pi models can probably do the job.

## Installing the Library into Processing
To install on Processing 3, you will need to download ExpanderVerse.zip and manually copy it to the ```libraries``` folder of your Processing sketchbook.  (Processing 4 has an easier procedure. I just need to finish testing with it. Meanwhile,
these instructions will still work.)

To find the Processing sketchbook on your computer, open the Preferences window from the Processing application (PDE) and look 
for the "Sketchbook location" item at the top.

By default the following locations are used for your sketchbook folder: 
  * For Mac users, the sketchbook folder is located in `~/Documents/Processing` 
  * For Windows users, the sketchbook folder is located in `My Documents/Processing`

Download the zip file for latest release from the repository.

Unzip and copy the ExpanderVerse folder into the `libraries` folder in the Processing sketchbook.
If `libraries` does not exist, (unlikely, but possible) you will need to create it.

The folder structure should look like this when you're done:

```
Processing
  libraries
    ExpanderVerse
      examples
      library
      reference
      src
```
             
After restarting Processing, check the File/Examples... menu.   ExpanderVerse and all its examples should be there,
in the "Contributed Libraries" section.

#### Documentation
Documentation is a work in progress.  More is always on the way!
- Javadocs are [here.](https://zranger1.github.io/expander-universe)
- [Additional Notes](./NOTES.md)

#### Warnings, Disclaimers, Potential Pitfalls
ExpanderVerse is a toolkit for artists and creators with intermediate to advanced hardware and software experience. You **WILL** have to write code to use ExpanderVerse. 

Processing is great place to learn graphics programming, and quite a few examples are provided, but there isn't a library of ready-to-run patterns.  What gets displayed on your ten thousand LED sculpture is mostly up to you.

You should also be familiar with the LED hardware you'll be using, how to wire it and importantly, how to power it properly. 
Large numbers of LEDs require large amounts of power, which can definitely be dangerous, both to the equipment and to you. It pays to be very careful, and to check multiple times that voltage and polarity are correct before hooking things up.







