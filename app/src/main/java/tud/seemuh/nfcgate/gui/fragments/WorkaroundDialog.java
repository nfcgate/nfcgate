package tud.seemuh.nfcgate.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tud.seemuh.nfcgate.R;

public class WorkaroundDialog extends DialogFragment{

    public interface WorkaroundDialogListener  {
        public void onWorkaroundPositiveClick();
        public void onWorkaroundNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    private static WorkaroundDialogListener mListener;

    public static WorkaroundDialog getInstance(WorkaroundDialogListener dialogInterface) {
        WorkaroundDialog fragmentDialog = new WorkaroundDialog();

        mListener = dialogInterface;

        return fragmentDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View pushDialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_workaroundwarning, null);

        final Button dismissBtn = (Button) pushDialogView.findViewById(R.id.btnWorkaroundNo);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send click event
                mListener.onWorkaroundNegativeClick();
                dismiss();
            }
        });

        Button goSettingsBtn = (Button) pushDialogView.findViewById(R.id.btnWorkaroundYes);
        goSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send click event
                mListener.onWorkaroundPositiveClick();
                dismiss();
            }
        });

        return pushDialogView;
    }
}
