package de.tu_darmstadt.seemoo.nfcgate.db.model;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SessionLogEntryViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application mApplication;
    private long mSessionLog;

    public SessionLogEntryViewModelFactory(Application application, long sessionLog) {
        mApplication = application;
        mSessionLog = sessionLog;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new SessionLogEntryViewModel(mApplication, mSessionLog);
    }
}