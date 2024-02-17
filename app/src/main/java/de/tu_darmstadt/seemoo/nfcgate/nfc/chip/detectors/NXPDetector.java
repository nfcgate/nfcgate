package de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors;

import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.NfcChipGuess;

/**
 * NXP NFC chip name detector.
 * Uses several known configuration filenames, combined with some config filenames derived from
 * props or other system params. If a chip type is specified, uses a table to convert it to
 * the chip name, otherwise falls back to the device node.
 */
public class NXPDetector extends BaseConfigLineDetector {

    private final static Map<String, String> NXPChipMap = new HashMap<String, String>() {{
        // NXP chip codes as of 2023
        put("0x01","PN547C2");
        put("0x02","PN65T");
        put("0x03","PN548AD");
        put("0x04","PN66T");
        put("0x05","PN551");
        put("0x06","PN67T");
        put("0x07","PN553");
        put("0x08","PN80T");
        put("0x09","PN557");
        put("0x0A","PN81T");
        put("0x0B","SN1X0");
        put("0x0C","SN2X0");
    }};

    // Different versions of libnfc use different keywords
    private final static Set<String> NfcChipKeywords = new HashSet<String>() {{
        add("NXP_NFC_CHIP");
        add("NXP_NFC_CHIP_TYPE");
    }};

    @Override
    protected List<String> getConfigFilenames() {
        List<String> result = new ArrayList<>(Arrays.asList("libnfc-nxp.conf"));

        // check SKU property for any NXP configs (often set, file rarely exists)
        String propHWSKU = getSystemProp("ro.boot.product.hardware.sku");
        if (!propHWSKU.isEmpty())
            result.addAll(Arrays.asList(
                    "libnfc-" + propHWSKU + ".conf",
                    "libnfc-nxp-" + propHWSKU + ".conf"));

        // check direct config file property (rarely set)
        String propCFN = getSystemProp("persist.vendor.nfc.config_file_name");
        if (!propCFN.isEmpty())
            result.add(propCFN);

        return result;
    }

    @Override
    protected boolean onLine(String line, NfcChipGuess guess) {
        Pair<String, String> keyVal = splitConfigLine(line);

        if (keyVal != null) {
            if ("NXP_NFC_DEV_NODE".equals(keyVal.first)) {
                // existence of this device node confirms this is (or is not) the correct config
                if (!fileExists(keyVal.second))
                    return false;

                guess.improveConfidence(0.9f);
                if (guess.chipName == null)
                    guess.chipName = "NXP Device " + formatNXPDeviceNode(keyVal.second);
            }
            else if (NfcChipKeywords.contains(keyVal.first)) {
                guess.improveConfidence(0.2f);
                guess.chipName = "NXP " + resolveNXPChipCode(keyVal.second);
            }
        }

        return true;
    }

    private static String formatNXPDeviceNode(String devNode) {
        return devNode.replace("/dev/", "");
    }

    private static String resolveNXPChipCode(String code) {
        return NXPChipMap.containsKey(code) ? NXPChipMap.get(code) : "Unknown";
    }

    /**
     * Gets a system prop
     *
     * @param prop Full name of property to get
     * @return Value of property on success or empty String otherwise
     */
    protected static String getSystemProp(String prop) {
        String value = "";
        Process p = null;

        try {
            p = new ProcessBuilder("getprop", prop).redirectErrorStream(true).start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            for (String line; (line = br.readLine()) != null; )
                value = line.trim();
        }
        catch (IOException ignored) { }

        if (p != null)
            p.destroy();

        return !value.isEmpty() ? value : "";
    }
}
