package tud.seemuh.nfcgate.util;

import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class contains a workaround to enable us to update UI elements from another thread,
 * something which is appearently not possible with standard android methods.
 */
public class UpdateUI extends AsyncTask<String, Void, String> {

    public enum UpdateMethod {
        appendTextView, setTextTextView, setTextButton, enableButton, disableButton
    }

    private TextView mView;
    private Button mButton;
    private UpdateMethod mMethod;

    /**
     *
     * @param lDebugView: we need the view for executing for example an setText() on it
     * @param lMethod: specifies the operation which should executed on the view
     */
    public UpdateUI(TextView lDebugView, UpdateMethod lMethod) {
        mView = lDebugView;
        mMethod = lMethod;
    }

    /**
     *
     * @param lButton: we need the button for executing for example an setText() on it
     * @param lMethod: specifies the operation which should executed on the view
     */
    public UpdateUI(Button lButton, UpdateMethod lMethod) {
        mButton = lButton;
        mMethod = lMethod;
    }

    @Override
    protected String doInBackground(String... params) {
        return params[0];
    }

    @Override
    protected void onPostExecute(String result) {
        if(mMethod == UpdateMethod.appendTextView) {
            mView.append(result);
        } else if(mMethod == UpdateMethod.setTextTextView) {
            mView.setText(result);
        } else if(mMethod == UpdateMethod.setTextButton) {
            mButton.setText(result);
        } else if(mMethod == UpdateMethod.enableButton) {
            mButton.setEnabled(true);
        } else if(mMethod == UpdateMethod.disableButton) {
            mButton.setEnabled(false);
        } else {
            //this should never happen
        }
    }
}
