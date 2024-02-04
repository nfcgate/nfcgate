package de.tu_darmstadt.seemoo.nfcgate.gui.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CustomArrayAdapter<T> extends ArrayAdapter<T> {
    private final int mResource;

    public CustomArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return convertView != null ? convertView :
                LayoutInflater.from(getContext()).inflate(mResource, null);
    }
}
