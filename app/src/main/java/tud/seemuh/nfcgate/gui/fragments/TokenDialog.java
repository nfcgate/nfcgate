package tud.seemuh.nfcgate.gui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;

public class TokenDialog extends DialogFragment {
/* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onTokenDialogPositiveClick();
        public void onTokenDialogNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    private static NoticeDialogListener mListener;

    public static TokenDialog getInstance(NoticeDialogListener dialogInterface) {
        TokenDialog fragmentDialog = new TokenDialog();

        mListener = dialogInterface;

        return fragmentDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set title
        getDialog().setTitle(R.string.title_dialog_token);

        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_token, null);

        final Button dismissBtn = (Button) view.findViewById(R.id.token_cancel_btn);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send click event
                mListener.onTokenDialogNegativeClick();
                dismiss();
            }
        });

        Button goSettingsBtn = (Button) view.findViewById(R.id.token_submit_btn);
        goSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send click event
                TextView tempToken = (TextView) view.findViewById(R.id.token);
                String token = tempToken.getText().toString();

                // create Shared Preferences Buffer in private mode
                //SharedPreferences preferences = getActivity().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
                // Store the token in the preferences buffer for later usage
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("token", token);
                editor.commit();
                mListener.onTokenDialogPositiveClick();
                dismiss();
            }
        });

        return view;
    }


}
