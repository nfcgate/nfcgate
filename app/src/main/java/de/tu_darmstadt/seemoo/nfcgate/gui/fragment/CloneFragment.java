package de.tu_darmstadt.seemoo.nfcgate.gui.fragment;

import android.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.R;
import de.tu_darmstadt.seemoo.nfcgate.db.TagInfo;
import de.tu_darmstadt.seemoo.nfcgate.db.model.TagInfoViewModel;
import de.tu_darmstadt.seemoo.nfcgate.gui.component.StatusBanner;
import de.tu_darmstadt.seemoo.nfcgate.nfc.NfcManager;
import de.tu_darmstadt.seemoo.nfcgate.nfc.config.ConfigBuilder;
import de.tu_darmstadt.seemoo.nfcgate.nfc.modes.CloneMode;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class CloneFragment extends BaseFragment {
    // UI references
    View mTagWaiting;
    ImageView mCloneType;
    TextView mCloneContent;
    ListView mCloneSaved;
    StatusBanner mStatusBanner;

    // clone data
    byte[] mCloneData;
    boolean mTagInfoDisplayed = false;

    // db data
    private TagInfoViewModel mTagInfoViewModel;
    private ArrayAdapter<TagInfo> mTagInfoAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clone, container, false);

        // setup
        mTagWaiting = v.findViewById(R.id.tag_wait);
        mCloneType = v.findViewById(R.id.type);
        mCloneContent = v.findViewById(R.id.data);
        mCloneSaved = v.findViewById(R.id.clone_saved);
        mStatusBanner = new StatusBanner(getMainActivity());

        setHasOptionsMenu(true);
        beginClone();

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
                    getNfc().handleData(false, new NfcComm(item.getData()));
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

        getNfc().setStatusChangedHandler(new NfcManager.StatusChangedListener() {
            @Override
            public void onChange() {
                mStatusBanner.reset();

                // show warning if xposed module does not respond
                if (!NfcManager.isModuleLoaded() || !getNfc().isHookEnabled())
                    mStatusBanner.setWarning(getString(R.string.error_xposed));

                // show error if NFC is disabled
                if (!getNfc().isEnabled())
                    mStatusBanner.setError(getString(R.string.error_nfc_disabled));
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        setSaveEnabled(menu, mTagInfoDisplayed);
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

    private void setSaveEnabled(Menu menu, boolean enabled) {
        MenuItem item = menu.findItem(R.id.action_save);
        if (item != null) {
            item.setEnabled(enabled);
            item.getIcon().mutate().setAlpha(enabled ? 255 : 130);
        }
    }

    private void setTagInfoDisplayed(boolean tagInfoDisplayed) {
        mTagInfoDisplayed = tagInfoDisplayed;
        getActivity().invalidateOptionsMenu();
    }

    void setCloneWait(boolean waiting) {
        setTagInfoDisplayed(!waiting);
        mTagWaiting.setVisibility(waiting ? ViewGroup.VISIBLE : ViewGroup.GONE);
        mCloneContent.setVisibility(waiting ? ViewGroup.GONE : ViewGroup.VISIBLE);
        mCloneType.setVisibility(waiting ? ViewGroup.GONE : ViewGroup.VISIBLE);
    }

    void setCloneContent(NfcComm data) {
        mCloneType.setImageResource(data.isCard() ? R.drawable.ic_tag_grey_60dp : R.drawable.ic_reader_grey_60dp);
        mCloneContent.setText(new ConfigBuilder(data.getData()).toString());
        mCloneData = data.toByteArray();
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
            .setTitle(getString(R.string.clone_save_title))
            .setView(input)
            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String description = input.getText().toString();

                    if (!description.isEmpty())
                        mTagInfoViewModel.insert(new TagInfo(description, mCloneData));
                }
            })
            .setNegativeButton(getString(R.string.button_cancel), null)
            .show();
    }

    class UICloneMode extends CloneMode {
        @Override
        public void onData(boolean isForeign, final NfcComm data) {
            super.onData(isForeign, data);

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
