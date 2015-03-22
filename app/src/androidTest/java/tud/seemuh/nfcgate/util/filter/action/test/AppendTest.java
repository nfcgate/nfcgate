package tud.seemuh.nfcgate.util.filter.action.test;

import junit.framework.TestCase;

import java.util.Arrays;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterInitException;
import tud.seemuh.nfcgate.util.filter.action.Action;
import tud.seemuh.nfcgate.util.filter.action.Append;

/**
 * Testing the implementation of the Append action.
 */
public class AppendTest extends TestCase {
    Append ap;
    protected byte[] target1 = new byte[] {(byte)0x00};
    protected byte[] target2 = new byte[] {(byte)0x00, (byte)0x10};
    protected byte[] rand1 = new byte[] {(byte)0x31, (byte)0x42, (byte)0xff};

    protected byte[] expected1 = new byte[] {(byte)0x00, (byte)0x00};
    protected byte[] expected2 = new byte[] {(byte)0x00, (byte)0x10, (byte)0x00};
    protected byte[] expected3 = new byte[] {(byte)0x31, (byte)0x42, (byte)0xff, (byte)0x00};

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
            ap = new Append(target1, Action.TARGET.ANTICOL);
            assertEquals("No exception thrown on incorrect constructor", true, false);
        } catch (Exception e) {
            assertTrue("Incorrect Exception thrown", (e instanceof FilterInitException));
        }
    }

    public void testNfcAppend() {
        try {
            ap = new Append(target1, Action.TARGET.NFC);
        } catch (Exception e) {
            assertTrue("Exception thrown where not expected", false);
        }

        NfcComm rep1 = ap.performAction(nfc1);
        NfcComm rep2 = ap.performAction(nfc2);
        NfcComm rep3 = ap.performAction(nfc3);

        assertTrue("Data change did not work", Arrays.equals(expected1, rep1.getData()));
        assertTrue("Data change did not work", Arrays.equals(expected2, rep2.getData()));
        assertTrue("Data change did not work", Arrays.equals(expected3, rep3.getData()));

        assertEquals("Old value was not preserved", target1, rep1.getOldData());
        assertEquals("Old value was not preserved", target2, rep2.getOldData());
        assertEquals("Old value was not preserved", rand1, rep3.getOldData());
    }

}
