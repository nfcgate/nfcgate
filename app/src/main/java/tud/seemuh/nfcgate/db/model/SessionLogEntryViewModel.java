package tud.seemuh.nfcgate.db.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.SessionLogJoin;

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
