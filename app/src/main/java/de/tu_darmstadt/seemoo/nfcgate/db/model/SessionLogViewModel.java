package de.tu_darmstadt.seemoo.nfcgate.db.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.db.AppDatabase;
import de.tu_darmstadt.seemoo.nfcgate.db.SessionLog;

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
