package tud.seemuh.nfcgate.util.filter.action.test;

import junit.framework.TestCase;

import java.util.Arrays;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.action.Action;
import tud.seemuh.nfcgate.util.filter.action.InsertBytes;

/**
 * Testing the implementation of the ReplaceMessage action.
 */
public class InsertBytesTest extends TestCase {
    InsertBytes ib;
    protected byte[] target1 = new byte[] {(byte)0x00};
    protected byte[] target2 = new byte[] {(byte)0x00, (byte)0x10};
    protected byte   target3 = (byte)0x12;

    protected byte[] rand1 = new byte[] {(byte)0x31, (byte)0x42, (byte)0xff};

    protected byte[] expected1 = new byte[] {(byte)0x00, (byte)0x00};
    protected byte[] expected2 = new byte[] {(byte)0x00, (byte)0x00, (byte)0x10};
    protected byte[] expected3 = new byte[] {(byte)0x31, (byte)0x00, (byte)0x42, (byte)0xff};

    protected byte[] expected4 = new byte[] {(byte)0x31, (byte)0x00, (byte)0x10, (byte)0x42, (byte)0xff};

    protected NfcComm nfc1;
    protected NfcComm nfc2;
    protected NfcComm nfc3;

    protected void setUp() {
        nfc1 = new NfcComm(NfcComm.Source.HCE, target1);
        nfc2 = new NfcComm(NfcComm.Source.HCE, target2);
        nfc3 = new NfcComm(NfcComm.Source.HCE, rand1);
    }

    public void testIncorrectConstructor1() {
        try {
            ib = new InsertBytes(target1, 0, Action.TARGET.ANTICOL);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testIncorrectConstructor5() {
        try {
            ib = new InsertBytes(target1, -1, Action.TARGET.NFC);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testNfcReplacement1() {
        try {
            ib = new InsertBytes(target1, 1, Action.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where not expected", false);
        }

        NfcComm rep1 = ib.performAction(nfc1);
        NfcComm rep2 = ib.performAction(nfc2);
        NfcComm rep3 = ib.performAction(nfc3);

        assertTrue("Data change did not work", Arrays.equals(expected1, rep1.getData()));
        assertTrue("Data change did not work", Arrays.equals(expected2, rep2.getData()));
        assertTrue("Data change did not work", Arrays.equals(expected3, rep3.getData()));

        assertEquals("Old value was not preserved", target1, rep1.getOldData());
        assertEquals("Old value was not preserved", target2, rep2.getOldData());
        assertEquals("Old value was not preserved", rand1, rep3.getOldData());
    }

    public void testNfcReplacement2() {
        try {
            ib = new InsertBytes(target2, 1, Action.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where not expected", false);
        }

        NfcComm rep3 = ib.performAction(nfc3);

        assertTrue("Data change did not work", Arrays.equals(expected4, rep3.getData()));

        assertEquals("Old value was not preserved", rand1, rep3.getOldData());
    }

    public void testTooLargeOffset() {
        try {
            ib = new InsertBytes(target1, 2, Action.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where not expected", false);
        }

        NfcComm rep1 = ib.performAction(nfc1);

        assertEquals("NFC data was modified", nfc1, rep1);
    }
}
