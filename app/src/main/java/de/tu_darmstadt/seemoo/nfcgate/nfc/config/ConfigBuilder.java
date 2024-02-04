package de.tu_darmstadt.seemoo.nfcgate.nfc.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a NCI config stream.
 * Parses an existing stream or builds a new one from options.
 */
public class ConfigBuilder {
    private final List<ConfigOption> mOptions = new ArrayList<>();

    public ConfigBuilder() { }

    public ConfigBuilder(byte[] config) {
        parse(config);
    }

    public void add(OptionType ID, byte[] data) {
        mOptions.add(new ConfigOption(ID, data));
    }

    public void add(OptionType ID, byte data) {
        mOptions.add(new ConfigOption(ID, data));
    }

    public void add(ConfigOption option) {
        mOptions.add(option);
    }

    public List<ConfigOption> getOptions() {
        return mOptions;
    }

    private void parse(byte[] config) {
        mOptions.clear();
        int index = 0;

        while(index + 2 < config.length) {
            byte type = config[index];
            byte length = config[index + 1];

            byte[] data = new byte[length];
            System.arraycopy(config, index + 2, data, 0, length);

            add(OptionType.fromType(type), data);
            index += length + 2;
        }
    }

    public byte[] build() {
        int length = 0;

        for (ConfigOption option : mOptions)
            length += option.len() + 2;

        byte[] data = new byte[length];
        int offset = 0;

        for (ConfigOption option : mOptions) {
            option.push(data, offset);
            offset += option.len() + 2;
        }

        return data;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (ConfigOption option : mOptions) {
            if (result.length() > 0)
                result.append("\n");

            result.append(option.toString());
        }

        return result.toString();
    }
}
