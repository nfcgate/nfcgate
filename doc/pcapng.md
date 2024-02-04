# pcapng
## Overview
This document does not describe the pcapng file format, it only describes how it is used within NFCGate.
For information about pcapng see the [official format documentation](https://github.com/pcapng/pcapng).

## Structure
NFCGate uses two interface description blocks to dump NFC traffic:

1. ISO 14443 data encoded as link-type ISO 14443 (interface 0)
2. Initial card data (NCI configuration stream) encoded as DLT_USER_0 (interface 1)

Wireshark contains a dissector for ISO 14443 data, allowing you to view and examine NFCGate dumps.
Newer versions of Wireshark (v2.9.0+) are also able to dissect ISO 7816 as a sub-dissector of ISO 14443.
At this time, no dissector for NCI configuration streams exists.
