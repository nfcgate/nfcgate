package tud.seemuh.nfcgate.gui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.nfc.hce.DaemonConfiguration;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.db.CloneListItem;
import tud.seemuh.nfcgate.util.db.CloneListStorage;
import tud.seemuh.nfcgate.util.sink.SinkInitException;
import tud.seemuh.nfcgate.util.sink.SinkManager;
import tud.seemuh.nfcgate.xposed.Native;

public class CloneFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private final static String TAG = "CloneFragment";

    private static CloneFragment mFragment;
    public NfcManager mNfcManager = NfcManager.getInstance();
    private SinkManager mSinkManager;
    private BlockingQueue<NfcComm> mSinkManagerQueue = new LinkedBlockingQueue<NfcComm>();

    private TextView mCurrUID;
    private Switch mToggleCloneMode;
    private Switch mPinUID;

    private Context mContext;
    private Button mSaveButton;
    private ListView mListView;

    //tell other classes about the status of the clone mode
    private boolean mCloneModeEnabled = false;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = inflater.getContext();
        final View v = inflater.inflate(R.layout.fragment_clone, container, false);
        mCurrUID = (TextView) v.findViewById(R.id.cloned_uid);
        mToggleCloneMode = (Switch) v.findViewById(R.id.btnSwitchCloneMode);
        mToggleCloneMode.setOnCheckedChangeListener(this);

        mPinUID = (Switch) v.findViewById(R.id.btnSwitchPinUID);
        mPinUID.setOnCheckedChangeListener(this);
        mPinUID.setClickable(false);

        mListView = (ListView) v.findViewById(R.id.savedList);

        mSaveButton = (Button) v.findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSaveButton.setVisibility(View.INVISIBLE);

                final EditText input = new EditText(mContext);

                new AlertDialog.Builder(mContext)
                    .setTitle("Save Tag")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable value = input.getText();
                            CloneListStorage storage = new CloneListStorage(mContext);
                            storage.add(new CloneListItem(RelayFragment.getInstance().mNfcManager.getAnticolData(), value.toString()));
                            refreshList();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
            }
        });



        refreshList();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                CloneListItem item = (CloneListItem) mListView.getAdapter().getItem(pos);
                RelayFragment.getInstance().mNfcManager.setAnticolData(item.getAnticolData());
                Toast.makeText(mContext, "Tag loaded: " + item.toString(), Toast.LENGTH_LONG).show();
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, final int pos, long id) {
                final CharSequence[] items = {"Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int _p) {
                        CloneListStorage storage = new CloneListStorage(mContext);
                        CloneListItem item = (CloneListItem) mListView.getAdapter().getItem(pos);
                        storage.delete(item);
                        refreshList();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });


        return v;
    }

    private void refreshList() {
        CloneListStorage storage = new CloneListStorage(mContext);
        final ArrayAdapter<CloneListItem> adapter = new ArrayAdapter<CloneListItem>(mContext,
                android.R.layout.simple_list_item_1, android.R.id.text1, storage.getAll());

        // Assign adapter to ListView
        mListView.setAdapter(adapter);
    }


    public boolean isCloneModeEnabled() {
        return mCloneModeEnabled;
    }

    public boolean isPinUID() {
        return mPinUID.isChecked();
    }

    public static CloneFragment getInstance() {

        if(mFragment == null) {
            mFragment = new CloneFragment();
        }

        return mFragment;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.btnSwitchCloneMode:
                if (isChecked) {
                    //set sink
                    try {
                        mSinkManager = new SinkManager(mSinkManagerQueue);
                        mNfcManager.setSinkManager(mSinkManager, mSinkManagerQueue);
                        mNfcManager.getSinkManager().addSink(SinkManager.SinkType.DISPLAY_TEXTVIEW, mCurrUID, true);

                        //the start method knows when there is already a thread running
                        mNfcManager.start();
                    } catch (SinkInitException e) {
                        // Do nothing.
                    }
                    mCloneModeEnabled = true;

                    mPinUID.setClickable(true);
                } else {
                    //remove + reset sink
                    mNfcManager.unsetSinkManager();
                    mNfcManager.shutdown();
                    mCloneModeEnabled = false;

                    mPinUID.setClickable(false);
                    mPinUID.setChecked(false);
                }
                break;
            case R.id.btnSwitchPinUID:
                if(isChecked) {
                    Log.i(TAG, "onClick(): PinUID on");
                    DaemonConfiguration.getInstance().disablePolling();
                } else {
                    Log.i(TAG, "onClick(): PinUID off");
                    DaemonConfiguration.getInstance().enablePolling();
                }
                break;
        }
    }

    public void onTagDiscoveredCommon(Tag tag) {
        if(mCloneModeEnabled) {
            //this call notifies the TextSink 2x: ok here, we override it anyway
            RelayFragment.getInstance().mNfcManager.setAnticolData(RelayFragment.getInstance().mNfcManager.getAnticolData());
            mSaveButton.setVisibility(View.VISIBLE);

            // pin uid as soon as tag was discovered
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getView().findViewById(R.id.btnSwitchPinUID).performClick();
                }
            });
        }
    }
}
