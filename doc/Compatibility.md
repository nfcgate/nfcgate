# Compatibility
This document states the compatibility of NFCGate's modes with different chipsets, devices, and ROM versions.

## General
NFCGate's patch to the Android NFC service only works with devices that use the NFC NCI specification. In our testings, Broadcom or NXP NFC chipsets use this specification.

### Determining the Chipset
On the device, one can find the NFC chipset on the NFCGate status page.

### Desfire Workaround
In previous versions of this application, NFCGate included a workaround for an Android NFC bug, that made it impossible to use with MiFare DESFire cards. This workaround is no longer part of the application due to complete code overhaul. We have not experienced the bug again. If you encounter that issue, please open an issue so the workaround can be included in the new version as well.

### OnePlus Devices
There are unidentified issues with applying NFC config streams. It looks like specific config stream values such as the NFCID are ignored. The NFC stack does not report errors. This leads to NFCGate not being able to emulate static tag information (i.e. NFCID), however, relay and replay functionality are still possible.

## Matrix
| Device                     | NFC chipset       | Android version | Android ROM version                        | Clone | On-device capture | Relay | Replay | Notes                                                 |
|:---------------------------|:------------------|-----------------|:-------------------------------------------|:-----:|:-----------------:|:-----:|:------:|:------------------------------------------------------|
| Google Pixel 6a            | ST54J             | 13              | Stock 13.0.0 (TQ3A.230605.010)             |   y   |         y         |   y   |   y    |                                                       |
| Google Nexus 5X (bullhead) | NXP (PN548AD)     | 11              | LineageOS 18.1 (RQ3A.211001.001)           |   y   |         y         |   y   |   y    |                                                       |
|                            |                   | 10              | PixelExperience 10 (20200511-1150)         |   y   |         y         |   y   |   y    |                                                       |
|                            |                   | 9               | PixelExperience 9 (20191212-0124)          |   y   |         y         |   y   |   y    |                                                       |
|                            |                   | 8.1             | Stock 8.1.0 (OPM7.181205.001)              |   y   |         y         |   y   |   y    |                                                       |
|                            |                   | 8.0             | Stock 8.0.0 (OPR4.170623.020)              |   y   |         y         |   y   |   y    |                                                       |
|                            |                   | 7.1             | Stock 7.1.2 (N2G48C)                       |   y   |         y         |   y   |   y    |                                                       |
|                            |                   | 6               | Stock 6.0.1 (MTC20F)                       |   y   |         y         |   y   |   y    | No ISO-DEP historical bytes (HIST) due to ROM issue.  |
| Redmi Note 9 Pro           | NXP (PN553)       | 11              | Stock 11.0.0 (RKQ1.200826.002)             |   y   |         y         |   y   |   y    |                                                       |
| Samsung Galaxy S8+         | Samsung (S3NRN82) | 10              | OneUI 2.5 mod (hadesRom_Q_OneUI_v3.0-FUK2) |   y   |         y         |   y   |   y    |                                                       |
| Xiaomi MI 6 (sagit)        | NXP (PN80T)       | 9               | PixelExperience 9 (20180912-0720)          |   y   |         y         |   y   |   y    |                                                       |
|                            |                   | 9               | Stock MIUI 11.0.5 (PKQ1.190118.001)        |   y   |         y         |   y   |   y    |                                                       |
| Any OnePlus                | -                 | -               | -                                          |   n   |         ?         |   ?   |   ?    | Unidentified issues with applying NFC config streams. |
