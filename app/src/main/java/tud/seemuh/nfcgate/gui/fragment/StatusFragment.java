package tud.seemuh.nfcgate.gui.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.MainActivity;

public class StatusFragment extends Fragment {
    // ui references
    private ListView mStatus;
    private StatusListAdapter mStatusAdapter;

    // logic
    private Map<String, String> mNxpMap = new HashMap<>();

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
        View v = inflater.inflate(R.layout.fragment_status, container, false);

        // setup listview
        mStatus = v.findViewById(R.id.status_list);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mStatusAdapter = new StatusListAdapter(getActivity(), R.layout.list_status);
        mStatus.setAdapter(mStatusAdapter);

        detect();
    }

    void detect() {
        mStatusAdapter.add(detectAndroidVersion());
        mStatusAdapter.add(detectNfcEnabled());
        mStatusAdapter.add(detectNfcModel());

        mStatusAdapter.notifyDataSetChanged();
    }

    StatusItem detectAndroidVersion() {
        return new StatusItem("Android Version", Build.VERSION.RELEASE);
    }

    StatusItem detectNfcEnabled() {
        return new StatusItem("NFC capability", getMainActivity().getNfc().hasNfc());
    }

    StatusItem detectNfcModel() {
        StatusItem chipName = new StatusItem("NFC Chip", "Unknown");

        File confNxp = new File("/vendor/etc/libnfc-nxp.conf");
        File confNxpOld = new File("/system/etc/libnfc-nxp.conf");
        File confBrcm = new File("/vendor/etc/libnfc-brcm.conf");
        File confBrcmOld = new File("/system/etc/libnfc-brcm.conf");

        if (confNxp.exists())
            readNxpConf(confNxp, chipName);
        else if(confNxpOld.exists())
            readNxpConf(confNxpOld, chipName);

        if (confBrcm.exists())
            readBrcmConf(confBrcm, chipName);
        else if (confBrcmOld.exists())
            readBrcmConf(confBrcmOld, chipName);

        return chipName;
    }

    void readNxpConf(File file, StatusItem item) {
        // read nxp config
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("NXP_NFC_CHIP=")) {
                    String chipCode = line.substring(line.indexOf('=') + 1);
                    item.setValue(mNxpMap.get(chipCode));
                    break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readBrcmConf(File file, StatusItem item) {
        // read brcm config
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("FW_PRE_PATCH=")) {
                    String firmware = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
                    item.setValue(firmware.substring(17, firmware.length() - 17));
                    break;
                }
                if (line.startsWith("FW_PATCH=")) {
                    String firmware = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
                    item.setValue(firmware.substring(17, firmware.length() - 13));
                    break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected MainActivity getMainActivity() {
        return ((MainActivity) getActivity());
    }


    private class StatusItem {
        private String mName;
        private String mValue;

        StatusItem(String name, String value) {
            mName = name;
            mValue = value;
        }

        StatusItem(String name, boolean yesNo) {
            this(name, yesNo ? "yes" : "no");
        }

        public String getName() {
            return mName;
        }

        public String getValue() {
            return mValue;
        }

        public void setValue(String value) {
            mValue = value;
        }
    }

    private class StatusListAdapter extends ArrayAdapter<StatusItem> {
        private int mResource;

        StatusListAdapter(@NonNull Context context, int resource) {
            super(context, resource);

            mResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;

            if (v == null)
                v = LayoutInflater.from(getContext()).inflate(mResource, null);

            final StatusItem entry = getItem(position);
            if (entry != null) {
                v.<TextView>findViewById(R.id.status_name).setText(entry.getName());
                v.<TextView>findViewById(R.id.status_value).setText(entry.getValue());
            }

            return v;
        }
    }
}
