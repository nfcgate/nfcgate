package tud.seemuh.nfcgate.db.model;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

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