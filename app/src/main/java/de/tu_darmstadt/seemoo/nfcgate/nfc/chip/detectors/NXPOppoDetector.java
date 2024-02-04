package de.tu_darmstadt.seemoo.nfcgate.nfc.chip.detectors;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NXP NFC chip name detector.
 * Specifically made to parse the complex NXP config system of
 * devices in the Oppo family (including OnePlus).
 * Key points:
 * - Every device model is considered a "project"
 * - There are several potential ways to derive the "project name" of a model
 * - In one of the config dirs is a subfolder "nfc", containing the mapping file nfc_conf_ref
 *   as well as various configuration files with "target config" suffixes e.g. "a.conf_291812"
 * - One or more "project names" can be mapped to a "target config" in the mapping file
 * By finding and parsing the mapping file, we can use the derived project name of this device
 * to determine the target config, which points to a specific config file in the nfc subfolder.
 */
public class NXPOppoDetector extends NXPDetector {
    protected static class ConfigTable {
        protected static class Entry {
            public Entry(String tc, List<String> pns) {
                targetConfig = tc;
                projectNames = pns;
            }

            @Override
            public String toString() {
                StringBuilder result = new StringBuilder();
                result.append(targetConfig).append(":");

                for (String pn : projectNames)
                    result.append(pn).append(" ");

                return result.toString();
            }

            public final String targetConfig;
            public final List<String> projectNames;
        }

        public void add(String tc, List<String> pns) {
            mEntries.add(new Entry(tc, pns));
        }

        public String findTargetByProjectName(String pn) {
            for (Entry entry : mEntries)
                if (entry.projectNames.contains(pn))
                    return entry.targetConfig;

            return null;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();

            for (Entry entry : mEntries)
                result.append(entry.toString()).append("\n");

            return result.toString();
        }

        protected final List<Entry> mEntries = new ArrayList<>();
    }

    @Override
    protected List<String> getConfigDirs() {
        List<String> result = new ArrayList<>();

        // oppo family devices have a special nfc subdirectory in one of the config dirs
        for (String configDir : super.getConfigDirs())
            result.add(configDir + "nfc/");

        return result;
    }

    /**
     * Finds the nfc_conf_ref file in one of the nfc/ folders.
     * This file contains the nfc config file associations
     */
    String getConfRefPath() {
        List<String> candidates = findConfigs(Arrays.asList("nfc_conf_ref"));
        return !candidates.isEmpty() ? candidates.get(0) : null;
    }

    /**
     * Parse the nfc_conf_ref table from its path into a searchable table
     */
    ConfigTable parseTable(String confRefPath) {
        ConfigTable result = new ConfigTable();

        boolean rv = readFileLines(confRefPath, line -> {
            // ignore comments and empty lines in the file
            if (line.startsWith("#") || line.isEmpty())
                return true;

            // split "target config" from the space-separated "list of project names"
            String[] parts1 = line.split(":", 2);
            if (parts1.length != 2)
                return false;

            // split the "list of project names"
            String[] parts2 = parts1[1].split(" ");
            if (parts2.length == 0)
                return false;

            // clean the "project names" of region/operator constraints
            List<String> projectNames = new ArrayList<>();
            for (String projectName : parts2)
                projectNames.add(projectName.replaceFirst("(\\(.*\\))", ""));

            result.add(parts1[0], Arrays.asList(parts2));
            return true;
        });

        return rv ? result : null;
    }

    /**
     * Per nfc_conf_ref documentation, multiple sources could be used as project name
     * of this device. This method reads the sources and returns them in the correct order.
     */
    List<String> getProjectNameCandidates() {
        return Arrays.asList(
                getSystemProp("ro.separate.soft"),
                getSystemProp("ro.build.product"),
                getFileContents("/proc/oppoVersion/prjName"),
                getFileContents("/proc/oppoVersion/prjVersion")
        );
    }

    /**
     * Get config file suffix for the nfc subfolder for this device.
     */
    String getConfigSuffix() {
        // find path to nfc_conf_ref containing the mapping
        String confRefPath = getConfRefPath();
        if (confRefPath == null || confRefPath.isEmpty())
            return null;
        Log.d("NFCCONFIG", String.format("Got nfc_conf_ref at %s", confRefPath));

        // parse the mapping into a convenient table
        ConfigTable configTable = parseTable(confRefPath);
        if (configTable == null)
            return null;
        Log.d("NFCCONFIG", String.format("Got config table: %s", configTable));

        // for each project name that this device could be known under in the mapping
        for (String candidate : getProjectNameCandidates()) {
            Log.d("NFCCONFIG", String.format("Checking candidate %s", candidate));
            String targetConfig = configTable.findTargetByProjectName(candidate);

            // if a target config was found for one of the project names, return it
            if (targetConfig != null && !targetConfig.isEmpty())
                return targetConfig;
        }

        return null;
    }

    @Override
    protected List<String> getConfigFilenames() {
        List<String> result = super.getConfigFilenames();

        // add suffixed file in the nfc subfolder to the list, with the suffix for this device
        String configSuffix = getConfigSuffix();
        if (configSuffix != null) {
            Log.d("NFCCONFIG", String.format("Found suffix %s", configSuffix));
            result.add("libnfc-nxp.conf_" + configSuffix);
        }

        return result;
    }

    /**
     * Read file contents to string, losing line ending format.
     * All lines will be read as \n
     *
     * @param path Path to read from
     * @return The file contents decoded in default locale
     */
    protected static String getFileContents(String path) {
        StringBuilder builder = new StringBuilder();

        boolean rv = readFileLines(path, line -> {
            builder.append(line).append("\n");
            return true;
        });

        return rv ? builder.toString() : null;
    }
}
