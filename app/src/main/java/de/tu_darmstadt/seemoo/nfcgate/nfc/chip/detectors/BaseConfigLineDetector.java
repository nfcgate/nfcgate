package de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.nfc.chip.NfcChipGuess;

/**
 * Base class for all NFC chip detectors using line-based configuration files
 */
public abstract class BaseConfigLineDetector implements INfcChipDetector {
    // directories used by NFC stack to search for configuration files
    private final String[] configDirs = new String[] {
            "/product/etc/",
            "/odm/etc/",
            "/vendor/etc/",
            "/system_ext/etc/",
            "/etc/",
    };

    @Override
    public List<NfcChipGuess> tryDetect() {
        List<NfcChipGuess> result = new ArrayList<>();

        for (String configFile : findConfigs(getConfigFilenames())) {
            NfcChipGuess guess = readConfig(configFile);

            // ignore confirmed misses
            if (guess != null) {
                Log.d("NFCCONFIG", String.format("Guess %s from %s", guess, configFile));
                result.add(guess);
            }
        }

        return result;
    }

    /**
     * Search standard config dirs and return all paths resulting from dir+fileName which exist
     *
     * @param fileNames List of filenames to check for existence in default config locations
     * @return List of existing configuration file paths
     */
    protected List<String> findConfigs(List<String> fileNames) {
        List<String> result = new ArrayList<>();

        for (String dir : getConfigDirs()) {
            for (String fileName : fileNames) {
                String path = dir + fileName;

                if (fileExists(path))
                    result.add(path);
            }
        }

        return result;
    }

    /**
     * Reads given config, triggers onLine for every line in the config
     *
     * @return A guess at the NFC chip name or null
     */
    protected NfcChipGuess readConfig(String path) {
        final NfcChipGuess result = new NfcChipGuess();

        if (!readFileLines(path, line -> onLine(line, result)))
            return null;

        return result;
    }

    protected interface ILineProcessor {
        boolean processLine(String line);
    }

    /**
     * Reads a file line-by-line, passing every line to the processor.
     * Return false from processing a line to indicate an error
     *
     * @param path Path to the file to read
     * @param processor Processor used for every line in the file
     * @return True if every line was successfully processed
     */
    protected static boolean readFileLines(String path, ILineProcessor processor) {
        File file = new File(path);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line; (line = br.readLine()) != null; )
                    if (!processor.processLine(line.trim()))
                        return false;
            }
            catch (IOException ignored) { }
        }

        return true;
    }

    /**
     * Splits config line, e.g. "a=b", "a =\"b\"", " a =  b  "
     * into (a, b) pair
     *
     * @return Pair of key, value or null if line is not in the expected format
     */
    protected static Pair<String, String> splitConfigLine(String line) {
        String[] parts = line.split("=", 2);

        if (parts.length == 2)
            return new Pair<>(parts[0].trim(),
                    parts[1].trim().replaceAll("(^\")|(\"$)", ""));

        return null;
    }

    /**
     * Checks if file exists
     */
    protected static boolean fileExists(String fileName) {
        return new File(fileName).exists();
    }

    protected abstract List<String> getConfigFilenames();
    protected List<String> getConfigDirs() {
        return Arrays.asList(configDirs);
    }
    protected abstract boolean onLine(String line, NfcChipGuess guess);
}
