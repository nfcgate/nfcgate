On-Device Capture Mode
=======

On-device capture mode enables the user to capture NFC traffic of third-party applications running on the device.
All ISO 14443 layer traffic between the application and the tag, or in case of HCE, the reader, is captured.

## Requirements
- Android 4.4+ (API level 19+)
- [EdXposed](https://github.com/ElderDrivers/EdXposed) or [Xposed](https://repo.xposed.info/)
- 3rd party app must use [reader mode](https://developer.android.com/reference/android/nfc/NfcAdapter#enableReaderMode%28android.app.Activity,%20android.nfc.NfcAdapter.ReaderCallback,%20int,%20android.os.Bundle%29)

## Usage
1. Switch to `Capture Mode` in the navigation drawer.
2. Start recording by pressing `Begin Capture`.
3. Switch to 3rd party app, that should be captured.
4. Use NFC functions of 3rd party app.
5. Switch back to NFCGate.
6. Stop recording by pressing `Stop Capture`.

Captured NFC traffic is available in `Logging`. From there, it can be exported as a pcapng file.

## Technical Information
Capturing NFC traffic requires hooking Java methods in Android's NFC service.
As framework code might change between Android versions, on-device capture might break, depending on the
Android version! Please see [compatibility document](/doc/Compatibility.md) for detailed information.
On-device capture hooks the following functions within the Android NFC service (`com.android.nfc`):

* `public TransceiveResult com.android.nfc.NfcService.TagService#transceive(int nativeHandle, byte[] data, boolean raw)`
* `public int com.android.nfc.NfcDispatcher#dispatchTag(Tag tag)`
* `public void com.android.nfc.cardemulation.HostEmulationManager#onHostEmulationData(byte[] data)`
* `public void com.android.nfc.cardemulation.HostEmulationManager#onHostEmulationActivated()`
* `public boolean com.android.nfc.NfcService#sendData(byte[] data)`
