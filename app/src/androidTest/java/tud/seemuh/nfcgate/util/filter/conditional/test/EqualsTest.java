package tud.seemuh.nfcgate.util.filter.conditional.test;

import junit.framework.TestCase;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;
import tud.seemuh.nfcgate.util.filter.conditional.Equals;

/**
 * Testing the implementation of the Equals conditional
 */
public class EqualsTest extends TestCase {
    protected Equals eq;
    protected byte[] pattern1 = new byte[] { (byte)0x00, (byte)0x10 };
    protected byte[] pattern2 = new byte[] { (byte)0x10, (byte)0x10 };
    protected byte[] pattern3 = new byte[] { (byte)0x10 };
    protected byte pattern4 = (byte)0x00;

    protected byte[] target1 = new byte[] { (byte)0x00, (byte)0x10, (byte)0x00 };
    protected byte[] target2 = new byte[] { (byte)0x10, (byte)0x10, (byte)0x00 };
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
            eq = new Equals(pattern1, Conditional.TARGET.ANTICOL);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testIncorrectNfcDataInit() {
        try {
            eq = new Equals(pattern1, Conditional.TARGET.NFC, Conditional.ANTICOLFIELD.ATQA);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testIncorrectAntocolSakInit() {
        try {
            eq = new Equals(pattern1, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.SAK);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testCorrectNfcDataInit() {
        try {
            eq = new Equals(pattern1, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
    }

    public void testCorrectAnticolAtqaInit() {
        try {
            eq = new Equals(pattern1, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.ATQA);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
    }

    public void testCorrectAnticolSakInit() {
        try {
            eq = new Equals(pattern4, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.SAK);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
    }

    public void testNFCDataPattern1Matching() {
        try {
            eq = new Equals(pattern1, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Target 1 matches", eq.applies(nfc1));
        assertFalse("Target 2 matches", eq.applies(nfc2));
        assertFalse("Target 3 matches", eq.applies(nfc3));
        assertFalse("Target 4 matches", eq.applies(nfc4));
        assertFalse("Target 5 matches", eq.applies(nfc5));
        assertTrue("Pattern does not match itself", eq.applies(nfc6));
        assertFalse("Pattern 2 matches", eq.applies(nfc7));
        assertFalse("Pattern 3 matches", eq.applies(nfc8));
    }

    public void testNFCDataPattern2Matching() {
        try {
            eq = new Equals(pattern2, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Target 1 matches", eq.applies(nfc1));
        assertFalse("Target 2 matches", eq.applies(nfc2));
        assertFalse("Target 3 matches", eq.applies(nfc3));
        assertFalse("Target 4 matches", eq.applies(nfc4));
        assertFalse("Target 5 matches", eq.applies(nfc5));
        assertFalse("Pattern 1 matches", eq.applies(nfc6));
        assertTrue("Pattern does not match itself", eq.applies(nfc7));
        assertFalse("Pattern 3 matches", eq.applies(nfc8));
    }

    public void testEmptyPatternMatching() {
        try {
            eq = new Equals(target5, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Target 1 matches", eq.applies(nfc1));
        assertFalse("Target 2 matches", eq.applies(nfc2));
        assertFalse("Target 3 matches", eq.applies(nfc3));
        assertFalse("Target 4 matches", eq.applies(nfc4));
        assertTrue("Target 5 does not match", eq.applies(nfc5));
        assertFalse("Pattern 1 matches", eq.applies(nfc6));
        assertFalse("Pattern 2 matches", eq.applies(nfc7));
        assertFalse("Pattern 3 matches", eq.applies(nfc8));
    }

    public void testIncorrectTypeNotMatching1() {
        try {
            eq = new Equals(target5, Conditional.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Incorrect type still matches", eq.applies(anticol1));
    }

    public void testIncorrectTypeNotMatching2() {
        try {
            eq = new Equals(pattern1, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.UID);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertFalse("Incorrect type still matches", eq.applies(nfc1));
    }

    public void testAnticolSakMatching() {
        try {
            eq = new Equals(pattern4, Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.SAK);
        } catch (Exception e) {
            assertTrue("Exception thrown where it was not supposed to", false);
        }
        assertTrue("Target 1 does not match", eq.applies(anticol1));
        assertFalse("Target 2 matches", eq.applies(anticol2));
        assertTrue("Target 3 does not match", eq.applies(anticol3));
        assertFalse("Target 4 matches", eq.applies(anticol4));
        assertTrue("Pattern 1 does not match", eq.applies(anticol6));
        assertFalse("Pattern 2 matches", eq.applies(anticol7));
        assertFalse("Pattern 3 matches", eq.applies(anticol8));
    }
}
