package tud.seemuh.nfcgate.gui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tud.seemuh.nfcgate.R;

public class EnablenfcDialog extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NFCNoticeDialogListener  {
        public void onNFCDialogPositiveClick();
        public void onNFCDialogNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    private static NFCNoticeDialogListener mListener;

    public static EnablenfcDialog getInstance(NFCNoticeDialogListener dialogInterface) {
        EnablenfcDialog fragmentDialog = new EnablenfcDialog();

        mListener = dialogInterface;

        return fragmentDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View pushDialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_enablenfc, null);

        final Button dismissBtn = (Button) pushDialogView.findViewById(R.id.enablenfc_dismiss_btn);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send click event
                mListener.onNFCDialogNegativeClick();
                dismiss();
            }
        });

        Button goSettingsBtn = (Button) pushDialogView.findViewById(R.id.enablenfc_go_settings_btn);
        goSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send click event
                mListener.onNFCDialogPositiveClick();
                dismiss();
            }
        });

        return pushDialogView;
    }
}
