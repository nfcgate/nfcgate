package tud.seemuh.nfcgate.network;

import android.os.AsyncTask;
import android.widget.TextView;


public class UpdateUI extends AsyncTask<String, Void, String> {

    public enum TextUpdates {
        append, setText
    }
    private TextView mView;
    private TextUpdates mMethod;

    /**
     *
     * @param ldebugView: we need the view to append debug output to that
     */
    public UpdateUI(TextView ldebugView, TextUpdates lmethod) {
        mView = ldebugView;
        mMethod = lmethod;
    }

    @Override
    protected String doInBackground(String... params) {
        return params[0];
    }

    @Override
    protected void onPostExecute(String result) {
        if(mMethod == TextUpdates.append) {
            mView.append(result + "\n");
        } else if(mMethod == TextUpdates.setText) {
            mView.setText(result);
        } else {
            //this should never happen
        }
    }
}
