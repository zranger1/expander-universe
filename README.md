## ExpanderVerse ##

** WORK IN PROGRESS - COMING SOON! **

** watch this space for more information as it develops **

ExpanderVerse lets you drive addressable LEDs with your computer, a USB->Serial adapter, and a [Pixelblaze Output Expander](https://www.bhencke.com/serial-led-driver) board.  Potentially *many* LEDs. 

#### Features
- Supports multiple serial ports (as many as your computer can handle) and multiple output expander boards (up to 8) per port.
- Each expander board channel can drive up to 800 RGB WS2812s, or 600 RGBW LEDs or APA102s.
- Mix and match LED types, then adjust them to match brightness - software allows both global and per channel brightness control and optional gamma correction (cubic curve).

- Supports Pixelblaze-style 2D and 3D coordinate mapping, and can import JSON pixelblaze maps.
- Render in [Processing](www.processing.org), then send to your LEDs!  Supports "index swizzling" to allow easy, one step transfer of 2D image sections from Processing's PImage and PGraphics objects. 

#### Why use this and not Pixelblaze?   
I'm a big fan of Pixelblaze. I use it for almost everything! It's generally the fastest way to get to the fun, creative part of an LED project. 

However there are some occasions when a little more computing power -- say, a not-so-tiny 5Ghz, 12 core controller with a GPU, gigabytes of RAM, terabytes of storage, etc. -- comes in handy. If:

- You're familiar with [Processing](www.processing.org), or willing to dive in and learn.
- You want to smoothly animate an installation with several thousand LEDs
- You want a hardwired interface rather than depending on WiFi
- You want a central point of control rather than distributing several microcontrollers
- You need to control or sync with other devices or media which run on, or communicate via
computer.

ExpanderVerse and a whole bunch of Output Expander boards might be exactly what you need.



#### Required Hardware
- A computer that will run [Processing](www.processing.org), with one or more USB ports (USB 2 minimum)
- A USB->Serial adapter that will run at 2,000,000 baud.  Not all of them will work, particularly those that use "clone" chipsets.  Look for one with a genuine FTDI chip.  Here's an Amazon link to one that's known to work: [USB to TTL Adapter, USB to Serial Converter for Development Projects](https://www.amazon.com/Adapter-Serial-Converter-Development-Projects/dp/B075N82CDL)
- One or more [Pixelblaze Output Expander](https://www.bhencke.com/serial-led-driver) boards, either regular or Pro. 

#### What does it NOT do?
- It is not turnkey solution. More a DIY thing for artists and creators with hardware and software experience. 
- It assumes that you are familiar with the LED hardware you'll be using, how to wire it, and importantly, how to power it properly.
- Same idea on the software side. Some coding is necessary. Although Processing is a great place to learn graphics programming, and examples are provided, there isn't a huge library of ready-to-run patterns. What gets displayed on all those LEDs is mostly up to you. 







