package de.tu_darmstadt.seemoo.nfcgate.db.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import de.tu_darmstadt.seemoo.nfcgate.db.AppDatabase;
import de.tu_darmstadt.seemoo.nfcgate.db.SessionLogJoin;

public class SessionLogEntryViewModel extends AndroidViewModel {
    private final LiveData<SessionLogJoin> mSession;

    public SessionLogEntryViewModel(@NonNull Application application, long sessionid) {
        super(application);

        mSession = AppDatabase.getDatabase(application).sessionLogJoinDao().get(sessionid);
    }

    public LiveData<SessionLogJoin> getSession() {
        return mSession;
    }
}
