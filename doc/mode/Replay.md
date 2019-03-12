Replay Mode
=======

Replay mode enables the user to replay previously captured NFC traffic. Every traffic available in `Logging` can be replayed, both the reader and the tag traffic.

## Requirements
- Android 4.4+ (API level 19+)
- [Xposed](https://repo.xposed.info/)
- Architecture: ARMv8-A, ARMv7
- When replaying tag traffic, the device requires [HCE support](https://developer.android.com/guide/topics/connectivity/nfc/hce).
- Logs in `Logging`

## Usage
1. Switch to `Replay` in the navigation drawer.
2. Select the logged session to be replayed.
3. Click `Reader` or `Tag` to replay the respective traffic.

The new NFC traffic will also be recorded and available in `Logging` for later use.

## Technical Information
See [clone mode documentation](/doc/mode/Clone.md).

### Traffic Selection
Replay mode selects the next entry in traffic log in order by type. It does not perform any advanced matching.
