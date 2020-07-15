package com.example.hm_soft_test;

import android.app.Activity;
import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class RealtimeLineChart {

    private Activity mActivity;
    private LineChart lineChart;


    public RealtimeLineChart(Activity activity, LineChart Chart) {
        mActivity=activity;
        lineChart=Chart;

    }

    public void lineChartSet() {
        lineChart.setDrawGridBackground(true);
        lineChart.setBackgroundColor(Color.BLACK);
        lineChart.setGridBackgroundColor(Color.BLACK);

        lineChart.getDescription().setEnabled(true);
        Description des = lineChart.getDescription();
        des.setEnabled(true);
        des.setText("Real-Time DATA");
        des.setTextSize(15f);
        des.setTextColor(Color.WHITE);

        lineChart.setTouchEnabled(false);

        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);

        lineChart.setAutoScaleMinMaxEnabled(true);

        lineChart.setPinchZoom(false);

        lineChart.getXAxis().setDrawGridLines(true);
        lineChart.getXAxis().setDrawAxisLine(false);

        lineChart.getXAxis().setEnabled(true);
        lineChart.getXAxis().setDrawGridLines(false);

        Legend l = lineChart.getLegend();
        l.setEnabled(true);
        l.setFormSize(10f);
        l.setTextSize(12F);
        l.setTextColor(Color.WHITE);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(mActivity.getResources().getColor((R.color.red)));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(mActivity.getResources().getColor(R.color.red));

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(true);

        lineChart.invalidate();


    }

    public void addEntry(int num1, int num2) {
        LineData data = lineChart.getData();
       // LineData data2 = lineChart.getData();

        if(data == null) {
            data = new LineData();
            lineChart.setData(data);
        }

//        if(data2 == null) {
//            data2 = new LineData();
//            lineChart.setData(data2);
//        }

        ILineDataSet set1 = data.getDataSetByIndex(0);
        ILineDataSet set2 = data.getDataSetByIndex(0);

        if(set1 == null) {
            set1 = createSet1();
            data.addDataSet(set1);
        }
        if(set2 == null) {
            set2 = createSet2();
            data.addDataSet(set2);
        }

        data.addEntry((new Entry((float)set1.getEntryCount(), (float)num1)),0);
        data.addEntry((new Entry((float)set2.getEntryCount(), (float)num2)),1);

        data.notifyDataChanged();
       // data2.notifyDataChanged();

        lineChart.notifyDataSetChanged();

        lineChart.setVisibleXRangeMaximum(150);

        lineChart.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);
    }

    private LineDataSet createSet1() {
        LineDataSet set = new LineDataSet(null, "Data1");
        set.setLineWidth(1f);
        set.setDrawValues(false);
        set.setValueTextColor(Color.GREEN);
        set.setColor(Color.GREEN);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);
        set.setHighLightColor(Color.rgb(190,190,190));

        return set;
    }

    private LineDataSet createSet2() {
        LineDataSet set = new LineDataSet(null, "Data2");
        set.setLineWidth(1f);
        set.setDrawValues(false);
        set.setValueTextColor(Color.RED);
        set.setColor(Color.RED);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircles(false);
        set.setHighLightColor(Color.rgb(190,190,190));

        return set;

    }


    public void displayGraph (final int datanum1, final int datanum2) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addEntry(datanum1, datanum2);
            }
        });
    }


}
