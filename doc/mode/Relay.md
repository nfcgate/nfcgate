Relay Mode
=======

Relay mode enables the user to relay NFC traffic over the network. All traffic on the ISO 14443 layer
can be relayed, along with initial tag information. Please see [clone mode](/doc/mode/Clone.md) for more information.

## Requirements

### Devices
- Two Android devices that will perform the relaying (called *relay devices*). A device cannot be used as both the reader and the tag (HCE) when participating in relaying.
- Reader and tag whose traffic should be relayed (called *relayed devices*). These devices can be Android devices.
- Any device running the [NFCGate server](https://github.com/nfcgate/server/) application. This can also be one of the Android devices running Termux.

### Configuration
Relay devices:

- Android 4.4+ (API level 19+)
- [EdXposed](https://github.com/ElderDrivers/EdXposed) or [Xposed](https://repo.xposed.info/)
- Architecture: ARMv8-A, ARMv7
- The device acting as tag (in "tag mode") requires [HCE support](https://developer.android.com/guide/topics/connectivity/nfc/hce).
- Networking connectivity with the server application, more specifically a TCP connection is required.

Relayed devices:

- No special configuration required.

## Usage
On both relay devices:

1. Switch to `Settings` in the navigation drawer.
2. Specify `Hostname` and `Port` and `Session`.
3. Make sure the server application is running and accessible over the network.
4. Switch to `Relay Mode` in the navigation drawer.
5. Click `Reader` or `Tag` depending on device: For a relay, one device in "reader mode" and one device in "tag mode" is required.
6. When the connection has been successfully established, the app will show a green status indicator.

Relaying traffic:
1. Place the relay device in "reader mode" to read the tag that you want to relay.
2. Place the second relay device in "tag mode" on the relayed reader.

The NFC traffic is captured and available in `Logging` for later use.

## Technical Information
See [clone mode documentation](/doc/mode/Clone.md).

### Architecture
![Relay mode architecture](/doc/media/architecture_connect.png)

## Caveats
### Timing
If the relayed reader and/or tag have constrained requirements for communication time, timeouts can occur.
In addition to that, poor network connectivity might also negatively affect performance and reliability.
