 
###
Our patch to the android NFC Daemon only works with devices using either a Broadcom or NXP NFC chip. If you are unsure what Chip your device is using, take a look at `/dev`: Broadcom devices are usually start with `bcm`, while the most common NXP chip is `pn544`. Our code has been successfully tested on the Nexus 4, 5, and 5X, and **should** work on other devices using the same libnfc and a compatible NFC chip. On incompatible devices, the application may still start, but it will be unable to proxy commands from the NFC reader. This is due to limitations in the Android API.

