nfcgate
=======

NFCGate is an Android application meant to relay communication between an NFC reader and a card. That way, NFC cards can be read over longer distances (we successfully read a card in Hamburg with a reader in Darmstadt). This also allows eavesdropping on the communication between card and reader, in order to reverse engineer the protocol, for example.

## Notice
This application was developed for security research purposes by students of the [TU Darmstadt](https://www.tu-darmstadt.de/), [Secure Mobile Networking Lab](https://www.seemoo.tu-darmstadt.de/). Please do not use this application for malicious purposes.

## Requirements
- Two android phones with NFC Chips, running Android 4.4+ (API-Level 19+). Android 5 is untested so far, but *may* work.
- At least one of these devices needs to support HCE (Host Card Emulation). Most NFC-Enabled phones of the lats few years should support that.
- The HCE phone needs to use a Broadcom NFC chip (compatible phones include the Nexus 4 and 5).
- The HCE phone needs to be rooted and have [Cydia Substrate](http://www.cydiasubstrate.com/) installed and enabled (see [here](https://github.com/malexmave/nfcgate/wiki/Cydia-Substrate) for help on how to get that done)
- One server to proxy communication between the two phones (see [nfcgate-misc](https://github.com/malexmave/nfcgate-misc) repository for server code)

## Requirements for compilation
- Android Studio
- Android NDK (required to compile our native code patch)

## Usage
The usage of this application is a bit fiddly right now, so follow these instructions exactly in order to make sure everything works.

After having installed and activated Cydia Substrate on at least one of the devices, install the application. Cydia Substrate will prompt you to reboot your phone, which you should do. Only the HCE device requires Cydia Substrate, the other one can work without it.

Afterwards, launch the app on both phones. Enter the IP and Port of the Server (default Port is 5566, feel free to set default values in the settings to avoid having to type them in each time) and hit "Create session" on one of the devices. The device will connect and display a session token with 6 digits. Hit "Join session" on the other device and enter that code. You should now be connected.

Now take the non-HCE-Phone and hold it to the card you want to read. The phone will detect the tag and read out some information, which will be sent to the HCE-Phone. There, it will be used to initialize and enable our patch to the Android NFC Daemon. Keep the phone attached to the card.

Now (*and only now*), you can hold the HCE-Phone to the reader **The first read may fail.** We are not quite sure why that is, but in our tests, the second read was always successful if the reader was not hardened against this attack. If you have enabled it, the non-HCE-phone will display the raw bytes it received from the card in the debug window. In the future, we will add an option to dump them to a file for later analysis. The server will also display the bytes.

Once you are done, you can remove the card and reader, and disconnect from the server using the disconnect button.

## Caveats
There are some caveats, so read this section carefully before using the application (and especially before filing a bug report).

### Native code patch compatibility
Our patch to the android NFC Daemon only works with Broadcom chips (or, to be more exact, with devices using libnfc-nci, not libnfc-nxp). It has been successfully tested on the Nexus 4 and 5, and may or may not work on other devices using the same libnfc. On incompatible devices, the application may still start, but it will be unable to proxy commands from the NFC reader. This is due to limitations in the Android API.

### Broadcom BCM20793 (Nexus 4) workaround
The android drivers for the Broadcom BCM20793 chip (as used in the Nexus 4) contain a bug which makes it impossible to use our application with MiFare DESFire cards (a common NFC card for payment systems). We are using a workaround to enable us to still read these cards, but that workaround has some side effects. When you start the application, you will get a warning if you are using an affected device. Please read the information carefully.

### Compatibility with cards
Android no longer offers support for MiFare classic chips on many devices. In general, we can only proxy tags supported by android. When in doubt, use an application like NFC Tag info to find out if your tag is compatible. We have done extensive testing with MiFare DESFire cards using a Nexus S, Nexus 4 and Nexus 5 as reader, and a Nexus 4 or Nexus 5 as HCE phone. All other combinations are untested (feedback is welcome).

### Compatibility with readers
This application only works with readers which do not implement additional security measures. One security measure which will prevent our application from working is when the reader checks the time it takes the card to respond. The network transmission adds a noticeable delay to any transaction, so any secure reader will not accept our proxied replies. However, if the reader does not implement any additional checks, it *should*be possible to proxy it.

### Confidentiality of data channel
Right now, all data is sent unencrypted over the network. We may or may not get around to implementing cryptographic protection, but for now, consider everything you send over the network to be readable by anyone interested. Keep that in mind while performing your own tests. Many NFC protocols are resistant to replay attacks, but your mileage may vary.

## Used Libraries
This application uses the following external libraries:
- [Cydia Substrate](http://www.cydiasubstrate.com/) (Licensed under the [LGPLv3](https://www.gnu.org/licenses/lgpl.html))
- [LibNFC](https://android.googlesource.com/platform/external/libnfc-nci/) (Licensed under the [Apache License v2.0](http://opensource.org/licenses/Apache-2.0))
- [Protobuf](https://code.google.com/p/protobuf/) (Licensed under the [BSD 3-Clause license](http://opensource.org/licenses/BSD-3-Clause))