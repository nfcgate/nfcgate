package de.tu_darmstadt.seemoo.nfcgate.util;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NfcConf {
    private static class ParserStatus {
        public String chipName = null;
        public boolean confirmed = false;

        @Override
        public String toString() {
            return "chipName: " + chipName + ", confirmed: " + confirmed;
        }
    }
    private static abstract class ILineParser {
        ParserStatus status = new ParserStatus();
        public ParserStatus getResult() {
            return status;
        }

        public abstract boolean onLine(String line);
    }

    private final ILineParser NXP_PARSER = new ILineParser() {
        @Override
        public boolean onLine(String line) {
            Pair<String, String> keyVal = splitConfigLine(line);

            if (keyVal != null) {
                // existence of this device node confirms this is (or is not) the correct config
                if ("NXP_NFC_DEV_NODE".equals(keyVal.first)) {
                    if (!(status.confirmed = fileExists(keyVal.second)))
                        return false;

                    if (status.chipName == null)
                        status.chipName = "NXP " + keyVal.second.replace("/dev/", "");
                }

                if ("NXP_NFC_CHIP".equals(keyVal.first))
                    status.chipName = "NXP " + resolveNXPChipCode(keyVal.second);
            }

            return true;
        }
    };
    private final ILineParser BRCM_PARSER = new ILineParser() {
        @Override
        public boolean onLine(String line) {
            Pair<String, String> keyVal = splitConfigLine(line);

            if (keyVal != null) {
                // existence of this device node confirms this is (or is not) the correct config
                if ("TRANSPORT_DRIVER".equals(keyVal.first)) {
                    if (!(status.confirmed = fileExists(keyVal.second)))
                        return false;

                    if (status.chipName == null)
                        status.chipName = "BRCM " + keyVal.second.replace("/dev/", "");
                }
            }

            return true;
        }
    };

    // NXP chip codes as of 2018-10-29
    private final static Map<String, String> NXPMap = new HashMap<String, String>() {{
        put("0x01","PN547C2");
        put("0x02","PN65T");
        put("0x03","PN548AD");
        put("0x04","PN66T");
        put("0x05","PN551");
        put("0x06","PN67T");
        put("0x07","PN553");
        put("0x08","PN80T");
    }};
    private static String resolveNXPChipCode(String code) {
        return NXPMap.containsKey(code) ? NXPMap.get(code) : "Unknown";
    }

    private final String[] configDirs = new String[] {
            "/odm/etc/",
            "/vendor/etc/",
            "/etc/",
    };
    private final String[] configNamesBRCM = new String[] {
            "libnfc-brcm.conf",
    };
    private final String[] configNamesNXP = new String[] {
            "libnfc-nxp.conf",
    };

    // search standard config dirs and return all paths resulting from dir+fileName which exist
    private List<Pair<String, ILineParser>> findConfigs(String[] fileNames, ILineParser parser) {
        List<Pair<String, ILineParser>> result = new ArrayList<>();

        for (String dir : configDirs) {
            for (String fileName : fileNames) {
                String path = dir + fileName;

                if (fileExists(path))
                    result.add(new Pair<>(path, parser));
            }
        }

        return result;
    }

    // get a list of existing config file paths
    private List<Pair<String, ILineParser>> getConfigPaths() {
        // build list of existing config files to parse in reverse order of precedence
        List<Pair<String, ILineParser>> result = new ArrayList<>();

        // try to find old broadcom configs first
        result.addAll(findConfigs(configNamesBRCM, BRCM_PARSER));
        // try to find old NXP configs next
        result.addAll(findConfigs(configNamesNXP, NXP_PARSER));

        // if this prop exists, try to find configs with SKU names
        String propHWSKU = getSystemProp("ro.boot.product.hardware.sku");
        if (!propHWSKU.isEmpty())
            result.addAll(findConfigs(new String[] {
                    "libnfc-" + propHWSKU + ".conf",
                    "libnfc-nxp-" + propHWSKU + ".conf",
            }, NXP_PARSER));

        // if this prop exists, try to find configs with its name
        String propCFN = getSystemProp("persist.vendor.nfc.config_file_name");
        if (!propCFN.isEmpty())
            result.addAll(findConfigs(new String[]{ propCFN }, NXP_PARSER));

        return result;
    }

    /**
     * Detects the NFCC on this device
     *
     * @return The name of the chip or null
     */
    public String detectNFCC() {
        String chipName = null;

        // search configuration files in order
        for (Pair<String, ILineParser> entry : getConfigPaths()) {
            ParserStatus result = readConf(entry.first, entry.second);

            // ignore confirmed misses
            if (result != null) {
                Log.d("NFCCONFIG", "Guess {" + result + "} from " + entry.first);

                // save guess
                if (result.chipName != null)
                    chipName = result.chipName;

                // confirmed guess ends the search
                if (result.confirmed)
                    break;
            }
        }

        return chipName;
    }

    /**
     * Reads given config with the specified parser
     *
     * @return Either a confirmed chipName, a unconfirmed chipName guess or null
     */
    private ParserStatus readConf(String path, ILineParser parser) {
        File file = new File(path);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line; (line = br.readLine()) != null; )
                    if (!parser.onLine(line.trim()))
                        return null;
            }
            catch (IOException ignored) { }
        }

        return parser.getResult();
    }

    /**
     * Splits config line, e.g. "a=b", "a =\"b\"", " a =  b  "
     * into (a, b) pair
     *
     * @return Pair of key, value or null if line is not in the expected format
     */
    private static Pair<String, String> splitConfigLine(String line) {
        String[] parts = line.split("=", 2);

        if (parts.length == 2)
            return new Pair<>(parts[0].trim(), parts[1].trim().replaceAll("(^\")|(\"$)", ""));

        return null;
    }

    /**
     * Checks if file exists
     *
     * @param fileName Path to file to check
     * @return True if exists
     */
    private static boolean fileExists(String fileName) {
        return new File(fileName).exists();
    }

    /**
     * Gets a system prop
     *
     * @param prop Full name of property to get
     * @return Value of property on success or empty String otherwise
     */
    private String getSystemProp(String prop) {
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
