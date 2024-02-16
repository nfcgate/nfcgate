package de.tu_darmstadt.seemoo.nfcgate.gui.component;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.tu_darmstadt.seemoo.nfcgate.BuildConfig;
import de.tu_darmstadt.seemoo.nfcgate.R;

public class FileShare {
    public interface IFileShareable {
        void write(OutputStream stream) throws IOException;
    }

    // state variables
    private final Context mContext;
    private String mPrefix;
    private String mExtension;
    private String mMimeType;

    public FileShare(Context context) {
        mContext = context;

        // defaults
        mPrefix = "";
        mExtension = ".bin";
        mMimeType = "application/*";
    }

    public FileShare setPrefix(String prefix) {
        mPrefix = prefix;
        return this;
    }

    public FileShare setExtension(String extension) {
        mExtension = extension;
        return this;
    }

    public FileShare setMimeType(String mimeType) {
        mMimeType = mimeType;
        return this;
    }

    public void share(IFileShareable share) {
        // ensure share directory exists
        final File shareDir = new File(mContext.getCacheDir() + "/share/");
        shareDir.mkdir();

        // create file with given prefix and extension
        final File file = new File(shareDir, mPrefix + mExtension);
        try (final OutputStream stream = new FileOutputStream(file)) {

            // write to file (overwrites if already exists)
            share.write(stream);
        }
        catch (IOException e) {
            Toast.makeText(mContext, mContext.getString(R.string.share_error),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        // generate file provider URI for the sharing app
        Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID, file);

        // create intent with binary content type
        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType(mMimeType)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // open chooser and start selected intent
        mContext.startActivity(Intent.createChooser(shareIntent,
                mContext.getString(R.string.share_file)));
    }
}
