package tud.seemuh.nfcgate.util.filter.conditional.test;

import junit.framework.TestCase;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;
import tud.seemuh.nfcgate.util.filter.conditional.EndsWith;

/**
 * Test cases for EndsWith conditional
 */
public class EndsWithTest extends TestCase {
    protected EndsWith ew;
    protected byte[] pattern1 = new byte[] { (byte)0x00, (byte)0x10 };
    protected byte[] pattern2 = new byte[] { (byte)0x10, (byte)0x10 };
    protected byte[] pattern3 = new byte[] { (byte)0x10 };

    protected byte[] target1 = new byte[] { (byte)0x00, (byte)0x00, (byte)0x10 };
    protected byte[] target2 = new byte[] { (byte)0x00, (byte)0x10, (byte)0x10 };
    protected byte[] target3 = new byte[] { (byte)0x00 };
    protected byte[] target4 = new byte[] { (byte)0x30, (byte)0x00, (byte)0x10 };
    protected byte[] target5 = new byte[] {};

    protected NfcComm nfc1 = new NfcComm(NfcComm.Source.HCE, target1);
    protected NfcComm nfc2 = new NfcComm(NfcComm.Source.HCE, target2);
    protected NfcComm nfc3 = new NfcComm(NfcComm.Source.HCE, target3);
    protected NfcComm nfc4 = new NfcComm(NfcComm.Source.HCE, target4);
    protected NfcComm nfc5 = new NfcComm(NfcComm.Source.HCE, target5);
    protected NfcComm nfc6 = new NfcComm(NfcComm.Source.HCE, pattern1);
    protected NfcComm nfc7 = new NfcComm(NfcComm.Source.HCE, pattern2);
    protected NfcComm nfc8 = new NfcComm(NfcComm.Source.HCE, pattern3);

    protected NfcComm anticol1 = new NfcComm(target1, target1[0], target1, target1);
    protected NfcComm anticol2 = new NfcComm(target2, target2[0], target2, target2);
    protected NfcComm anticol3 = new NfcComm(target3, target3[0], target3, target3);
    protected NfcComm anticol4 = new NfcComm(target4, target4[0], target4, target4);
    protected NfcComm anticol6 = new NfcComm(pattern1, pattern1[0], pattern1, pattern1);
    protected NfcComm anticol7 = new NfcComm(pattern2, pattern2[0], pattern2, pattern2);
    protected NfcComm anticol8 = new NfcComm(pattern3, pattern3[0], pattern3, pattern3);

