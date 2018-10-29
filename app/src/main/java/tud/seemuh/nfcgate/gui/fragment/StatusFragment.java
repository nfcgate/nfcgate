package tud.seemuh.nfcgate.gui.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jaredrummler.android.device.DeviceName;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.component.CustomArrayAdapter;
import tud.seemuh.nfcgate.gui.component.StatusItem;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcConf;

public class StatusFragment extends BaseFragment {
    // ui references
    private ListView mStatus;
    private StatusListAdapter mStatusAdapter;

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
        mStatusAdapter.add(detectDeviceName());
        mStatusAdapter.add(detectAndroidVersion());
        mStatusAdapter.add(detectNfcEnabled());
        mStatusAdapter.add(detectModuleEnabled());
        mStatusAdapter.add(detectNfcModel());

        mStatusAdapter.notifyDataSetChanged();
    }

    StatusItem detectDeviceName() {
        // transform code name into market name
        String deviceName = DeviceName.getDeviceName();

        // No hist byte on this specific combination
        boolean is5X601 = deviceName.equals("Nexus 5X") && Build.VERSION.RELEASE.equals("6.0.1");

        return new StatusItem("Device Name")
                .setState(is5X601 ? StatusItem.State.WARN: StatusItem.State.OK)
                .setValue(deviceName);
    }

    StatusItem detectAndroidVersion() {
        return new StatusItem("Android Version")
                .setState(Build.VERSION.SDK_INT < 26 ? StatusItem.State.OK : StatusItem.State.WARN)
                .setValue(Build.VERSION.RELEASE);
    }

    StatusItem detectNfcEnabled() {
        // nfc capability and enabled
        boolean hasNfc = getNfc().hasNfc();

        return new StatusItem("NFC Capability")
                .setState(hasNfc ? StatusItem.State.OK : StatusItem.State.ERROR)
                .setValue(hasNfc);
    }

    StatusItem detectModuleEnabled() {
        // xposed module enabled
        boolean hasModule = NfcManager.isHookLoaded();

        return new StatusItem("Xposed Module Enabled")
                .setState(hasModule ? StatusItem.State.OK : StatusItem.State.WARN)
                .setValue(hasModule);
    }

    StatusItem detectNfcModel() {
        // null or chip model name
        String chipName = new NfcConf().detectNfcc();

        return new StatusItem("NFC Chip")
                .setState(chipName != null ? StatusItem.State.OK : StatusItem.State.WARN)
                .setValue(chipName != null ? chipName : "Unknown");
    }

    private class StatusListAdapter extends CustomArrayAdapter<StatusItem> {
        StatusListAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        @DrawableRes
        private int byState(StatusItem.State state) {
            switch (state) {
                default:
                case OK:
                    return R.drawable.ic_check_circle_green_24dp;
                case WARN:
                    return R.drawable.ic_help_orange_24dp;
                case ERROR:
                    return R.drawable.ic_error_red_24dp;
            }
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            final StatusItem item = getItem(position);

            v.<TextView>findViewById(R.id.status_name).setText(item.getName());
            v.<TextView>findViewById(R.id.status_value).setText(item.getValue());
            v.<ImageView>findViewById(R.id.status_icon).setImageResource(byState(item.getState()));

            return v;
        }
    }
}
