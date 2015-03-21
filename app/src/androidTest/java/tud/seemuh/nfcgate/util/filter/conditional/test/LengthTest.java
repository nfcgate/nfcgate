package tud.seemuh.nfcgate.util.filter.conditional.test;

import junit.framework.TestCase;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;
import tud.seemuh.nfcgate.util.filter.conditional.Length;

/**
 * Test cases for the Length conditional
 */
public class LengthTest extends TestCase {
    Length le;

    protected byte[] pattern1 = new byte[] {(byte)0x00, (byte)0x10, (byte)0x23};
    protected byte[] pattern2 = new byte[] {};
    protected byte   pattern3 = (byte)0x00;

    protected NfcComm nfc1 = new NfcComm(NfcComm.Source.HCE, pattern1);
    protected NfcComm nfc2 = new NfcComm(NfcComm.Source.CARD, pattern2);
    protected NfcComm anticol1 = new NfcComm(pattern1, pattern3, pattern1, pattern1);


    public void testLengthConditional1() {
        try {
            le = new Length(3, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where none should have been", false);
        }

        assertTrue("Pattern 1 not detected as length 3", le.applies(nfc1));
        assertFalse("Pattern 2 detected as length 3", le.applies(nfc2));
    }

    public void testLengthConditional2() {
        try {
            le = new Length(0, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where none should have been", false);
        }

        assertFalse("Pattern 1 detected as length 0", le.applies(nfc1));
        assertTrue("Pattern 2 not detected as length 0", le.applies(nfc2));
    }

    public void testLengthConditional3() {
        try {
            le = new Length(0, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.SAK);
        } catch (Exception e) {
            assertTrue("Exception thrown where none should have been", false);
        }

        assertFalse("Byte detected as length 0", le.applies(anticol1));
    }

    public void testLengthConditional4() {
        try {
            le = new Length(1, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.SAK);
        } catch (Exception e) {
            assertTrue("Exception thrown where none should have been", false);
        }

        assertTrue("Byte not detected as length 1", le.applies(anticol1));
    }
}
