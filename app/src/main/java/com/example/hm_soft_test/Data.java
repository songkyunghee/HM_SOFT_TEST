package com.example.hm_soft_test;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "data_table")
public class Data {
    @PrimaryKey(autoGenerate = true)
    public int idx = 0;

    public int receiveData1;

    public int receiveData2;

    public void setReceiveData1(int receiveData1) {
        this.receiveData1 = receiveData1;
    }

    public void setReceiveData2(int receiveData2) {
        this.receiveData2 = receiveData2;
    }
}
