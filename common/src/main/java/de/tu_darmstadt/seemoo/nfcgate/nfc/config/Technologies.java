package de.tu_darmstadt.seemoo.nfcgate.nfc.config;

/**
 * Defines available NFC Tag technologies.
 *
 * We cannot define those as TagTechnology.class.getName() here otherwise we could not use these
 * constants in switch statements.
 */
public final class Technologies {
    public static final String A = "android.nfc.tech.NfcA";
    public static final String B = "android.nfc.tech.NfcB";
    public static final String F = "android.nfc.tech.NfcF";
    public static final String V = "android.nfc.tech.NfcV";
    public static final String IsoDep = "android.nfc.tech.IsoDep";
}
