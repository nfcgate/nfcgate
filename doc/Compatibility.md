# Compatibility
This document states the compatibility of NFCGate's modes with different chipsets, devices, and ROM versions.

## General
NFCGate's patch to the Android NFC service only works with devices that use the NFC NCI specification. In our testings, Broadcom or NXP NFC chipsets use this specification.

### Determining the Chipset
On the device, one can find the NFC chipset on the NFCGate status page.

### Desfire Workaround
In previous versions of this application, NFCGate included a workaround for an Android NFC bug, that made it impossible to use with MiFare DESFire cards. This workaround is no longer part of the application due to complete code overhaul. We have not experienced the bug again. If you encounter that issue, please open an issue so the workaround can be included in the new version as well.

## Matrix
| Device                   | NFC chipset | Android ROM version    | Clone              | On-device capture  | Relay              | Replay             | Notes              |
| :----------------------- | :---------- | :--------------------- | :----------------: | :----------------: | :----------------: | :----------------: | :----------------- |
| Google Nexus 5X          | NXP (PN548AD)     | Stock 7.1.2 (N2G48C)   | y                  | y                  | y                  | y                  |                    |
| Google Nexus 5X          | NXP (PN548AD)     | Stock 8.0.0 (OPR4.170623.020)         | y                  | y                  | y                  | y                  |                    |
