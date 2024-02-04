package de.tu_darmstadt.seemoo.nfcgate.db.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.db.AppDatabase;
import de.tu_darmstadt.seemoo.nfcgate.db.TagInfo;
import de.tu_darmstadt.seemoo.nfcgate.db.TagInfoDao;

public class TagInfoViewModel extends AndroidViewModel {
    private final LiveData<List<TagInfo>> mTagInfos;
    private final AppDatabase mAppDb;

    public TagInfoViewModel(@NonNull Application application) {
        super(application);

        mAppDb = AppDatabase.getDatabase(application);
        mTagInfos = mAppDb.tagInfoDao().getAll();
    }

    public LiveData<List<TagInfo>> getTagInfos() {
        return mTagInfos;
    }

    public void insert(TagInfo tagInfo) {
        new insertAsyncTask(mAppDb.tagInfoDao()).execute(tagInfo);
    }

    public void delete(TagInfo tagInfo) {
        new deleteAsyncTask(mAppDb.tagInfoDao()).execute(tagInfo);
    }

    private static class insertAsyncTask extends AsyncTask<TagInfo, Void, Void> {
        private final TagInfoDao mAsyncTaskDao;

        insertAsyncTask(TagInfoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TagInfo... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<TagInfo, Void, Void> {
        private final TagInfoDao mAsyncTaskDao;

        deleteAsyncTask(TagInfoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TagInfo... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }
}
