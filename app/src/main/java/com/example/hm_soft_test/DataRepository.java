package com.example.hm_soft_test;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class DataRepository {

    private DataDao mDataDao;
    private LiveData<List<Data>> mAllData;
    private LiveData<List<Data>> mAllData_Limit1;
    private LiveData<List<Data>> mAllReceiveData1_Limit1;
    private LiveData<List<Data>> mAllReceiveData2_Limit1;

    public DataRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);

        mDataDao = db.dataDao();

        mAllData = mDataDao.getAllData();
        mAllData_Limit1 = mDataDao.getAllData_Limit1();
//        mAllReceiveData1_Limit1 = mDataDao.getAllReceiveData1_Limit1();
//        mAllReceiveData2_Limit1 = mDataDao.getAllReceiveData2_Limit1();

    }

    public LiveData<List<Data>> getAllData() {
        return mAllData;
    }

    public LiveData<List<Data>> getmAllData_Limit1() {
        return mAllData_Limit1;
    }

//    public LiveData<List<Data>> getmAllReceiveData1_Limit1() {
//        return mAllReceiveData1_Limit1;
//    }
//
//    public LiveData<List<Data>> getmAllReceiveData2_Limit1() {
//        return mAllReceiveData2_Limit1;
//    }

    public void insert(Data data) {
        new isnertAsyncTask(mDataDao).execute(data);
    }

    public void deleteAll() {
        new deleteAllAsyncTask(mDataDao).execute();
    }

    private static class isnertAsyncTask extends AsyncTask<Data, Void, Void> {
        private DataDao mAsyncTaskDao;

        isnertAsyncTask(DataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Data... data) {
            mAsyncTaskDao.insert(data[0]);
            return null;
        }
    }

    private static class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private DataDao mAsyncTaskDao;

        deleteAllAsyncTask(DataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }
}
