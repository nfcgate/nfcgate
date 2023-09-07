package de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors;

import android.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.NfcChipGuess;

/**
 * ST NFC chip name detector.
 * Uses the ST HAL configuration file.
 * ST configurations have not been observed to contain the chip name directly,
 * so we use information from the firmware filename or fall back to the transport driver.
 */
public class STDetector extends BaseConfigLineDetector {
    @Override
    protected List<String> getConfigFilenames() {
        return Arrays.asList("libnfc-hal-st.conf");
    }

    @Override
    protected boolean onLine(String line, NfcChipGuess guess) {
        Pair<String, String> keyVal = splitConfigLine(line);

        if (keyVal != null) {
            if ("NCI_HAL_MODULE".equals(keyVal.first)) {
                String device = "/dev/" + keyVal.second.replace("nfc_nci.", "");
                // existence of this device node confirms this is (or is not) the correct config
                if (!fileExists(device))
                    return false;

                guess.confidence = 0.9f;
                if (guess.chipName == null)
                    guess.chipName = "ST Device " + formatSTDeviceNode(keyVal.second);
            }
            else if ("STNFC_FW_BIN_NAME".equals(keyVal.first)
                    || "STNFC_FW_CONF_NAME".equals(keyVal.first)) {
                guess.improveConfidence(0.2f);
                guess.chipName = "NXP Device " + formatFirmwareName(keyVal.second);
            }
        }

        return true;
    }

    private static String formatSTDeviceNode(String devNode) {
        return devNode;
    }

    private static String formatFirmwareName(String firmware) {
        Pattern pattern = Pattern.compile("^.*/(\\w+)_");
        Matcher matcher = pattern.matcher(firmware);

        return matcher.lookingAt() && matcher.groupCount() > 0 ?
                matcher.group(1).toUpperCase() : null;
    }
}
