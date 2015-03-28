package tud.seemuh.nfcgate.util.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * A preference designed to allow the user to enter an integer value.
 * Source: https://stackoverflow.com/questions/3721358/preferenceactivity-save-value-as-integer/3755608#3755608
 */
public class IntEditTextPreference extends EditTextPreference {

    public IntEditTextPreference(Context context) {
        super(context);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.valueOf(value));
    }
}
