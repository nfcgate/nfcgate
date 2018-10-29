package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tud.seemuh.nfcgate.R;

public class StatusFragment extends Fragment {
    Map<String, String> mNxpMap = new HashMap<>();

    public StatusFragment() {
        mNxpMap.put("0x01","PN547C2");
        mNxpMap.put("0x02","PN65T");
        mNxpMap.put("0x03","PN548AD");
        mNxpMap.put("0x04","PN66T");
        mNxpMap.put("0x05","PN551");
        mNxpMap.put("0x06","PN67T");
        mNxpMap.put("0x07","PN553");
        mNxpMap.put("0x08","PN80T");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toast.makeText(getActivity(), detectNfcModel(), Toast.LENGTH_SHORT).show();
    }

    String detectNfcModel() {
        String chipName = "";

        File confNxp = new File("/vendor/etc/libnfc-nxp.conf");
        File confNxpOld = new File("/system/etc/libnfc-nxp.conf");
        File confBrcm = new File("/vendor/etc/libnfc-brcm.conf");
        File confBrcmOld = new File("/system/etc/libnfc-brcm.conf");

        if (confNxpOld.exists())
            chipName = readNxpConf(confNxpOld, chipName);
        if (confNxp.exists())
            chipName = readNxpConf(confNxp, chipName);

        if (confBrcmOld.exists())
            chipName = readBrcmConf(confBrcmOld, chipName);
        if (confBrcm.exists())
            chipName = readBrcmConf(confBrcm, chipName);

        return chipName;
    }

    String readNxpConf(File file, String def) {
        // read nxp config
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("NXP_NFC_CHIP=")) {
                    String chipCode = line.substring(line.indexOf('=') + 1);
                    return mNxpMap.get(chipCode);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return def;
    }

    String readBrcmConf(File file, String def) {
        // read brcm config
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("FW_PRE_PATCH=")) {
                    String firmware = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
                    return firmware.substring(17, firmware.length() - 17);
                }
                if (line.startsWith("FW_PATCH=")) {
                    String firmware = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
                    return firmware.substring(17, firmware.length() - 13);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return def;
    }
}
