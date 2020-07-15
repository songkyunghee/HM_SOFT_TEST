package com.example.hm_soft_test;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


import java.util.List;

public class DataViewModel extends AndroidViewModel {
    private DataRepository mRepository;
    private LiveData<List<Data>> mAllData;
    private LiveData<List<Data>> mAllData_Limit1;
    private LiveData<List<Data>> mAllReceiveData1_Limit1;
    private LiveData<List<Data>> mAllReceiveData2_Limit1;

    public DataViewModel(Application application) {
        super(application);
        mRepository = new DataRepository(application);
        mAllData = mRepository.getAllData();
        mAllData_Limit1 = mRepository.getmAllData_Limit1();
//        mAllReceiveData1_Limit1 = mRepository.getmAllReceiveData1_Limit1();
//        mAllReceiveData2_Limit1 = mRepository.getmAllReceiveData2_Limit1();
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
        mRepository.insert(data);
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }
}
