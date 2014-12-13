package tud.seemuh.nfcgate.network;

import android.os.AsyncTask;
import android.widget.TextView;

/**
 * Created by daniel on 11/22/14.
 */
public class UpdateUI extends AsyncTask<String, Void, String> {
    private TextView debugView;

    /**
     *
     * @param ldebugView: we need the view to append debug output to that
     */
    public UpdateUI(TextView ldebugView) {
        debugView = ldebugView;
    }

    @Override
    protected String doInBackground(String... params) {
        return params[0];
    }

    @Override
    protected void onPostExecute(String result) {
        debugView.append(result + "\n");
    }
}
