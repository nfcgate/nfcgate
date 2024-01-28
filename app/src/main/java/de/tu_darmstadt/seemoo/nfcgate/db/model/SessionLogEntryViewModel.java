package de.tu_darmstadt.seemoo.nfcgate.db.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

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
