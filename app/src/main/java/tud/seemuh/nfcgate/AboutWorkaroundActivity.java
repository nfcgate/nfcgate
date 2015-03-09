package tud.seemuh.nfcgate;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import java.io.IOException;
import java.util.Locale;


public class AboutWorkaroundActivity extends Activity {
    private WebView mWebView;

    private String TAG = "AboutWorkaroundAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_workaround);

        mWebView = (WebView) findViewById(R.id.workaroundDescWebView);
        String loc = Locale.getDefault().getLanguage();
        AssetManager mg = getResources().getAssets();
        String path = "html/bcm20793-info." + loc + ".html";
        try {
            mg.open(path);
            Log.i(TAG, "HTML exists for locale " + loc + ", using it.");
            mWebView.loadUrl("file:///android_asset/" + path);
        } catch (IOException ex) {
            Log.i(TAG, "No HTML for locale " + loc + ", using default (en)");
            mWebView.loadUrl("file:///android_asset/html/bcm20793-info.en.html");
        }
    }
}
