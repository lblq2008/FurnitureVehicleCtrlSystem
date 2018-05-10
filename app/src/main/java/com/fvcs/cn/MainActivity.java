package com.fvcs.cn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fvcs.cn.utils.AccpetBluetoothMsgThread;
import com.fvcs.cn.utils.LogUtil;
import com.fvcs.cn.utils.OrderUtils;
import com.fvcs.cn.utils.PreferenceUtils;
import com.fvcs.cn.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private Button btn_up, btn_middle, btn_down,btn_blue;
    private RelativeLayout rl_dq, rl_dg, rl_hj;

    private String[] btnNames = {"灯光","电器","环境"};

    private ListView lv_data;
    private ArrayAdapter<String> adapter;

    private BluetoothAdapter bluetoothAdapter;

    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private List<String> names = new ArrayList<String>();
    private List<String> addresses = new ArrayList<String>();

    private PopupWindow pw = null ;//蓝牙扫描弹框
    private TextView tv_title = null ;

    private BluetoothSocket clientSocket = null ;
    private AccpetBluetoothMsgThread abmThread = null ;//结束蓝牙数据的线程

    private OrderUtils mOrderUtils ;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://连接成功
                    //ToastUtils.showToast(MainActivity.this,"连接成功");
                    break;
                case 1://连接失败
                    bluetoothDisConnected(null);
                    break;
                case 3://接收的蓝牙返回数据
                    String res = msg.obj + "" ;
                    dealReceiveMsg(res);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 根据底层数据刷新页面
     * @param res
     */
    private void dealReceiveMsg(String res) {
        if(TextUtils.isEmpty(res))return;
        ToastUtils.showToast(MainActivity.this,"收到数据: " + res);
        mOrderUtils.dealReceiveMsg(res);
    }

    /**
     * 连接蓝牙的弹出框
     * @param v
     */
    private void showPW(View v){
        if(pw == null){
            // 用于PopupWindow的View
            View contentView= LayoutInflater.from(MainActivity.this).inflate(R.layout.pop_blue_datas, null, false);
            // 创建PopupWindow对象，其中：
            // 第一个参数是用于PopupWindow中的View，第二个参数是PopupWindow的宽度，
            // 第三个参数是PopupWindow的高度，第四个参数指定PopupWindow能否获得焦点
            pw=new PopupWindow(contentView, 800, 600, true);
            // 设置PopupWindow的背景
            pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // 设置PopupWindow是否能响应外部点击事件
            pw.setOutsideTouchable(true);
            // 设置PopupWindow是否能响应点击事件
            pw.setTouchable(true);

            initViews(contentView);
            pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    stopScanBlue();
                }
            });

        }
        dissmissPW();
        pw.showAtLocation(v, Gravity.CENTER,0,0);
        blueDatasAdd();
        scanBlue();
    }

    /**
     * 蓝牙弹出框消失
     */
    private void dissmissPW(){
        if(pw != null && pw.isShowing()){
            pw.dismiss();
        }
        //stopScanBlue();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        setContentView(R.layout.activity_main);
        //initViews();

        // 蓝牙扫描结束
        IntentFilter filter = new IntentFilter(
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);// 蓝牙扫描获取到设备
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);// 蓝牙连接
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);// 蓝牙断开连接
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// 蓝牙状态监听
        registerReceiver(receiver, filter);
        initTabViews();
        mOrderUtils = new OrderUtils();
        mOrderUtils.setListener(new OrderUtils.OnBottomMachineOrderListener() {
            @Override
            public void onDealReceiceMsg(String msg) {
                //在此做页面上的刷新操作
                LogUtil.e(TAG,msg.length() + "--result-->" + msg);
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

        autoConnectBluetooth();
    }

    private void initTabViews() {
        // TODO Auto-generated method stub
        btn_up = (Button) findViewById(R.id.btn_up);
        btn_up.setOnClickListener(this);
        btn_down = (Button) findViewById(R.id.btn_down);
        btn_down.setOnClickListener(this);
        btn_middle = (Button) findViewById(R.id.btn_middle);
        btn_middle.setOnClickListener(this);
        btn_blue = (Button) findViewById(R.id.btn_blue);
        btn_blue.setOnClickListener(this);
        btn_blue.setText("未连接");

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
            case R.id.btn_blue:
                showPW(v);
                break;
            case R.id.btn_up:
                temp = btn_up.getText().toString();
                btn_up.setText(btn_middle.getText().toString());
                btn_middle.setText(temp);
                sendBTMsg("1234");
                break;
            case R.id.btn_middle:

                break;
            case R.id.btn_down:
                temp = btn_down.getText().toString();
                btn_down.setText(btn_middle.getText().toString());
                btn_middle.setText(temp);
                sendBTMsg("AbCDe");
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

    private void initViews(View pw) {
        // TODO Auto-generated method stub

        tv_title = (TextView) pw.findViewById(R.id.tv_title);

        lv_data = (ListView)pw.findViewById(R.id.lv_data);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, names);
        lv_data.setAdapter(adapter);
        lv_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                // TODO Auto-generated method stub
                if(tv_title != null){
                    tv_title.setText("正在连接:" + devices.get(position).getName());
                }
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        connect(devices.get(position));
                    }
                }).start();

            }
        });

    }

    /**
     * 自动连接上次连接的蓝牙
     */
    public void autoConnectBluetooth(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();
        String lastConnectAddress = PreferenceUtils.getInstance().getBTAddress();
        if(TextUtils.isEmpty(lastConnectAddress)) return ;
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
               if(lastConnectAddress.equals(device.getAddress())){
                   connect(device);
                   break ;
               }
            }
        }
    }

    /**
     * 重置蓝牙列表
     */
    private void blueDatasAdd(){
        devices.clear();
        addresses.clear();
        names.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device);
                addresses.add(device.getAddress());
                names.add(device.getName());
                adapter.notifyDataSetChanged();
                LogUtil.e(TAG, device.getName() + " : " + device.getAddress());
            }
        }
    }

    /**
     * 开始扫描蓝牙
     */
    private void scanBlue() {
        setProgressBarIndeterminateVisibility(true);
        //setTitle("正在扫描...");
        LogUtil.e(TAG,"正在扫描...");
        //ToastUtils.showToast(this,"正在扫描...");
        if(tv_title != null) tv_title.setText("正在扫描...");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    /**
     * 蓝牙扫描结束
     */
    private void stopScanBlue() {
        LogUtil.e(TAG,"蓝牙扫描结束...");
        setProgressBarIndeterminateVisibility(false);
        //setTitle("扫描完毕");
        //ToastUtils.showToast(this,"扫描完毕...");
        if(tv_title != null) tv_title.setText("蓝牙列表");
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                if (device != null
                        && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    devices.add(device);
                    addresses.add(device.getAddress() + "");
                    names.add(device.getName() + " : " + device.getAddress());
                    if(adapter != null) adapter.notifyDataSetChanged();

                    LogUtil.e(TAG,
                            device.getName() + " : " + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                stopScanBlue();
            } else if(BluetoothDevice.ACTION_ACL_CONNECTED
                    .equals(action)){
                if(device != null){
                    //LogUtil.e("blue", "蓝牙已连接");
                    bluetoothConnected(device);
                }
            } else if(BluetoothDevice.ACTION_ACL_DISCONNECTED
                    .equals(action)){
                if(device != null){
                    //LogUtil.e("blue", "蓝牙断开连接");
                    bluetoothDisConnected(device);
                }

            }
        }

    };

    /**
     *  蓝牙连接成功处理
     */
    private void bluetoothConnected(BluetoothDevice device) {
        LogUtil.e(TAG, device.getName() + " : " + device.getAddress() + "-->已连接");
        //stopScanBlue();
        PreferenceUtils.getInstance().saveBTName(device.getName() + "");
        PreferenceUtils.getInstance().saveBTAddress(device.getAddress());
        dissmissPW();
        btn_blue.setText(device.getName() + "\n已连接");
    }

    /**
     *蓝牙断开处理
     */
    private void bluetoothDisConnected(BluetoothDevice device) {
        if(device != null)LogUtil.e(TAG, device.getName() + " : " + device.getAddress() + "-->已断开");
        if(clientSocket !=null && clientSocket.isConnected()){
            ToastUtils.showToast(this,"连接失败");
        }else{
            btn_blue.setText("未连接");
            tv_title.setText("蓝牙列表");
        }
    }


    /**
     * 连接蓝牙设备
     * @param device
     */
    private void connect(BluetoothDevice device){
        // 固定的UUID
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

        UUID uuid = UUID.fromString(SPP_UUID);
        try {
            clientSocket = device.createRfcommSocketToServiceRecord(uuid);
            clientSocket.connect();
            abmThread = new AccpetBluetoothMsgThread(handler,clientSocket);
            abmThread.setIsRun(true);
            abmThread.start();
            //abmThread.sendBTMsg();
            Message msg = Message.obtain();
            msg.what = 0 ;
            handler.sendMessage(msg);
        } catch (IOException e) {
            Message msg = Message.obtain();
            msg.what = 1 ;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    /**
     * 发送命令信息
     * @param msg
     */
    private void sendBTMsg(String msg){
        if(abmThread != null){
            abmThread.sendBTMsg(msg);
        }
    }

        @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopScanBlue();
        unregisterReceiver(receiver);
        if(clientSocket != null){
             try {
                 clientSocket.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
        }
        if(abmThread != null){
            abmThread.setIsRun(false);
        }
    }
}
