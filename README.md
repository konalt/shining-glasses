# N Glasses
## Partially reverse-engineered API for LED glasses
I got [these](https://www.amazon.ie/Luobannm-Luminous-Bluetooth-Programmable-Halloween/dp/B0FVLL9H5X) LED glasses from Amazon, but I was severely disappointed that the app was constantly recording the microphone, was proprietary, and needed file permissions, despite me never using these features. Through the magic of JADX and Android Studio I kind of reverse-engineered them.

I was surprised nobody on the internet had already attempted this (at least not with these specific glasses, and at least not that I've found) so I hope this repo can assist any others in their journey. [xkcd/979](https://xkcd.com/979/) comes to mind. If anybody comes across this and finds it useful, I'd appreciate a star or something. I don't know. I'm tired.

If anybody has further reverse-engineered the DIY image protocol, please enlighten me.

### Currently working:
- Brightness
- Clearing screen
- Selecting built-in animations
- Selecting built-in images
- Custom image implementation
- Eyes (multiple colors, blinking, blushing)
- Support for custom animations (currently only monochrome, and only Bad Apple)

### Probably not planned and also not working:
- Uploading DIY images (but my super cool custom image implementation is a way around this)
- Microphone support
- Converting images into glasses-ready ones on the fly (I don't speak Android Image Processing)

Some helper JS scripts (`convert_img.js`, `convert_img_palette.js` and `convert_animation.js`) are also provided for converting images and animations into strings for the app.

I think the helper scripts require `node-canvas`. And animations first need to be processed into 12-pixel-high frames by `ffmpeg`. I honestly don't remember. Make an issue if you need anything.

_Disclaimer: I am not an Android developer. This is my first, and currently only Android app. I have never seen a "bluetooth" before. This was made in like 5 days. The code is spaghetti-like, ugly and awful, but it works. It has also only currently been tested on a Samsung A54._

Most of the key protocol-related information can be found in these files:
- `RawGlassesDevice.java`
- `GlassesDevice.java`
- `Enigma.java`
- `UUIDS.java`
