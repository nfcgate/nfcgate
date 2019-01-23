NFCGate
=======

NFCGate is an Android application meant to capture, analyze, or modify NFC traffic. It can be used to reverse engineer protocols or assess the security of protocols against traffic modifications.

## Notice
This application was developed for security research purposes by students of the [Secure Mobile Networking Lab](https://www.seemoo.tu-darmstadt.de/) at [TU Darmstadt](https://www.tu-darmstadt.de/). Please do not use this application for malicious purposes.

## Features
- **On-device capture**: Captures NFC traffic sent and received by other applications running on the device.
- **Relay**: Relays NFC traffic between two devices. One device operates as a "reader" reading an NFC tag, the other device emulates an NFC tag using the Host Card Emulation (HCE).
- **Replay**: Replays previously captured NFC traffic in either "reader" or "tag" mode.
- **Clone**: Clones the initial tag information (e.g. ID).
- PCAPNG export of captured NFC traffic, readable by Wireshark.

## Requirements for specific modes
- NFC support
- Android 4.4+ (API level 19+)
- [Xposed](https://repo.xposed.info/): On-device capture, relay tag mode, replay tag mode, clone mode.
- ARMv8-A, ARMv7: Relay tag mode, replay tag mode, clone mode.
- [HCE](https://developer.android.com/guide/topics/connectivity/nfc/hce): Relay tag mode, replay tag mode, clone mode.



### Android version compatibility
See the [compatibility matrix](docs/Compatibility.md).  

## Known Issues
??

### 
- The HCE phone needs to use `libnfc-nci` (compatible phones include the Nexus 4 and 5, more on that below).

### 
### 

## Usage
The usage of this application is a bit fiddly right now, so follow these instructions exactly in order to make sure everything works.

After having installed and activated Xposed on at least one of the devices, install the application. Xposed will prompt you to reboot your phone, which you should do. Only the HCE device requires Xposed, the other one can work without it.

Afterwards, launch the app on both phones. Enter the IP and Port of the Server (default Port is 5566, feel free to set default values in the settings to avoid having to type them in each time) and hit "Create session" on one of the devices. The device will connect and display a session token with 6 digits. Hit "Join session" on the other device and enter that code. You should now be connected.

Now take the non-HCE-Phone and hold it to the card you want to read. The phone will detect the tag and read out some information, which will be sent to the HCE-Phone. There, it will be used to initialize and enable our patch to the Android NFC Daemon. Keep the phone attached to the card.

Now (*and only now*), you can hold the HCE-Phone to the reader. **The first read may fail** if you are using a Device with a Broadcom BCM20793 chip like the Nexus 4. If this happens, just try again by removing the device from the reader and re-attaching it. So far, we have only observed this behaviour with the Nexus 4, other devices like the Nexus 5 seem to not be affected.

If you have enabled it, the non-HCE-phone will display the raw bytes it received from the card in the debug window. In the future, we will add an option to dump them to a file for later analysis. The server will also display the bytes.

Once you are done, you can remove the card and reader, and disconnect from the server using the disconnect button.

## Caveats
There are some caveats, so read this section carefully before using the application (and especially before filing a bug report).

### Native code patch compatibility
Our patch to the android NFC Daemon only works with devices using either a Broadcom or NXP NFC chip. If you are unsure what Chip your device is using, take a look at `/dev`: Broadcom devices are usually start with `bcm`, while the most common NXP chip is `pn544`. Our code has been successfully tested on the Nexus 4, 5, and 5X, and **should** work on other devices using the same libnfc and a compatible NFC chip. On incompatible devices, the application may still start, but it will be unable to proxy commands from the NFC reader. This is due to limitations in the Android API.

The patch is also only compatible with devices that can run Xposed. For Android 4.4.X, this requires a rooted device. Android 5+ devices do not need root, but the installation procedure is more complex and requires flashing the device.

NFCGate supports ARM, THUMB, and ARM64 ABIs only.


### DESFire workaround
The Android NFC Libraries contain a bug which makes it impossible to use our application with MiFare DESFire cards (a common NFC card for payment systems). We are using a workaround to enable us to still read these cards, but that workaround has some side effects. When you start the application, you will get a warning. Please read the information carefully.

### Compatibility with cards
Android no longer offers support for MiFare classic chips on many devices. In general, we can only proxy tags supported by Android. When in doubt, use an application like NFC Tag info to find out if your tag is compatible. We have done extensive testing with MiFare DESFire cards (NFC-A) using a Nexus S, 4, 5, and 5X as reader, and a Nexus 4, 5, or 5X as HCE phone. All other combinations are untested (feedback is welcome).

Also, at the moment, every tag technology supported by Android's HCE is supported (A, B, F), however NFC-B and NFC-F remain untested. NFC-A tags are the most common tags (for example, both the MiFare DESFire and specialized chips like the ones in electronic passports use NFC-A), but you may experience problems if you use other tags.

### Compatibility with readers
This application only works with readers which do not implement additional security measures. One security measure which will prevent our application from working is when the reader checks the time it takes the card to respond (or, to use the more general case, if the reader implements "distance bounding"). The network transmission adds a noticeable delay to any transaction, so any secure reader will not accept our proxied replies. However, if the reader does not implement any additional checks, it *should*be possible to proxy it.

### Android NFC limitations
Some features of NFC are not supported by Android and thus cannot be used with our application. These features include *extended length APDUs*. We have also experienced cases where the NFC field generated by the phone was not strong enough to properly power more advanced features of some NFC chips (e.g. cryptographic operations). Keep this in mind if you are testing chips we have not experimented with.

### Confidentiality of data channel
Right now, all data in relay mode is sent *unencrypted* over the network. We may or may not get around to implementing cryptographic protection, but for now, consider everything you send over the network to be readable by anyone interested, unless you use extra protection like VPNs. Keep that in mind while performing your own tests.



### 
### 


## Publications and Media
This application was presented at WiSec 2015. The [extended Abstract](https://blog.velcommuta.de/wp-content/uploads/2015/07/nfcgate-extended-abstract.pdf) (outdated by now) and [poster](https://blog.velcommuta.de/wp-content/uploads/2015/07/NFCGate-Poster.pdf) (slightly outdated) can be found on the [website](https://blog.velcommuta.de/publications/) of one of the authors. It was also presented in a brief [Lightning Talk](https://media.ccc.de/browse/conferences/camp2015/camp2015-6862-lightning_talks_day_2.html#video&t=300) at the [Chaos Communication Camp 2015](https://events.ccc.de/camp/2015/wiki/Main_Page).

## Used Libraries
- [Xposed Bridge](https://github.com/rovo89/XposedBridge) (Licensed under the [Apache License v2.0](http://opensource.org/licenses/Apache-2.0))
- [LibNFC-NCI](https://android.googlesource.com/platform/external/libnfc-nci/) (Licensed under the [Apache License v2.0](http://opensource.org/licenses/Apache-2.0))
- [Protobuf](https://github.com/protocolbuffers/protobuf) (Licensed under the modified [BSD 3-Clause License](http://opensource.org/licenses/BSD-3-Clause))
- [Android About Page](https://github.com/medyo/android-about-page) (Licensed under the [MIT License](https://opensource.org/licenses/MIT))
- [Android Device Names](https://github.com/jaredrummler/AndroidDeviceNames) (Licensed under the [Apache License v2.0](http://opensource.org/licenses/Apache-2.0))
- [Android Support library - preference v7 bugfix](https://github.com/Gericop/Android-Support-Preference-V7-Fix) (Released into the public domain and partly licensed under the [Apache License v2.0](http://opensource.org/licenses/Apache-2.0))
- [Android Room](https://developer.android.com/topic/libraries/architecture/room) (Licensed under the [Apache License v2.0](http://opensource.org/licenses/Apache-2.0))
- [Android Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) (Licensed under the [Apache License v2.0](http://opensource.org/licenses/Apache-2.0))

## Credits
- [ADBI](https://github.com/crmulliner/adbi): ARM and THUMB inline hooking
