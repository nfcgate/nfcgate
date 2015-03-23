package tud.seemuh.nfcgate.util.filter.conditional.test;

import junit.framework.TestCase;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.conditional.All;
import tud.seemuh.nfcgate.util.filter.conditional.And;
import tud.seemuh.nfcgate.util.filter.conditional.Conditional;
import tud.seemuh.nfcgate.util.filter.conditional.EndsWith;
import tud.seemuh.nfcgate.util.filter.conditional.StartsWith;

/**
 * Test for the AND Conditional operator
 */
public class AndTest extends TestCase {
    protected And and;
    protected Conditional cond1;
    protected Conditional cond2;
    protected Conditional cond3;

    protected byte[] pattern1 = new byte[] { (byte)0x00, (byte)0x10 };
    protected byte[] pattern2 = new byte[] { (byte)0x10, (byte)0x10 };

    protected byte[] target1 = new byte[] { (byte)0x00, (byte)0x00, (byte)0x10 };
    protected byte[] target2 = new byte[] { (byte)0x00, (byte)0x10, (byte)0x10 };

    protected NfcComm nfc1 = new NfcComm(NfcComm.Source.HCE, target1);
    protected NfcComm nfc2 = new NfcComm(NfcComm.Source.HCE, target2);

    protected NfcComm anticol1 = new NfcComm(target1, target1[0], target1, target1);
    protected NfcComm anticol2 = new NfcComm(target2, target2[0], target2, target2);

    public void testAllAndAll() {
        try {
            cond1 = new All(Conditional.TARGET.NFC);
            cond2 = new All(Conditional.TARGET.NFC);
            and = new And(cond1, cond2);
        } catch (Exception e) {
            assertTrue("Exception thrown where none was expected: " + e.toString(), false);
        }

        assertTrue("False where True was expected", and.applies(nfc1));
        assertTrue("False where True was expected", and.applies(nfc2));

        assertFalse("True where False was expected", and.applies(anticol1));
        assertFalse("True where False was expected", and.applies(anticol2));
    }

    public void testContradiction() {
        try {
            cond1 = new All(Conditional.TARGET.NFC);
            cond2 = new All(Conditional.TARGET.ANTICOL, Conditional.ANTICOLFIELD.ATQA);
            and = new And(cond1, cond2);
        } catch (Exception e) {
            assertTrue("Exception thrown where none was expected", false);
        }

        assertFalse("True where False was expected", and.applies(nfc1));
        assertFalse("True where False was expected", and.applies(nfc2));

        assertFalse("True where False was expected", and.applies(anticol1));
        assertFalse("True where False was expected", and.applies(anticol2));
    }

    public void testBeginsAndEnds() {
        try {
            cond1 = new StartsWith(pattern1, Conditional.TARGET.NFC);
            cond2 = new EndsWith(pattern2, Conditional.TARGET.NFC);
            and = new And(cond1, cond2);
        } catch (Exception e) {
            assertTrue("Exception thrown where none was expected", false);
        }

        assertFalse("True where false was expected", and.applies(nfc1));
        assertTrue("False where True was expected", and.applies(nfc2));
    }

    public void testBeginsAndEndsAndTrue() {
        try {
            // This test isn't useful per se, but it tests the behaviour for more than two inputs
            cond1 = new StartsWith(pattern1, Conditional.TARGET.NFC);
            cond2 = new EndsWith(pattern2, Conditional.TARGET.NFC);
            cond3 = new All(Conditional.TARGET.NFC);
            and = new And(cond1, cond2, cond3);
        } catch (Exception e) {
            assertTrue("Exception thrown where none was expected", false);
        }

        assertFalse("True where false was expected", and.applies(nfc1));
        assertTrue("False where True was expected", and.applies(nfc2));
    }
}
