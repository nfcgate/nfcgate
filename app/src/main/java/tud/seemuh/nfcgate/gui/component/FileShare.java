package tud.seemuh.nfcgate.gui.component;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import tud.seemuh.nfcgate.BuildConfig;

public class FileShare {
    private Context mContext;
    private File mFile;
    private OutputStream mStream;

    public FileShare(Context context, String prefix, String extension) {
        mContext = context;
        createShareFile(prefix, extension);
    }

    public OutputStream getStream() {
        return mStream;
    }

    public void share() {
        // generate file provider URI for the sharing app
        Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID, mFile);

        // create intent with binary content type
        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("application/*")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // open chooser and start selected intent
        mContext.startActivity(Intent.createChooser(shareIntent, "Share file"));
    }

    private void createShareFile(String prefix, String extension) {
        // ensure share directory exists
        File shareDir = new File(mContext.getCacheDir() + "/share/");
        shareDir.mkdir();

        // create random file in share directory with given prefix and extension
        try {
            mFile = File.createTempFile(prefix, extension, shareDir);
            mStream = new FileOutputStream(mFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
