package tud.seemuh.nfcgate.util.sink;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.Utils;

/**
 * The FileSink class provides a simple file dump interface. Data is written to a regular text file
 * and can be read using any text editor.
 */
public class FileSink implements Sink {
    public String TAG = "FileSink";

    private BlockingQueue<NfcComm> mQueue;
    private File mOutfile;

    public FileSink(String filename) throws SinkInitException {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath() + "/nfcgate/dump");
            dir.mkdirs();
            mOutfile = new File(dir, filename);
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            throw new SinkInitException("SDCard is mounted read-only");
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            throw new SinkInitException("External Storage not available");
        }
    }

    @Override
    public void setQueue(BlockingQueue<NfcComm> readQueue) {
        mQueue = readQueue;
    }

    @Override
    public void run() {
        // Initialization phase
        FileWriter outStream;
        try {
            // Open file to write, in append mode
            outStream = new FileWriter(mOutfile, true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date now = new Date();
        String strDate = sdfDate.format(now);
        try {
            outStream.write("Dump started at " + strDate + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        sdfDate = new SimpleDateFormat("HH:mm:ss", Locale.US);

        // Main loop
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Get a new message
                NfcComm msg = mQueue.take();
                now = new Date();
                strDate = sdfDate.format(now);

                if (msg.getType() == NfcComm.Type.AnticolBytes) {
                    String output = "";
                    // We are dealing with an Anticol message
                    if (msg.isChanged()) {
                        output = "Card data (pre-filter in bracket): "
                                + "UID: " + Utils.bytesToHex(msg.getUid())
                                + " (" + Utils.bytesToHex(msg.getOldUid()) + ")"
                                + " - ATQA: " + Utils.bytesToHex(msg.getAtqa())
                                + " (" + Utils.bytesToHex(msg.getOldAtqa()) + ")"
                                + " - SAK: " + Utils.bytesToHex(msg.getSak())
                                + " (" + Utils.bytesToHex(msg.getOldSak()) + ")"
                                + " - Hist: " + Utils.bytesToHex(msg.getHist())
                                + " (" + Utils.bytesToHex(msg.getOldHist()) + ")";
                    } else {
                        output = "Card data: "
                                + "UID: " + Utils.bytesToHex(msg.getUid())
                                + " - ATQA: " + Utils.bytesToHex(msg.getAtqa())
                                + " - SAK: " + Utils.bytesToHex(msg.getSak())
                                + " - Hist: " + Utils.bytesToHex(msg.getHist());
                        // Write to file
                    }
                    outStream.write(strDate + ": " + output + "\n");

                } else if (msg.getType() == NfcComm.Type.NFCBytes) {
                    // We are dealing with regular NFC traffic.
                    if (msg.getSource() == NfcComm.Source.CARD) {
                        // Write out NFC data sent by card
                        if (msg.isChanged()) {
                            outStream.write(strDate + ": Card: " + Utils.bytesToHex(msg.getData()) +
                                    " (" + Utils.bytesToHex(msg.getOldData()) + ")\n");
                        } else {
                            outStream.write(strDate + ": Card: " + Utils.bytesToHex(msg.getData()) + "\n");
                        }
                    } else if (msg.getSource() == NfcComm.Source.HCE) {
                        // Write out NFC data sent by reader
                        if (msg.isChanged()) {
                            outStream.write(strDate + ": HCE:  " + Utils.bytesToHex(msg.getData()) +
                                    " (" + Utils.bytesToHex(msg.getOldData()) + ")\n");
                        } else {
                            outStream.write(strDate + ": HCE:  " + Utils.bytesToHex(msg.getData()) + "\n");
                        }
                    } else {
                        Log.e(TAG, "run: Unhandled message source, doing nothing");
                    }
                } else {
                    Log.e(TAG, "run: Unhandled message type, doing nothing");
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "run: Interrupted, exiting.");
                break;
            } catch (IOException e) {
                Log.e(TAG, "run: IOException", e);
                break;
            }
        }

        // Main loop terminated, clean up
        try {
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
