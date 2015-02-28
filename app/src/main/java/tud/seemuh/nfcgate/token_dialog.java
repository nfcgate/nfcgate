package tud.seemuh.nfcgate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Tom on 14.02.2015.
 */
public class token_dialog extends DialogFragment {
/* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onTokenDialogPositiveClick(DialogFragment dialog);
        public void onTokenDialogNegativeClick(DialogFragment dialog);
    }

    public static final String PREF_FILE_NAME = "SeeMoo.NFCGate.Prefs";

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

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
        final View view = getActivity().getLayoutInflater().inflate(R.layout.token, null);
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
                        mListener.onTokenDialogPositiveClick(token_dialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onTokenDialogNegativeClick(token_dialog.this);
                    }
                });
        return builder.create();
    }

}