    public void testIncorrectAnticolInit() {
        try {
            ew = new EndsWith(pattern1, Conditional.TARGET.ANTICOL);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testIncorrectNfcDataInit() {
        try {
            ew = new EndsWith(pattern1, Conditional.TARGET.NFC, Conditional.ANTICOLFIELD.ATQA);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testCorrectNfcDataInit() {
        try {
            ew = new EndsWith(pattern1, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
    }

    public void testCorrectAnticolInit() {
        try {
            ew = new EndsWith(pattern1, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.ATQA);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
    }

    public void testNFCDataPattern1Matching() {
        try {
            ew = new EndsWith(pattern1, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertTrue("Target 1 does not match", ew.applies(nfc1));
        assertFalse("Target 2 matches", ew.applies(nfc2));
        assertFalse("Target 3 matches", ew.applies(nfc3));
        assertTrue("Target 4 does not match", ew.applies(nfc4));
        assertFalse("Target 5 matches", ew.applies(nfc5));
        assertTrue("Pattern does not match itself", ew.applies(nfc6));
        assertFalse("Pattern 2 matches", ew.applies(nfc7));
        assertFalse("Pattern 3 matches", ew.applies(nfc8));
    }

    public void testNFCDataPattern2Matching() {
        try {
            ew = new EndsWith(pattern2, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Target 1 matches", ew.applies(nfc1));
        assertTrue("Target 2 does not match", ew.applies(nfc2));
        assertFalse("Target 3 matches", ew.applies(nfc3));
        assertFalse("Target 4 matches", ew.applies(nfc4));
        assertFalse("Target 5 matches", ew.applies(nfc5));
        assertFalse("Pattern 1 matches", ew.applies(nfc6));
        assertTrue("Pattern does not match itself", ew.applies(nfc7));
        assertFalse("Pattern 3 matches", ew.applies(nfc8));
    }

    public void testNFCDataPattern3Matching() {
        try {
            ew = new EndsWith(pattern3, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertTrue("Target 1 does not match", ew.applies(nfc1));
        assertTrue("Target 2 does not match", ew.applies(nfc2));
        assertFalse("Target 3 matches", ew.applies(nfc3));
        assertTrue("Target 4 does not match", ew.applies(nfc4));
        assertFalse("Target 5 matches", ew.applies(nfc5));
        assertTrue("Pattern 1 matches", ew.applies(nfc6));
        assertTrue("Does not match pattern 2", ew.applies(nfc7));
        assertTrue("Does not match itself", ew.applies(nfc8));
    }

    public void testEmptyPatternMatching() {
        try {
            ew = new EndsWith(target5, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertTrue("Target 1 does not match", ew.applies(nfc1));
        assertTrue("Target 2 does not match", ew.applies(nfc2));
        assertTrue("Target 3 does not match", ew.applies(nfc3));
        assertTrue("Target 4 does not match", ew.applies(nfc4));
        assertTrue("Target 5 does not match", ew.applies(nfc5));
        assertTrue("Pattern 1 does not match", ew.applies(nfc6));
        assertTrue("Pattern 2 does not match", ew.applies(nfc7));
        assertTrue("Pattern 3 does not match", ew.applies(nfc8));
    }

    public void testIncorrectTypeNotMatching1() {
        try {
            ew = new EndsWith(target5, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Incorrect type still matches", ew.applies(anticol1));
    }

    public void testIncorrectTypeNotMatching2() {
        try {
            ew = new EndsWith(pattern1, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.UID);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Incorrect type still matches", ew.applies(nfc1));
    }

    public void testAnticolSakMatching() {
        try {
            ew = new EndsWith((byte) pattern3[0], Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.SAK);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Target 1 matches", ew.applies(anticol1));
        assertFalse("Target 2 matches", ew.applies(anticol2));
        assertFalse("Target 3 matches", ew.applies(anticol3));
        assertFalse("Target 4 matches", ew.applies(anticol4));
        assertFalse("Pattern 1 matches", ew.applies(anticol6));
        assertTrue("Does not match pattern 2", ew.applies(anticol7));
        assertTrue("Does not match itself", ew.applies(anticol8));
    }

    public void testAnticolAtqaMatching() {
        try {
            ew = new EndsWith(pattern3, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.ATQA);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertTrue("Target 1 does not match", ew.applies(anticol1));
        assertTrue("Target 2 does not match", ew.applies(anticol2));
        assertFalse("Target 3 matches", ew.applies(anticol3));
        assertTrue("Target 4 does not match", ew.applies(anticol4));
        assertTrue("Pattern 1 does not match", ew.applies(anticol6));
        assertTrue("Does not match pattern 2", ew.applies(anticol7));
        assertTrue("Does not match itself", ew.applies(anticol8));
    }

    public void testAnticolUidMatching() {
        try {
            ew = new EndsWith(pattern3, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.UID);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertTrue("Target 1 does not match", ew.applies(anticol1));
        assertTrue("Target 2 does not match", ew.applies(anticol2));
        assertFalse("Target 3 matches", ew.applies(anticol3));
        assertTrue("Target 4 does not match", ew.applies(anticol4));
        assertTrue("Pattern 1 does not match", ew.applies(anticol6));
        assertTrue("Does not match pattern 2", ew.applies(anticol7));
        assertTrue("Does not match itself", ew.applies(anticol8));
    }

    public void testAnticolHistMatching() {
        try {
            ew = new EndsWith(pattern3, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.HIST);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertTrue("Target 1 matches", ew.applies(anticol1));
        assertTrue("Target 2 does not match", ew.applies(anticol2));
        assertFalse("Target 3 matches", ew.applies(anticol3));
        assertTrue("Target 4 matches", ew.applies(anticol4));
        assertTrue("Pattern 1 matches", ew.applies(anticol6));
        assertTrue("Does not match pattern 2", ew.applies(anticol7));
        assertTrue("Does not match itself", ew.applies(anticol8));
    }
}
