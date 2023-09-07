package de.tu_darmstadt.seemoo.nfcgate.nfc.chip;

/**
 * Represents a guess of the NFC chip name
 */
public class NfcChipGuess {
    // guessed chip name or null
    public String chipName = null;
    // confidence in the guess, ranges 0-1 in percent
    public float confidence = 0;

    /**
     * Only set value if it improves the confidence
     *
     * @param value Set confidence to this value
     */
    public void improveConfidence(float value) {
        if (confidence < value)
            confidence = value;
    }

    @Override
    public String toString() {
        return String.format("{chipName: \"%s\", confidence: %.2f", chipName, confidence);
    }
}
