package tud.seemuh.nfcgate.gui.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.db.SessionLogDao;
import tud.seemuh.nfcgate.db.TagInfo;
import tud.seemuh.nfcgate.db.TagInfoDao;

public class SessionLogViewModel extends AndroidViewModel {
    private final LiveData<List<SessionLog>> mSessionLog;
    private AppDatabase mAppDb;

    public SessionLogViewModel(@NonNull Application application) {
        super(application);

        mAppDb = AppDatabase.getDatabase(application);
        mSessionLog = mAppDb.sessionLogDao().getAll();
    }

    public LiveData<List<SessionLog>> getTagInfos() {
        return mSessionLog;
    }

    public void insert(SessionLog tagInfo) {
        new insertAsyncTask(mAppDb.sessionLogDao()).execute(tagInfo);
    }

    private static class insertAsyncTask extends AsyncTask<SessionLog, Void, Void> {

        private SessionLogDao mAsyncTaskDao;

        insertAsyncTask(SessionLogDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SessionLog... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
