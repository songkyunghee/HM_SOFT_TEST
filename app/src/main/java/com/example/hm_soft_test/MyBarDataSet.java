package com.example.hm_soft_test;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

public class MyBarDataSet extends BarDataSet {
    public MyBarDataSet(List<BarEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public int getColor(int index) {
        if(getEntryForIndex(index).getY() < 100) {
            return mColors.get(0);
        } else if(getEntryForIndex(index).getY() > 100 && getEntryForIndex(index).getY() < 200) {
            return mColors.get(1);
        } else {
            return mColors.get(2);
        }
    }
}
