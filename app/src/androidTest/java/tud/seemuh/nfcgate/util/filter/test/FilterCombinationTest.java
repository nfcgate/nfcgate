package tud.seemuh.nfcgate.util.filter.test;

import junit.framework.TestCase;

import java.util.Arrays;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.Filter;
import tud.seemuh.nfcgate.util.filter.action.Action;
import tud.seemuh.nfcgate.util.filter.action.ReplaceContent;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;
import tud.seemuh.nfcgate.util.filter.conditional.StartsWith;

/**
 * Test cases for the combination of Conditionals and Actions into Filters
 */
public class FilterCombinationTest extends TestCase {
    protected byte[] pattern = new byte[] {(byte)0x10, (byte)0x00};

    protected byte[] message1 = new byte[] {(byte)0x10, (byte)0x00, (byte)0xff};
    protected byte[] message2 = new byte[] {(byte)0x00, (byte)0x10, (byte)0x42};

    protected NfcComm nfc1 = new NfcComm(NfcComm.Source.HCE, message1);
    protected NfcComm nfc2 = new NfcComm(NfcComm.Source.HCE, message2);

    protected ReplaceContent rc;
    protected StartsWith sw;
    protected Filter filter;

    protected void setUp() {
        try {
            sw = new StartsWith(pattern, Conditional.TARGET.NFC);
            rc = new ReplaceContent(message2, Action.TARGET.NFC);
        } catch (Exception e) {
            assertFalse("Exception where none should be thrown", true);
        }
    }

    public void testFilterAssembly() {
        filter = new Filter(sw, rc);
    }

    public void testFilterMatchingInput() {
        filter = new Filter(sw, rc);
        NfcComm rep1 = filter.filter(nfc1);
        assertTrue("Message was not replaced correctly", Arrays.equals(rep1.getData(), message2));
        assertTrue("Old message state was not preserved", Arrays.equals(rep1.getOldData(), message1));
        assertTrue("NfcComm object claims to have not been changed", rep1.isChanged());
    }

    public void testFilterNonMatchingInput() {
        filter = new Filter(sw, rc);
        NfcComm rep1 = filter.filter(nfc2);
        assertTrue("Message was replaced, but should not have been", Arrays.equals(rep1.getData(), message2));
        assertTrue("Old message state was not preserved", Arrays.equals(rep1.getOldData(), message2));
        assertFalse("NfcComm object claims to have been changed", rep1.isChanged());
    }
}
