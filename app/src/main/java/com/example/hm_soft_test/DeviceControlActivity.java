package com.example.hm_soft_test;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private final static String TIMER_TAG = "TIMER";
    private final static String COUNT_TAG = "COUNT";
    private final static String AVERAGE_TAG = "AVERAGE";
;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private TextView mGraphData;
    private Button mDataBtn;
    private Button mDataDeleteBtn;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;

    //버튼 제어, 쓰레드 제어 변수들
    Boolean flag;
    Boolean running;
    Boolean timer_control;
    int count = 0;

    private int hex_to_int[];
    List<Integer> dataValue = new ArrayList<>();

    public DataViewModel mDataViewModel;
    private List<Data> mData;

    private RealtimeLineChart linechartService = null;
    LineChart lineChart;

    int a, b;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if(!mBluetoothLeService.initialize()) {
                Logs.e(TAG,"Unable to initialize Bluetooth");
                finish();
            }

            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
            } else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                findGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if(mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        flag = true;
        running = true;
        timer_control = true;

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        ((TextView)findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView)findViewById(R.id.connection_state);
        mDataField = (TextView)findViewById(R.id.data_value);
        mGraphData = (TextView)findViewById(R.id.graph_data);
        mDataBtn = (Button)findViewById(R.id.btn_send);
        mDataDeleteBtn = (Button)findViewById(R.id.btn_delete_data);

        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);

        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this,"SDcard is not mounted", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int checkResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if(checkResult == PackageManager.PERMISSION_DENIED) {
                String[] permisions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permisions, 100);
                return;
            }
        }

        lineChart = (LineChart)findViewById(R.id.LineChart);

        if(linechartService == null) {
            linechartService = new RealtimeLineChart(this, lineChart);
        }

        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);




        mDataViewModel = ViewModelProviders.of(this, new DataFactory(getApplication())).get(DataViewModel.class);
        mDataViewModel.getmAllData_Limit1().observe(this, new Observer<List<Data>>() {
            @Override
            public void onChanged(List<Data> data) {
               setData(data);
            }
        });

        mDataViewModel.getAllData().observe(this, new Observer<List<Data>>() {
            @Override
            public void onChanged(List<Data> data) {
           //     barchartService.addEntry(data);
            }
        });

        mDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag) {
                    mDataBtn.setText("STOP");
                    mDataBtn.setTextColor(getApplicationContext().getResources().getColor(R.color.red));

                    Timer_Thread();
                    Thread send_thread = new DataSendThread();
                    send_thread.start();

                    flag = false;
                    running = true;

                } else {
                    running = false;
                    mDataBtn.setText("START");
                    mDataBtn.setTextColor(getApplicationContext().getResources().getColor(R.color.white));
                    flag = true;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case 100:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"외부 저장소 쓰기 가능", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "거부", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Logs.d(TAG, "Connect request result = " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }



    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void findGattServices(List<BluetoothGattService> gattServices) {
        for(BluetoothGattService gattService: gattServices) {
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void displayData(final String data) {
        hex_to_int = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(data != null) {
                    count++;
                    Logs.d(TAG, data);
                    Logs.d(COUNT_TAG, String.valueOf(count));

                    String sb_array[] = new String[4];

                    if(data == null || data.length() < 4) {

                    } else {
                        for(int i = 0; i < 4; i++) {
                            sb_array[i] = data.substring(((i-1)+1)*2, ((i-1)+1)*2+2);
                            Logs.d(TAG, sb_array[i]);
                        }
                        hex_to_int = HexStringToInt(sb_array);

                        a = hex_to_int[0] + hex_to_int[1] * 256;
                        b = hex_to_int[2] + hex_to_int[3] * 256;

                        Logs.d("TAG", String.valueOf(a));
                        Logs.d("TAG", String.valueOf(b));

                        linechartService.addEntry(a, b);

//                        dataValue.add(hex_to_int[4]);
//
//                        if(count == 10) {
//                            dataAverage(dataValue, count);
//                            timer_control = true;
//                            count = 0;
//                        }

                        Data data = new Data();
                        data.setReceiveData1((int) a);
                        data.setReceiveData2((int) b);
                        mDataViewModel.insert(data);
                        dataValue.clear();

                        mGraphData.setText("Green Data = "+a+"  Red Data = "+b);
                        mDataField.setText(sb_array[0] + " " + sb_array[1] + " " + sb_array[2] + " " + sb_array[3] );


                    }
                }
            }
        });
        SystemClock.sleep(10);
    }

    private static int[] HexStringToInt(String[] h) {

        int hi_array[] = new int[4];

        for(int i=0; i < h.length; i++) {
            hi_array[i] = Integer.parseInt(h[i], 16);
        }
        return hi_array;
    }

//    private void dataAverage(List<Integer> mdata, int mcount) {
//        Double total = 0.0;
//        Integer copyArray[] = new Integer[mdata.size()];
//
//        for(int i=0; i < mdata.size(); i++) {
//            copyArray[i] = mdata.get(i);
//        }
//
//        for(int i=0; i<copyArray.length; i++) {
//            total += copyArray[i];
//        }
//
//        double average = total/mcount;
//        Logs.d(AVERAGE_TAG, String.valueOf(average));
//
//        Data data = new Data();
//        data.setReceiveData((int) average);
//        mDataViewModel.insert(data);
//        dataValue.clear();
//
//
//    }

    public void setData(List<Data> data) {
        mData = data;
        String data_value1 = String.valueOf(mData.get(0).receiveData1);
        String data_value2 = String.valueOf(mData.get(0).receiveData2);
        File path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
       // File[] dirs = getExternalFilesDirs("MyData");
      //  path=dirs[0];

        File file = new File(path, "Data.txt");
        Logs.d(TAG, String.valueOf(data));

        try{
           // FileOutputStream fos = new FileOutputStream(file, true);
            FileWriter fw = new FileWriter(file, true);
            PrintWriter writer = new PrintWriter(fw);

            writer.println(data_value1+"  "+data_value2);
            writer.flush();
            writer.close();

            Logs.d(TAG, "파일 저장 완료");
        } catch(IOException e) {
            e.printStackTrace();
        }

//        try {
//            FileOutputStream fos = openFileOutput("myfile.txt", Context.MODE_APPEND);
//            PrintWriter out = new PrintWriter(fos);
//            out.println(data_value);
//            out.close();
//
//            Logs.d(TAG, "파일 저장 완료");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



      //  mGraphData.setText(String.valueOf(mData.get(0).receiveData));
    }

    class DataSendThread extends Thread {
        public void run() {
            byte[] byteSendData = new byte[1];
            byteSendData[0] = 0x4D;
            while(running) {
                try {
                    makeChange(byteSendData);
                    Thread.sleep(100);

                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void makeChange(byte[] out) {
        if(mConnected) {
            characteristicTX.setValue(out);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    private void Timer_Thread() {
        new Thread() {
            @Override
            public void run() {
                timer_control = true;
                TimerProcess();
            }
        }.start();
    }

    private void TimerProcess() {
        int i = 0;
        while(true) {
            try {
                handler.sendEmptyMessage(i++);
                Thread.sleep(1000);
            } catch(InterruptedException e) {

            }
        }
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            timer_control = false;
            Logs.d(TIMER_TAG, String.valueOf(msg.what));
        }
    };
}
