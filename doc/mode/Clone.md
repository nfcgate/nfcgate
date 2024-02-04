Clone Mode
=======

Clone mode enables the user to clone initial information of a tag, e.g. tag ID. Every functionality of a tag that uses an advanced protocol, such as ISO 7816, will **NOT** be cloned.

## Requirements
- Android 4.4+ (API level 19+)
- [EdXposed](https://github.com/ElderDrivers/EdXposed) or [Xposed](https://repo.xposed.info/)
- Architecture: ARMv8-A, ARMv7
- [HCE](https://developer.android.com/guide/topics/connectivity/nfc/hce): Relay tag mode, replay tag mode, clone mode.

## Usage
1. Switch to `Clone Mode` in the navigation drawer.
2. Scan a tag.
3. The phone will clone the tag information.
4. When read by another reader, the phone will now respond with the cloned tag information.

Tag information can also be saved for later use.

## Technical Information
Modifying the tag information that is sent by the phone requires patching the Android NCI stack.
Because of this, only devices using the NCI stack are supported.
In addition to that, the patch modifies the NCI system library in-memory by hooking library symbols. Therefore it does **NOT** modify anything under `/system`.
Due to the NCI system library being native code, the patch is architecture specific and is currently only implemented for ARMv8-A and ARMv7 (ARM and Thumb mode).
