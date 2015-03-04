package tud.seemuh.nfcgate;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


public class AboutWorkaroundActivity extends Activity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_workaround);

        mWebView = (WebView) findViewById(R.id.workaroundDescWebView);
        mWebView.loadUrl("file:///android_asset/WorkaroundInfo.html");
    }
}
