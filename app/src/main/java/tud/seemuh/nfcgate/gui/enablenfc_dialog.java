package tud.seemuh.nfcgate.gui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tud.seemuh.nfcgate.R;

public class enablenfc_dialog extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NFCNoticeDialogListener  {
        public void onNFCDialogPositiveClick();
        public void onNFCDialogNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    static NFCNoticeDialogListener mListener;

    public static enablenfc_dialog getInstance(NFCNoticeDialogListener dialogInterface) {
        enablenfc_dialog fragmentDialog = new enablenfc_dialog();

        mListener = dialogInterface;

        return fragmentDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View pushDialogView = getActivity().getLayoutInflater().inflate(R.layout.enablenfc, null);

        Button dismissBtn = (Button) pushDialogView.findViewById(R.id.dismiss);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send click event
                mListener.onNFCDialogNegativeClick();
            }
        });

        Button goSettingsBtn = (Button) pushDialogView.findViewById(R.id.go_settings);
        goSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send click event
                mListener.onNFCDialogPositiveClick();
            }
        });

        return pushDialogView;
    }
}
