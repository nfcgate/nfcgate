package tud.seemuh.nfcgate.gui.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.db.SessionLogJoin;

public class SessionLogEntryViewModel extends AndroidViewModel {
    private final LiveData<SessionLogJoin> mSession;
    private AppDatabase mAppDb;

    public SessionLogEntryViewModel(@NonNull Application application, long sessionid) {
        super(application);

        mAppDb = AppDatabase.getDatabase(application);
        mSession = mAppDb.sessionLogJoinDao().get(sessionid);
    }

    public LiveData<SessionLogJoin> getSession() {
        return mSession;
    }
}
