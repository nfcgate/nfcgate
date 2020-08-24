Relay Mode
=======

Relay mode enables the user to relay NFC traffic over a network. Every traffic on ISO 14443 layer can be relayed in addition to initial tag information (see [clone mode](/doc/mode/Clone.md) for more information).

## Requirements

### Devices
- Two Android devices that peform the relay (called *relay devices*). A device cannot be used as reader nor as tag (HCE) when participating in relay.
- Reader and tag whose traffic should be relayed (called *relayed devices*). These devices can be Android devices.
- Any device running the [NFCGate server](https://github.com/nfcgate/server/) application. This can also be one of the Android devices running Termux.

### Configuration
Relay devices:

- Android 4.4+ (API level 19+)
- [EdXposed](https://github.com/ElderDrivers/EdXposed) or [Xposed](https://repo.xposed.info/)
- Architecture: ARMv8-A, ARMv7
- The device acting as tag (in "tag mode") requires [HCE support](https://developer.android.com/guide/topics/connectivity/nfc/hce).
- Sharing network with the server application, so that TCP communication is possible.

Relayed devices:

- No special configuration required.

## Usage
On both relay devices:

1. Switch to `Settings` in the navigation drawer.
2. Specify `Hostname` and `Port` and `Session`.
3. Make sure the server application is running and accessible over the network.
4. Switch to `Relay Mode` in the navigation drawer.
5. Click `Reader` or `Tag` depending on device: For a relay, one device in "reader mode" and one device in "tag mode" is needed.
6. When the connection has been sucessfully established, the app shows a green status indicator.

Relaying traffic:
1. Place the relay device in "reader mode" on the relayed tag.
2. Place the relay device in "tag mode" on the relayed reader.

The NFC traffic is recorded and available in `Logging` for later use.

## Technical Information
See [clone mode documentation](/doc/mode/Clone.md).

### Architecture
![Relay mode architecture](/doc/media/architecture_connect.png)

## Caveats
### Timing
If the relayed reader and/or tag constraint the communication time timeouts may occur. In addition, a sloppy network connection might also affect the performance and reliability.
