package de.tu_darmstadt.seemoo.nfcgate.nfc.chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors.BRCMDetector;
import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors.INfcChipDetector;
import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors.NXPDetector;
import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors.NXPOppoDetector;
import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors.STDetector;
import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors.SamsungDetector;

public class NfcChip {

    // Prevent creating this class, it only has static methods.
    private NfcChip() {}

    /**
     * Detects the NFC chip on this device
     *
     * @return The name of the chip or null
     */
    public static String detect() {
        NfcChipGuess best = new NfcChipGuess();

        // search guesses in order
        for (NfcChipGuess guess : collectGuesses()) {
            if (guess.confidence > best.confidence)
                best = guess;
        }

        if (best.chipName != null && !best.chipName.isEmpty())
            return best.chipName;

        return null;
    }

    private static List<NfcChipGuess> collectGuesses() {
        List<NfcChipGuess> result = new ArrayList<>();
        List<INfcChipDetector> detectors = Arrays.asList(
                new BRCMDetector(),
                new NXPDetector(),
                new NXPOppoDetector(),
                new SamsungDetector(),
                new STDetector()
        );

        for (INfcChipDetector detector : detectors)
            result.addAll(detector.tryDetect());

        return result;
    }
}
