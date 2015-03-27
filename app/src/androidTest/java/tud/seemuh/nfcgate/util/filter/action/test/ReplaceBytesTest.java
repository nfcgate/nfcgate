package tud.seemuh.nfcgate.util.filter.action.test;

import junit.framework.TestCase;

import java.util.Arrays;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.action.Action;
import tud.seemuh.nfcgate.util.filter.action.ReplaceBytes;

/**
 * Testing the implementation of the ReplaceMessage action.
 */
public class ReplaceBytesTest extends TestCase {
    ReplaceBytes rb;
    protected byte[] target1 = new byte[] {(byte)0x00};
    protected byte[] target2 = new byte[] {(byte)0x00, (byte)0x10};
    protected byte   target3 = (byte)0x12;

    protected byte[] rand1 = new byte[] {(byte)0x31, (byte)0x42, (byte)0xff};

    protected byte[] expected1 = new byte[] {(byte)0x00};
    protected byte[] expected2 = new byte[] {(byte)0x00, (byte)0x00};
    protected byte[] expected3 = new byte[] {(byte)0x31, (byte)0x00, (byte)0xff};

    protected byte[] expected4 = new byte[] {(byte)0x31, (byte)0x00, (byte)0x10};

    protected NfcComm nfc1;
    protected NfcComm nfc2;
    protected NfcComm nfc3;

    protected NfcComm anticol1;
    protected NfcComm anticol2;
    protected NfcComm anticol3;
    protected NfcComm anticol4;

    protected void setUp() {
        nfc1 = new NfcComm(NfcComm.Source.HCE, target1);
        nfc2 = new NfcComm(NfcComm.Source.HCE, target2);
        nfc3 = new NfcComm(NfcComm.Source.HCE, rand1);
        anticol1 = new NfcComm(target1, target1[0], target1, target1);
        anticol2 = new NfcComm(target2, target2[0], target2, target2);
        anticol3 = new NfcComm(new byte[] {target3}, target3, new byte[] {target3}, new byte[] {target3});
        anticol4 = new NfcComm(rand1, rand1[0], rand1, rand1);
    }

    public void testIncorrectConstructor1() {
        try {
            rb = new ReplaceBytes(target1, 0, Action.TARGET.ANTICOL);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testIncorrectConstructor2() {
        try {
            rb = new ReplaceBytes(target1, 0, Action.TARGET.NFC, Action.ANTICOLFIELD.UID);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testIncorrectConstructor3() {
        try {
            rb = new ReplaceBytes(target1, 0, Action.TARGET.ANTICOL, Action.ANTICOLFIELD.SAK);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testIncorrectConstructor4() {
        try {
            rb = new ReplaceBytes(target3, 0, Action.TARGET.ANTICOL, Action.ANTICOLFIELD.HIST);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testIncorrectConstructor5() {
        try {
            rb = new ReplaceBytes(target1, -1, Action.TARGET.NFC);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testNfcReplacement1() {
        try {
            rb = new ReplaceBytes(target1, 1, Action.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where not expected", false);
        }

        NfcComm rep1 = rb.performAction(nfc1);
        NfcComm rep2 = rb.performAction(nfc2);
        NfcComm rep3 = rb.performAction(nfc3);

        assertTrue("Data change did not work", Arrays.equals(expected1, rep1.getData()));
        assertTrue("Data change did not work", Arrays.equals(expected2, rep2.getData()));
        assertTrue("Data change did not work", Arrays.equals(expected3, rep3.getData()));

        assertEquals("Old value was not preserved", target1, rep1.getOldData());
        assertEquals("Old value was not preserved", target2, rep2.getOldData());
        assertEquals("Old value was not preserved", rand1, rep3.getOldData());
    }

    public void testNfcReplacement2() {
        try {
            rb = new ReplaceBytes(target2, 1, Action.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where not expected", false);
        }

        NfcComm rep3 = rb.performAction(nfc3);

        assertTrue("Data change did not work", Arrays.equals(expected4, rep3.getData()));

        assertEquals("Old value was not preserved", rand1, rep3.getOldData());
    }

    public void testUidReplacement() {
        try {
            rb = new ReplaceBytes(target1, 1, Action.TARGET.ANTICOL, Action.ANTICOLFIELD.UID);
        } catch (Exception e) {
            assertTrue("Exception thrown where not expected", false);
        }

        NfcComm rep1 = rb.performAction(anticol1);
        NfcComm rep2 = rb.performAction(anticol2);
        NfcComm rep4 = rb.performAction(anticol4); // Ignore the single byte nonsense

        assertTrue("New value was not saved", Arrays.equals(expected1, rep1.getUid()));
        assertTrue("New value was not saved", Arrays.equals(expected2, rep2.getUid()));
        assertTrue("New value was not saved", Arrays.equals(expected3, rep4.getUid()));

        assertEquals("Old value was not preserved for target1", target1, rep1.getOldUid());
        assertEquals("Old value was not preserved for target2", target2, rep2.getOldUid());
        assertTrue("Old value was not preserved for target 3", Arrays.equals(rand1, rep4.getOldUid()));
    }

    public void testSakReplacement() {
        try {
            rb = new ReplaceBytes(target3, 0, Action.TARGET.ANTICOL, Action.ANTICOLFIELD.SAK);
        } catch (Exception e) {
            assertTrue("Exception thrown where not expected", false);
        }

        NfcComm rep1 = rb.performAction(anticol1);
        NfcComm rep2 = rb.performAction(anticol2);
        NfcComm rep3 = rb.performAction(anticol3);

        assertEquals("New value was not saved", target3, rep1.getSak());
        assertEquals("New value was not saved", target3, rep2.getSak());
        assertEquals("New value was not saved", target3, rep3.getSak());

        assertEquals("Old value was not preserved for target1", target1[0], rep1.getOldSak());
        assertEquals("Old value was not preserved for target2", target2[0], rep2.getOldSak());
        assertEquals("Old value was not preserved for target 3", target3, rep3.getOldSak());
    }


}
