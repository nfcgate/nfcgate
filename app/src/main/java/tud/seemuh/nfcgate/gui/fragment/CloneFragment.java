package tud.seemuh.nfcgate.gui.fragment;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.TagInfo;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.gui.Util;
import tud.seemuh.nfcgate.db.model.TagInfoViewModel;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.nfc.modes.CloneMode;
import tud.seemuh.nfcgate.util.NfcComm;

public class CloneFragment extends Fragment {
    // UI references
    View mTagWaiting;
    TextView mCloneContent;
    ListView mCloneSaved;

    // clone data
    byte[] mCloneData;
    boolean mTagInfoDisplayed;

    // db data
    private TagInfoViewModel mTagInfoViewModel;
    private ArrayAdapter<TagInfo> mTagInfoAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clone, container, false);

        // setup
        mTagWaiting = v.findViewById(R.id.tag_wait);
        mCloneContent = v.findViewById(R.id.clone_content);
        mCloneSaved = v.findViewById(R.id.clone_saved);

        setHasOptionsMenu(true);
        beginClone();

        mTagInfoDisplayed = false;

        // setup db model
        mTagInfoViewModel = ViewModelProviders.of(this).get(TagInfoViewModel.class);
        mTagInfoViewModel.getTagInfos().observe(this, new Observer<List<TagInfo>>() {
            @Override
            public void onChanged(@Nullable List<TagInfo> tagInfos) {
                mTagInfoAdapter.clear();
                mTagInfoAdapter.addAll(tagInfos);
                mTagInfoAdapter.notifyDataSetChanged();
            }
        });

        // handlers
        mCloneSaved.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    // deleting inside models automatically updates adapter
                    mTagInfoViewModel.delete(mTagInfoAdapter.getItem(position));
                    return true;
                }
                return false;
            }
        });
        mCloneSaved.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    // load configuration of saved tag
                    final TagInfo item = mTagInfoAdapter.getItem(position);
                    getNfc().handleData(new NfcComm(item.getData()));
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTagInfoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mCloneSaved.setAdapter(mTagInfoAdapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        Util.setMenuItemEnabled(menu, R.id.action_save, mTagInfoDisplayed);
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
            case R.id.action_save:
                beginSave();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setTagInfoDisplayed(boolean tagInfoDisplayed) {
        mTagInfoDisplayed = tagInfoDisplayed;
        if (getActivity() != null)
            getActivity().invalidateOptionsMenu();
    }

    void setCloneWait(boolean waiting) {
        mTagWaiting.setVisibility(waiting ? ViewGroup.VISIBLE : ViewGroup.GONE);
        mCloneContent.setVisibility(waiting ? ViewGroup.GONE : ViewGroup.VISIBLE);
    }

    void setCloneContent(NfcComm data) {
        final ConfigBuilder builder = new ConfigBuilder(data.getData());
        mCloneContent.setText(builder.toString());
        mCloneData = data.toByteArray();

        setTagInfoDisplayed(true);
    }

    void beginClone() {
        // start waiting for tag
        setCloneWait(true);

        // start custom clone mode
        getNfc().startMode(new UICloneMode());
    }

    private void beginSave() {
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(getContext())
            .setTitle("Enter a description")
            .setView(input)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String description = input.getText().toString();

                    if (!description.isEmpty())
                        mTagInfoViewModel.insert(new TagInfo(description, mCloneData));
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public NfcManager getNfc() {
        return ((MainActivity) getActivity()).getNfc();
    }

    class UICloneMode extends CloneMode {
        @Override
        public void onData(final NfcComm data) {
            super.onData(data);

            FragmentActivity activity = getActivity();
            if (activity != null && data.isInitial()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // stop waiting and display data
                        setCloneWait(false);
                        setCloneContent(data);
                    }
                });
            }
        }
    }
}
