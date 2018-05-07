package com.fvcs.cn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.fvcs.cn.utils.LogUtil;
import com.fvcs.cn.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_up, btn_middle, btn_down;
    private RelativeLayout rl_dq, rl_dg, rl_hj;
    //private int currentIndex = 1;

    private String[] btnNames = {"灯光","电器","环境"};

    private ListView lv_data;
    private ArrayAdapter<String> adapter;

    private BluetoothAdapter bluetoothAdapter;

    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private List<String> names = new ArrayList<String>();
    private List<String> addresses = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        setContentView(R.layout.activity_main);
        initViews();
        initTabViews();
    }

    private void initTabViews() {
        // TODO Auto-generated method stub
        btn_up = (Button) findViewById(R.id.btn_up);
        btn_up.setOnClickListener(this);
        btn_down = (Button) findViewById(R.id.btn_down);
        btn_down.setOnClickListener(this);
        btn_middle = (Button) findViewById(R.id.btn_middle);
        //btn_middle.setOnClickListener(this);

        rl_dg = (RelativeLayout) findViewById(R.id.rl_dg);
        rl_dq = (RelativeLayout) findViewById(R.id.rl_dq);
        rl_hj = (RelativeLayout) findViewById(R.id.rl_hj);

        dealViews();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        String temp = "" ;
        switch (v.getId()) {
            case R.id.btn_up:
                temp = btn_up.getText().toString();
                btn_up.setText(btn_middle.getText().toString());
                btn_middle.setText(temp);
                break;
            case R.id.btn_middle:

                break;
            case R.id.btn_down:
                temp = btn_down.getText().toString();
                btn_down.setText(btn_middle.getText().toString());
                btn_middle.setText(temp);
                break;
            default:
                break;
        }
        temp = "";
        dealViews();
    }

    private void dealViews() {
        // TODO Auto-generated method stub

        hiddenRLViews();
        String text = btn_middle.getText().toString();
        if(btnNames[0].equals(text)){
            rl_dg.setVisibility(View.VISIBLE);
        }else if(btnNames[1].equals(text)){
            rl_dq.setVisibility(View.VISIBLE);
        }else{
            rl_hj.setVisibility(View.VISIBLE);
        }

    }

    private void hiddenRLViews(){
        rl_dg.setVisibility(View.GONE);
        rl_dq.setVisibility(View.GONE);
        rl_hj.setVisibility(View.GONE);
    }

    private void initViews() {
        // TODO Auto-generated method stub
        lv_data = (ListView) findViewById(R.id.lv_data);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, names);
        lv_data.setAdapter(adapter);
        lv_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                    }
                }).start();

            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device);
                addresses.add(device.getAddress());
                names.add(device.getName());
                adapter.notifyDataSetChanged();
                LogUtil.e("blue2", device.getName() + " : " + device.getAddress());
            }
        }

        // 蓝牙扫描结束
        IntentFilter filter = new IntentFilter(
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);// 蓝牙扫描获取到设备
        registerReceiver(receiver, filter);

        scanBlue();
    }

    private void scanBlue() {
        setProgressBarIndeterminateVisibility(true);
        //setTitle("正在扫描...");
        ToastUtils.showToast(this,"正在扫描...");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void stopScanBlue() {
        setProgressBarIndeterminateVisibility(false);
        //setTitle("扫描完毕");
        ToastUtils.showToast(this,"扫描完毕...");
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null
                        && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    devices.add(device);
                    addresses.add(device.getAddress() + "");
                    names.add(device.getName() + " : " + device.getAddress());
                    adapter.notifyDataSetChanged();

                    LogUtil.e("blue",
                            device.getName() + " : " + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                stopScanBlue();
            }
        }

    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopScanBlue();
        unregisterReceiver(receiver);
    }
}
