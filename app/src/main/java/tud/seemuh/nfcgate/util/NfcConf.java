package tud.seemuh.nfcgate.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NfcConf {
    private interface ILineParser {
        void onLine(String line);
    }

    private Map<String, String> mNxpMap = new HashMap<>();
    private String mChipName = null;

    private ILineParser NXP_PARSER = new ILineParser() {
        @Override
        public void onLine(String line) {
            if (line.startsWith("NXP_NFC_CHIP=")) {
                String chipCode = line.substring(line.indexOf('=') + 1);
                mChipName = mNxpMap.get(chipCode);
            }
        }
    };
    private ILineParser BRCM_PARSER = new ILineParser() {
        @Override
        public void onLine(String line) {
            if (line.startsWith("TRANSPORT_DRIVER=")) {
                String devStr = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
                mChipName = devStr.replace("/dev/", "");
            }
        }
    };

    public NfcConf() {
        // NXP chip codes as of 2018-10-29
        mNxpMap.put("0x01","PN547C2");
        mNxpMap.put("0x02","PN65T");
        mNxpMap.put("0x03","PN548AD");
        mNxpMap.put("0x04","PN66T");
        mNxpMap.put("0x05","PN551");
        mNxpMap.put("0x06","PN67T");
        mNxpMap.put("0x07","PN553");
        mNxpMap.put("0x08","PN80T");
    }

    public String detectNfcc() {
        // old broadcom
        readConf(new File("/system/etc/libnfc-brcm.conf"), BRCM_PARSER);
        // new broadcom
        readConf(new File("/vendor/etc/libnfc-brcm.conf"), BRCM_PARSER);
        // old nxp
        readConf(new File("/system/etc/libnfc-nxp.conf"), NXP_PARSER);
        // new nxp
        readConf(new File("/vendor/etc/libnfc-nxp.conf"), NXP_PARSER);

        return mChipName;
    }

    private void readConf(File file, ILineParser parser) {
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line; (line = br.readLine()) != null; )
                    parser.onLine(line);
            }
            catch (IOException ignored) { }
        }
    }
}
