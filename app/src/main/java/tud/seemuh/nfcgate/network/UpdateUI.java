package tud.seemuh.nfcgate.network;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import tud.seemuh.nfcgate.MainActivity;

/**
 * Created by daniel on 11/22/14.
 */
public class UpdateUI extends AsyncTask<String, Void, String> {
    private TextView debugView;

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
