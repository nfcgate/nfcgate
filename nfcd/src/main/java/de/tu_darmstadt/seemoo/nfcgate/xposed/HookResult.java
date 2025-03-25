package de.tu_darmstadt.seemoo.nfcgate.xposed;

public enum HookResult {
    SUCCESS(0),
    ERROR_RETRY(1),
    ERROR_FATAL(2);

    HookResult(int x) {
        value = x;
    }

    final int value;

    public static HookResult fromValue(int value) {
        for (HookResult result : HookResult.values())
            if (result.value == value)
                return result;

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
