package de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors;

import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.NfcChipGuess;

/**
 * Common interface for all NFCC detectors
 */
public interface INfcChipDetector {
    List<NfcChipGuess> tryDetect();
}
