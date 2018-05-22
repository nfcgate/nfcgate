package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;

public class CloneFragment extends BaseFragment {
    View mCloneWaiting;
    TextView mCloneContent;
    ListView mCloneSaved;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clone, container, false);

        // setup
        mCloneWaiting = v.findViewById(R.id.clone_wait);
        mCloneContent = v.findViewById(R.id.clone_content);
        mCloneSaved = v.findViewById(R.id.clone_saved);

        setHasOptionsMenu(true);
        setCloneWait(false);

        return v;
    }

    @Override
    public String getTagName() {
        return "clone";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_clone, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clone:
                beginClone();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setCloneWait(boolean waiting) {
        mCloneWaiting.setVisibility(waiting ? ViewGroup.VISIBLE : ViewGroup.GONE);
        mCloneContent.setVisibility(waiting ? ViewGroup.GONE : ViewGroup.VISIBLE);
    }

    void onTagDiscovered() {
        // stop waiting and display data
        setCloneWait(false);
        setCloneContent("Found Tag");
    }

    void setCloneContent(String content) {
        mCloneContent.setText(content);
    }

    void beginClone() {
        setCloneWait(true);

        // TODO: debug code
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // simulate tag found
                onTagDiscovered();
            }
        }, 3000);
    }
}
