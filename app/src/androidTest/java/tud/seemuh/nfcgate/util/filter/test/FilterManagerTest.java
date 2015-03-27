package tud.seemuh.nfcgate.util.filter.test;

import junit.framework.TestCase;

import java.util.Arrays;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.Filter;
import tud.seemuh.nfcgate.util.filter.FilterManager;
import tud.seemuh.nfcgate.util.filter.action.Action;
import tud.seemuh.nfcgate.util.filter.action.ReplaceContent;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;
import tud.seemuh.nfcgate.util.filter.conditional.StartsWith;

/**
 * Test class for the FilterManager
 */
public class FilterManagerTest extends TestCase {
    protected byte[] pattern1 = new byte[] {(byte)0x10, (byte)0x00};
    protected byte[] pattern2 = new byte[] {(byte)0x00, (byte)0x10};

    protected byte[] message1 = new byte[] {(byte)0x10, (byte)0x00, (byte)0xff};
    protected byte[] message2 = new byte[] {(byte)0x00, (byte)0x10, (byte)0x42};
    protected byte[] message3 = new byte[] {(byte)0xff, (byte)0x4A, (byte)0x42};

    protected NfcComm nfc1 = new NfcComm(NfcComm.Source.HCE, message1);
    protected NfcComm nfc2 = new NfcComm(NfcComm.Source.HCE, message2);
    protected NfcComm nfc3 = new NfcComm(NfcComm.Source.HCE, message3);

    protected ReplaceContent rc1;
    protected StartsWith sw1;
    protected Filter filter1;
    protected ReplaceContent rc2;
    protected StartsWith sw2;
    protected Filter filter2;

    protected FilterManager mFilterManager;

    protected void setUp() {
        try {
            sw1 = new StartsWith(pattern1, Conditional.TARGET.NFC);
            rc1 = new ReplaceContent(message2, Action.TARGET.NFC);
            filter1 = new Filter(sw1, rc1);

            sw2 = new StartsWith(pattern2, Conditional.TARGET.NFC);
            rc2 = new ReplaceContent(message3, Action.TARGET.NFC);
            filter2 = new Filter(sw2, rc2);

            mFilterManager = new FilterManager();
        } catch (Exception e) {
            assertTrue("Exception where none should be", false);
        }
    }

    public void testHCERuleOrdering1() {
        mFilterManager.addHCEDataFilter(filter1);
        mFilterManager.addHCEDataFilter(filter2);
        NfcComm rep1 = mFilterManager.filterHCEData(nfc1);

        assertTrue("Resulting data is incorrect (nfc1)", Arrays.equals(rep1.getData(), message3));
        assertTrue("Old data was not preserved (nfc1)", Arrays.equals(rep1.getOldData(), message1));

        NfcComm rep2 = mFilterManager.filterHCEData(nfc2);

        assertTrue("Resulting data is incorrect (nfc2)", Arrays.equals(rep2.getData(), message3));
        assertTrue("Old data was not preserved (nfc2)", Arrays.equals(rep2.getOldData(), message2));

        NfcComm rep3 = mFilterManager.filterHCEData(nfc3);

        assertTrue("Resulting data is incorrect (nfc3)", Arrays.equals(rep3.getData(), message3));
        assertTrue("Old data was not preserved (nfc3)", Arrays.equals(rep3.getOldData(), message3));
    }

    public void testHCERuleOrdering2() {
        mFilterManager.addHCEDataFilter(filter2);
        mFilterManager.addHCEDataFilter(filter1);
        NfcComm rep1 = mFilterManager.filterHCEData(nfc1);

        assertTrue("Resulting data is incorrect (nfc1)", Arrays.equals(rep1.getData(), message2));
        assertTrue("Old data was not preserved (nfc1)", Arrays.equals(rep1.getOldData(), message1));

        NfcComm rep2 = mFilterManager.filterHCEData(nfc2);

        assertTrue("Resulting data is incorrect (nfc2)", Arrays.equals(rep2.getData(), message3));
        assertTrue("Old data was not preserved (nfc2)", Arrays.equals(rep2.getOldData(), message2));

        NfcComm rep3 = mFilterManager.filterHCEData(nfc3);

        assertTrue("Resulting data is incorrect (nfc3)", Arrays.equals(rep3.getData(), message3));
        assertTrue("Old data was not preserved (nfc3)", Arrays.equals(rep3.getOldData(), message3));
    }
}
