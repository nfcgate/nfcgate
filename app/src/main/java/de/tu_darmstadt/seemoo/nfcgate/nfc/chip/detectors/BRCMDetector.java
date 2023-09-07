package de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors;

import android.util.Pair;

import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.NfcChipGuess;

/**
 * Broadcom NFC chip name detector.
 * Uses the only known configuration filename.
 * Broadcom configurations have not been observed to contain the chip name directly,
 * so fall back to the transport driver.
 */
public class BRCMDetector extends BaseConfigLineDetector {
    @Override
    protected List<String> getConfigFilenames() {
        return Arrays.asList("libnfc-brcm.conf");
    }

    @Override
    protected boolean onLine(String line, NfcChipGuess guess) {
        Pair<String, String> keyVal = splitConfigLine(line);

        if (keyVal != null) {
            if ("TRANSPORT_DRIVER".equals(keyVal.first)) {
                // the existence of this device node confirms this is (or is not) the correct config
                if (!fileExists(keyVal.second))
                    return false;

                guess.confidence = 0.9f;
                if (guess.chipName == null)
                    guess.chipName = "Broadcom Device " + formatBRCMDeviceNode(keyVal.second);
            }
        }

        return true;
    }

    private static String formatBRCMDeviceNode(String devNode) {
        return devNode.replace("/dev/", "");
    }
}
