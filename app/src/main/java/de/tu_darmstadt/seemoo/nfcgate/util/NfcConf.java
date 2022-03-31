package de.tu_darmstadt.seemoo.nfcgate.util;

import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NfcConf {
    private static class ParserStatus {
        public String chipName = null;
        public boolean confirmed = false;
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
                if ("NXP_NFC_DEV_NODE".equals(keyVal.first))
                    if (!(status.confirmed = new File(keyVal.second).exists()))
                        return false;

                if ("NXP_NFC_CHIP".equals(keyVal.first))
                    status.chipName = resolveNXPChipCode(keyVal.second);
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
                    if (!(status.confirmed = new File(keyVal.second).exists()))
                        return false;

                    status.chipName = keyVal.second.replace("/dev/", "");
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
        return NXPMap.containsKey(code) ? NXPMap.get(code) : "Unknown NXP";
    }

    // list of config files and their parsers in system search order
    private final List<Pair<String, ILineParser>> configPaths = Arrays.asList(
        new Pair<>("/odm/etc/libnfc-brcm.conf", BRCM_PARSER),
        new Pair<>("/odm/etc/libnfc-nxp.conf", NXP_PARSER),

        new Pair<>("/vendor/etc/libnfc-brcm.conf", BRCM_PARSER),
        new Pair<>("/vendor/etc/libnfc-nxp.conf", NXP_PARSER),

        new Pair<>("/system/etc/libnfc-brcm.conf", BRCM_PARSER),
        new Pair<>("/system/etc/libnfc-nxp.conf", NXP_PARSER)
    );

    /**
     * Detects the NFCC on this device
     *
     * @return The name of the chip or null
     */
    public String detectNFCC() {
        String chipName = null;

        // search configuration files in order
        for (Pair<String, ILineParser> path : configPaths) {
            ParserStatus result = readConf(path.first, path.second);

            // ignore confirmed misses
            if (result != null) {
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
}
