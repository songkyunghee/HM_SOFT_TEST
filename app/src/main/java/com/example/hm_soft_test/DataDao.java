package com.example.hm_soft_test;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import java.util.List;

@Dao
public interface DataDao {
    @Insert
    void insert(Data data);

//    @Query("SELECT * from data_table WHERE receiveData1 ORDER BY idx DESC LIMIT 1")
//    LiveData<List<Data>> getAllReceiveData1_Limit1();
//
//    @Query("SELECT * from data_table WHERE receiveData2 ORDER BY idx DESC LIMIT 1")
//    LiveData<List<Data>> getAllReceiveData2_Limit1();

    @Query("SELECT * from data_table ORDER BY idx DESC LIMIT 1")
    LiveData<List<Data>> getAllData_Limit1();

    @Query("SELECT * from data_table ORDER BY idx DESC")
    LiveData<List<Data>> getAllData();

    @Query("DELETE FROM data_table")
    void deleteAll();
}
