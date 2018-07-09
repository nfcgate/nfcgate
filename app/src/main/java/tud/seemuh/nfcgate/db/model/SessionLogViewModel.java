package tud.seemuh.nfcgate.db.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.SessionLog;

public class SessionLogViewModel extends AndroidViewModel {
    private final LiveData<List<SessionLog>> mSessionLog;

    public SessionLogViewModel(@NonNull Application application) {
        super(application);

        mSessionLog = AppDatabase.getDatabase(application).sessionLogDao().getAll();
    }

    public LiveData<List<SessionLog>> getSessionLogs() {
        return mSessionLog;
    }
}
