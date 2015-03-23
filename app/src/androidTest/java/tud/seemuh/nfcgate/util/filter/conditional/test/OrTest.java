package tud.seemuh.nfcgate.util.filter.conditional.test;

import junit.framework.TestCase;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.conditional.All;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;
import tud.seemuh.nfcgate.util.filter.conditional.EndsWith;
import tud.seemuh.nfcgate.util.filter.conditional.Or;
import tud.seemuh.nfcgate.util.filter.conditional.StartsWith;

/**
 * Test the OR conditional operator
 */
public class OrTest extends TestCase {
    protected Or or;
    protected Conditional cond1;
    protected Conditional cond2;
    protected Conditional cond3;

    protected byte[] pattern1 = new byte[] { (byte)0x00, (byte)0x10 };
    protected byte[] pattern2 = new byte[] { (byte)0x10, (byte)0x10 };

    protected byte[] target1 = new byte[] { (byte)0x00, (byte)0x00, (byte)0x10 };
    protected byte[] target2 = new byte[] { (byte)0x00, (byte)0x10, (byte)0x10 };
    protected byte[] target3 = new byte[] { (byte)0x00, (byte)0x10, (byte)0x00 };

    protected NfcComm nfc1 = new NfcComm(NfcComm.Source.HCE, target1);
    protected NfcComm nfc2 = new NfcComm(NfcComm.Source.HCE, target2);
    protected NfcComm nfc3 = new NfcComm(NfcComm.Source.HCE, target3);

    protected NfcComm anticol1 = new NfcComm(target1, target1[0], target1, target1);
    protected NfcComm anticol2 = new NfcComm(target2, target2[0], target2, target2);

    public void testAllOrAll() {
        try {
            cond1 = new All(Conditional.TARGET.NFC);
            cond2 = new All(Conditional.TARGET.NFC);
            or = new Or(cond1, cond2);
        } catch (Exception e) {
            assertTrue("Exception thrown where none was expected", false);
        }

        assertTrue("False where True was expected", or.applies(nfc1));
        assertTrue("False where True was expected", or.applies(nfc2));
        assertTrue("False where True was expected", or.applies(nfc3));

        assertFalse("True where False was expected", or.applies(anticol1));
        assertFalse("True where False was expected", or.applies(anticol2));
    }

    public void testTautology() {
        try {
            cond1 = new All(Conditional.TARGET.NFC);
            cond2 = new All(Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.ATQA);
            or = new Or(cond1, cond2);
        } catch (Exception e) {
            assertTrue("Exception thrown where none was expected", false);
        }

        assertTrue("True where False was expected", or.applies(nfc1));
        assertTrue("True where False was expected", or.applies(nfc2));
        assertTrue("True where False was expected", or.applies(nfc3));

        assertTrue("True where False was expected", or.applies(anticol1));
        assertTrue("True where False was expected", or.applies(anticol2));
    }

    public void testBeginsOrEnds() {
        try {
            cond1 = new StartsWith(pattern1, Conditional.TARGET.NFC);
            cond2 = new EndsWith(pattern2, Conditional.TARGET.NFC);
            or = new Or(cond1, cond2);
        } catch (Exception e) {
            assertTrue("Exception thrown where none was expected", false);
        }

        assertFalse("True where false was expected", or.applies(nfc1));
        assertTrue("False where True was expected", or.applies(nfc2));
        assertTrue("False where True was expected", or.applies(nfc3));
    }

    public void testBeginsOrEndsOrTrue() {
        try {
            cond1 = new StartsWith(pattern1, Conditional.TARGET.NFC);
            cond2 = new EndsWith(pattern2, Conditional.TARGET.NFC);
            cond3 = new All(Conditional.TARGET.NFC);
            or = new Or(cond1, cond2, cond3);
        } catch (Exception e) {
            assertTrue("Exception thrown where none was expected", false);
        }

        assertTrue("False where true was expected", or.applies(nfc1));
        assertTrue("False where True was expected", or.applies(nfc2));
        assertTrue("False where True was expected", or.applies(nfc3));
    }
}
