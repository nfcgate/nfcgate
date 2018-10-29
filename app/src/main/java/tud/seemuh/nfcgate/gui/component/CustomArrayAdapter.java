package tud.seemuh.nfcgate.gui.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CustomArrayAdapter<T> extends ArrayAdapter<T> {
    private int mResource;

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
