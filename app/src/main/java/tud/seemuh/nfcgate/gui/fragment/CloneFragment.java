package tud.seemuh.nfcgate.gui.fragment;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import tud.seemuh.nfcgate.gui.Util;
import tud.seemuh.nfcgate.gui.model.TagInfoViewModel;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.nfc.config.ConfigBuilder;
import tud.seemuh.nfcgate.util.NfcComm;

public class CloneFragment extends BaseFragment implements NfcManager.Callback {
    View mCloneWaiting;
    TextView mCloneContent;
    ListView mCloneSaved;
    byte[] mCloneConfig;
    boolean mTagInfoDisplayed;

    private TagInfoViewModel mTagInfoViewModel;
    private ArrayAdapter<TagInfo> mTagInfoAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clone, container, false);

        // setup
        mCloneWaiting = v.findViewById(R.id.clone_wait);
        mCloneContent = v.findViewById(R.id.clone_content);
        mCloneSaved = v.findViewById(R.id.clone_saved);

        setHasOptionsMenu(true);
        setCloneWait(false);

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

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTagInfoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mCloneSaved.setAdapter(mTagInfoAdapter);
    }

    @Override
    public String getTagName() {
        return "clone";
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
        mCloneWaiting.setVisibility(waiting ? ViewGroup.VISIBLE : ViewGroup.GONE);
        mCloneContent.setVisibility(waiting ? ViewGroup.GONE : ViewGroup.VISIBLE);
    }

    void setCloneContent(ConfigBuilder config) {
        mCloneContent.setText(config.toString());
        mCloneConfig = config.build();

        setTagInfoDisplayed(true);
    }

    void beginClone() {
        setCloneWait(true);

        // add callback
        getNfc().setCallback(this);

        // enable clone mode
        getNfc().enableCloneMode();
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
                        mTagInfoViewModel.insert(new TagInfo(description, mCloneConfig));
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void notify(NfcComm data) {
        if (data.getType() == NfcComm.Type.Initial) {
            // stop waiting and display data
            setCloneWait(false);
            setCloneContent(data.getConfig());
        }
    }
}
