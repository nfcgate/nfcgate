package de.tu_darmstadt.seemoo.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.nfc.tech.TagTechnology;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.Technologies;

/**
 * Interface to all NFCTagReader-Classes.
 */
public abstract class NFCTagReader {
    final TagTechnology mReader;

    NFCTagReader(TagTechnology reader) {
        mReader = reader;
    }

    /**
     * Indicates whether the connection is open
     */
    boolean isConnected() {
        return mReader.isConnected();
    }

    /**
     * Opens the connection
     */
    public void connect() {
        try{
            mReader.connect();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the connection, no further communication will be possible
     */
    public void close() {
        try{
            mReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a raw command to the NFC chip, receiving the answer as a byte[]
     *
     * @param command: byte[]-representation of the command to be sent
     * @return byte[]-representation of the answer of the NFC chip
     */
    public byte[] transceive(byte[] command) {
        try {
            // there is no common interface for TagTechnology...
            Method transceive = mReader.getClass().getMethod("transceive", byte[].class);
            return (byte[])transceive.invoke(mReader, command);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a config object with options set to emulate this tag
     */
    public abstract ConfigBuilder getConfig();

    /**
     * Returns the raw ATS_RES/ATTRIB_RES bytes of the tag response.
     * Xposed hooks this method to return the res bytes of the tag if hooks are available.
     */
    public static byte[] extractTagResBytes() {
        return null;
    }

    /**
     * Picks the highest available technology for a given Tag
     */
    public static NFCTagReader create(Tag tag) {
        List<String> technologies = Arrays.asList(tag.getTechList());

        // look for higher layer technology
        if (technologies.contains(Technologies.IsoDep)) {
            // an IsoDep tag can be backed by either NfcA or NfcB technology
            if (technologies.contains(Technologies.A))
                return new IsoDepReader(tag, Technologies.A);
            else if (technologies.contains(Technologies.B))
                return new IsoDepReader(tag, Technologies.B);
            else
                Log.e("NFCGATE", "Unknown tag technology backing IsoDep" +
                        TextUtils.join(", ", technologies));
        }

        for (String tech : technologies) {
            switch (tech) {
                case Technologies.A:
                    return new NfcAReader(tag);
                case Technologies.B:
                    return new NfcBReader(tag);
                case Technologies.F:
                    return new NfcFReader(tag);
                case Technologies.V:
                    return new NfcVReader(tag);
            }
        }

        throw new UnsupportedOperationException("Unknown Tag type");
    }

    protected static byte findMaxNCIBitRate(byte bc) {
        // extract positive integer value
        int bci = bc & 0xFF;
        // bits 7-4: Card to reader divisor (DS)
        // bits 3-0: Reader to card divisor (DR)
        int ds = (bci >> 4) & 0x0F;  // Card to reader
        int dr = bci & 0x0F;         // Reader to card

        // default to 106 kbps
        byte result = 0x00;

        // check if 212 kbps (0x01) is supported
        if ((ds & 0x01) != 0 && (dr & 0x01) != 0)
            result = 0x01;
        // check if 424 kbps (0x02) is supported
        if ((ds & 0x03) == 0x03 && (dr & 0x03) == 0x03)
            result = 0x02;
        // check if 848 kbps (0x03) is supported
        if ((ds & 0x07) == 0x07 && (dr & 0x07) == 0x07)
            result = 0x03;

        return result;
    }
}
