package tud.seemuh.nfcgate.gui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    public static final String PREF_FILE_NAME = "SeeMoo.NFCGate.Prefs";

    // Use this instance of the interface to deliver action events
    private static NoticeDialogListener mListener;

    public static TokenDialog getInstance(NoticeDialogListener dialogInterface) {
        TokenDialog fragmentDialog = new TokenDialog();

        mListener = dialogInterface;

        return fragmentDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
                SharedPreferences preferences = getActivity().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
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

    /*
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_token, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setMessage("Enter Token")
                .setPositiveButton("Submit Token", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get user input (e.g. token) from the textview
                        TextView tempToken = (TextView) view.findViewById(R.id.token);
                        String token = tempToken.getText().toString();

                        // create Shared Preferences Buffer in private mode
                        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
                        // Store the token in the preferences buffer for later usage
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", token);
                        editor.commit();
                        mListener.onTokenDialogPositiveClick(TokenDialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onTokenDialogNegativeClick(TokenDialog.this);
                    }
                });
        return builder.create();
    }
    */


}
