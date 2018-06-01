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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fvcs.cn.utils.AccpetBluetoothMsgThread;
import com.fvcs.cn.utils.AppUtils;
import com.fvcs.cn.utils.LogUtil;
import com.fvcs.cn.utils.OrderUtils;
import com.fvcs.cn.utils.PopViewUtils;
import com.fvcs.cn.utils.PreferenceUtils;
import com.fvcs.cn.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    private Button btn_dq, btn_dg, btn_hj;
    private ImageButton btn_blue;
    private RelativeLayout rl_dq, rl_dg, rl_hj;

    private String[] btnNames = {"灯光", "电器", "环境"};

    private ListView lv_data;
    private ArrayAdapter<String> adapter;

    private BluetoothAdapter bluetoothAdapter;

    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private List<String> names = new ArrayList<String>();
    private List<String> addresses = new ArrayList<String>();

    private PopupWindow pw = null;//蓝牙扫描弹框
    private TextView tv_title = null;

    private BluetoothSocket clientSocket = null;
    private AccpetBluetoothMsgThread abmThread = null;//结束蓝牙数据的线程

    private OrderUtils mOrderUtils;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://连接成功
                    //ToastUtils.showToast(MainActivity.this,"连接成功");
                    bluetoothConnected(null);
                    break;
                case 1://连接失败
                    bluetoothDisConnected(null);
                    break;
                case 3://接收的蓝牙返回数据
                    String res = msg.obj + "";
                    dealReceiveMsg(res);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 根据底层数据刷新页面
     *
     * @param res
     */
    private void dealReceiveMsg(String res) {
        if (TextUtils.isEmpty(res)) return;
        ToastUtils.showToast(MainActivity.this, "收到数据: " + res);
        mOrderUtils.dealReceiveMsg(res);
    }

    /**
     * 连接蓝牙的弹出框
     *
     * @param v
     */
    private void showPW(View v) {
        if (pw == null) {

            int width = AppUtils.getScreenWidth(this)[0] ;
            int wid = (width == 1920)? 960:((int)(width*0.7));
            int hei = AppUtils.dp2px(this,300);

            // 用于PopupWindow的View
            View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.pop_blue_datas, null, false);
            // 创建PopupWindow对象，其中：
            // 第一个参数是用于PopupWindow中的View，第二个参数是PopupWindow的宽度，
            // 第三个参数是PopupWindow的高度，第四个参数指定PopupWindow能否获得焦点
            pw = new PopupWindow(contentView, wid, hei, true);
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
        pw.showAtLocation(v, Gravity.CENTER, 0, 0);
        blueDatasAdd();
        scanBlue();
    }

    /**
     * 蓝牙弹出框消失
     */
    private void dissmissPW() {
        if (pw != null && pw.isShowing()) {
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
        OrderUtils.setKeepTime();
        mOrderUtils.setListener(new OrderUtils.OnBottomMachineOrderListener() {
            @Override
            public void onDealReceiceMsg(String msg) {
                //在此做页面上的刷新操作
                refreshElectricState(msg);
                LogUtil.e(TAG, msg.length() + "--result-->" + msg);
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

        initAppView();// TODO: 2018/5/18
    }

    private void initTabViews() {
        // TODO Auto-generated method stub
        btn_dq = (Button) findViewById(R.id.btn_main_electric);
        btn_dq.setOnClickListener(this);
        btn_hj = (Button) findViewById(R.id.btn_main_environment);
        btn_hj.setOnClickListener(this);
        btn_dg = (Button) findViewById(R.id.btn_main_lighting);
        btn_dg.setOnClickListener(this);
        btn_blue = (ImageButton) findViewById(R.id.btn_blue);
        btn_blue.setOnClickListener(this);
//        btn_blue.setText("未连接");

        btn_blue.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopViewUtils.getInstance().showSetTimeInfo(v);
                return false;
            }
        });

        rl_dg = (RelativeLayout) findViewById(R.id.rl_main_lighting);
        rl_dq = (RelativeLayout) findViewById(R.id.rl_main_electric);
        rl_hj = (RelativeLayout) findViewById(R.id.rl_main_environment);

        //dealViews();
        hiddenRLViews();
        rl_dq.setVisibility(View.VISIBLE);
        btn_dq.setSelected(true);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_blue:
                showPW(v);
                break;
            case R.id.btn_main_electric:
                hiddenRLViews();
                //sendBTMsg(mOrderUtils.getCFJOrder(true));
                rl_dq.setVisibility(View.VISIBLE);
                btn_dq.setSelected(true);
                break;
            case R.id.btn_main_lighting:
                hiddenRLViews();
                rl_dg.setVisibility(View.VISIBLE);
                btn_dg.setSelected(true);
                break;
            case R.id.btn_main_environment:
                hiddenRLViews();
                rl_hj.setVisibility(View.VISIBLE);
                btn_hj.setSelected(true);
                //sendBTMsg(mOrderUtils.getCFJOrder(false));
                break;
            default:
                break;
        }

    }

    private void hiddenRLViews() {
        rl_dg.setVisibility(View.GONE);
        rl_dq.setVisibility(View.GONE);
        rl_hj.setVisibility(View.GONE);
        btn_dq.setSelected(false);
        btn_dg.setSelected(false);
        btn_hj.setSelected(false);

    }

    private void initViews(View pw) {
        // TODO Auto-generated method stub

        tv_title = (TextView) pw.findViewById(R.id.tv_title);

        lv_data = (ListView) pw.findViewById(R.id.lv_data);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, names);
        lv_data.setAdapter(adapter);
        lv_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                // TODO Auto-generated method stub
                if (tv_title != null) {
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
    public void autoConnectBluetooth() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();
        String lastConnectAddress = PreferenceUtils.getInstance().getBTAddress();
        if (TextUtils.isEmpty(lastConnectAddress)) return;
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (lastConnectAddress.equals(device.getAddress())) {
                    connect(device);
                    break;
                }
            }
        }
    }

    /**
     * 重置蓝牙列表
     */
    private void blueDatasAdd() {
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
        LogUtil.e(TAG, "正在扫描...");
        //ToastUtils.showToast(this,"正在扫描...");
        if (tv_title != null) tv_title.setText("正在扫描...");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    /**
     * 蓝牙扫描结束
     */
    private void stopScanBlue() {
        LogUtil.e(TAG, "蓝牙扫描结束...");
        setProgressBarIndeterminateVisibility(false);
        //setTitle("扫描完毕");
        //ToastUtils.showToast(this,"扫描完毕...");
        if (tv_title != null) tv_title.setText("蓝牙列表");
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
                    if (adapter != null) adapter.notifyDataSetChanged();

                    LogUtil.e(TAG,
                            device.getName() + " : " + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                stopScanBlue();
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED
                    .equals(action)) {
                if (device != null) {
                    //LogUtil.e("blue", "蓝牙已连接");
                    bluetoothConnected(device);
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED
                    .equals(action)) {
                if (device != null) {
                    //LogUtil.e("blue", "蓝牙断开连接");
                    bluetoothDisConnected(device);
                }

            }
        }

    };

    /**
     * 蓝牙连接成功处理
     */
    private void bluetoothConnected(BluetoothDevice device) {
       if(device != null){
           LogUtil.e(TAG, device.getName() + " : " + device.getAddress() + "-->已连接");
           //stopScanBlue();
           PreferenceUtils.getInstance().saveBTName(device.getName() + "");
           PreferenceUtils.getInstance().saveBTAddress(device.getAddress());
           dissmissPW();
       }
//        btn_blue.setText(device.getName() + "\n已连接");
        btn_blue.setSelected(true);
    }

    /**
     * 蓝牙断开处理
     */
    private void bluetoothDisConnected(BluetoothDevice device) {
        if (device != null)
            LogUtil.e(TAG, device.getName() + " : " + device.getAddress() + "-->已断开");
        if (clientSocket != null && clientSocket.isConnected()) {
            ToastUtils.showToast(this, "连接失败");
        } else {
//            btn_blue.setText("未连接");
            if (tv_title != null) tv_title.setText("蓝牙列表");
            btn_blue.setSelected(false);
        }

    }


    /**
     * 连接蓝牙设备
     *
     * @param device
     */
    private void connect(BluetoothDevice device) {
        // 固定的UUID
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

        UUID uuid = UUID.fromString(SPP_UUID);
        try {
            clientSocket = device.createRfcommSocketToServiceRecord(uuid);
            clientSocket.connect();
            abmThread = new AccpetBluetoothMsgThread(handler, clientSocket);
            abmThread.setIsRun(true);
            abmThread.start();
            //abmThread.sendBTMsg();
            Message msg = Message.obtain();
            msg.what = 0;
            handler.sendMessage(msg);
        } catch (IOException e) {
            Message msg = Message.obtain();
            msg.what = 1;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    /**
     * 发送命令信息
     *
     * @param msg
     */
    private void sendBTMsg(byte[] msg) {
        if (abmThread != null) {
            LogUtil.e(TAG, "sendBTMsg------->" + new String(msg));
            abmThread.sendBTMsgBytes(msg);
        }else{
            ToastUtils.showToast(this,"请先用蓝牙连接车载系统");
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopScanBlue();
        unregisterReceiver(receiver);
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (abmThread != null) {
            abmThread.setIsRun(false);
        }
    }

    /**
     * 电器页面中控制窗帘升降
     *
     * @param view
     */
    public void electricCurtain(View view) {
        String str_description = view.getContentDescription().toString();
        if (!TextUtils.isEmpty(str_description)) {
            sendBTMsg(mOrderUtils.getCLOrder(Integer.parseInt(str_description.split("-")[0]), str_description.split("-")[1].equals("1") ? true : false));
            LogUtil.e(TAG, "electric_curtainDescription------->" + str_description + "-------->" + Integer.parseInt(str_description.split("-")[0]) + (str_description.split("-")[1].equals("1") ? true : false));
        }
    }

    private ImageButton cb_smoke_wind_small, cb_smoke_wind_mid, cb_cold_wind_small, cb_cold_wind_mid, cb_cold_wind_large,
            cb_new_wind_small, cb_new_wind_mid, cb_warm_wind_small, cb_warm_wind_mid, cb_warm_wind_large;
    private CheckBox cb_electric_coffee, cb_electric_refrigerator, cb_electric_audio, cb_electric_table, cb_electric_door,
            cb_lighting_mbhtop, cb_lighting_bar, cb_lighting_front, cb_lighting_read_one, cb_lighting_read_two, cb_lighting_rear,
            cb_lighting_top_atmosphere, cb_lighting_mid_atmosphere, cb_lighting_down_atmosphere,
            cb_environment_smoke, cb_environment_cold, cb_environment_new_wind, cb_environment_warm_wind;

    public void initAppView() {


        //电器
        cb_electric_coffee = (CheckBox) findViewById(R.id.cb_electric_coffee);
        cb_electric_coffee.setOnCheckedChangeListener(this);
        cb_electric_refrigerator = (CheckBox) findViewById(R.id.cb_electric_refrigerator);
        cb_electric_refrigerator.setOnCheckedChangeListener(this);
        cb_electric_audio = (CheckBox) findViewById(R.id.cb_electric_audio);
        cb_electric_audio.setOnCheckedChangeListener(this);
        cb_electric_table = (CheckBox) findViewById(R.id.cb_electric_table);
        cb_electric_table.setOnCheckedChangeListener(this);
        cb_electric_door = (CheckBox) findViewById(R.id.cb_electric_door);
        cb_electric_door.setOnCheckedChangeListener(this);
        //灯光
        cb_lighting_mbhtop = (CheckBox) findViewById(R.id.cb_lighting_mbhtop);
        cb_lighting_mbhtop.setOnCheckedChangeListener(this);
        cb_lighting_bar = (CheckBox) findViewById(R.id.cb_lighting_bar);
        cb_lighting_bar.setOnCheckedChangeListener(this);
        cb_lighting_front = (CheckBox) findViewById(R.id.cb_lighting_front);
        cb_lighting_front.setOnCheckedChangeListener(this);
        cb_lighting_read_two = (CheckBox) findViewById(R.id.cb_lighting_read_two);
        cb_lighting_read_two.setOnCheckedChangeListener(this);
        cb_lighting_read_one = (CheckBox) findViewById(R.id.cb_lighting_read_one);
        cb_lighting_read_one.setOnCheckedChangeListener(this);
        cb_lighting_rear = (CheckBox) findViewById(R.id.cb_lighting_rear);
        cb_lighting_rear.setOnCheckedChangeListener(this);

        mAtmosphereChaneListener = new AtmosphereChaneListener();

        //氛围灯
        cb_lighting_top_atmosphere = (CheckBox) findViewById(R.id.cb_lighting_top_atmosphere);
        cb_lighting_top_atmosphere.setOnCheckedChangeListener(mAtmosphereChaneListener);
        cb_lighting_mid_atmosphere = (CheckBox) findViewById(R.id.cb_lighting_mid_atmosphere);
        cb_lighting_mid_atmosphere.setOnCheckedChangeListener(mAtmosphereChaneListener);
        cb_lighting_down_atmosphere = (CheckBox) findViewById(R.id.cb_lighting_down_atmosphere);
        cb_lighting_down_atmosphere.setOnCheckedChangeListener(mAtmosphereChaneListener);

        mHJChangeListener = new HJChangeListener();
        //环境风
        cb_environment_smoke = (CheckBox) findViewById(R.id.cb_environment_smoke);
        cb_environment_smoke.setOnCheckedChangeListener(mHJChangeListener);
        cb_environment_new_wind = (CheckBox) findViewById(R.id.cb_environment_new_wind);
        cb_environment_new_wind.setOnCheckedChangeListener(mHJChangeListener);
        cb_environment_cold = (CheckBox) findViewById(R.id.cb_environment_cold);//冷风
        cb_environment_cold.setOnCheckedChangeListener(mHJChangeListener);
        cb_environment_warm_wind = (CheckBox) findViewById(R.id.cb_environment_warm);
        cb_environment_warm_wind.setOnCheckedChangeListener(mHJChangeListener);
        //风速
        cb_smoke_wind_small = (ImageButton) findViewById(R.id.cb_smoke_wind_small);
        cb_smoke_wind_mid = (ImageButton) findViewById(R.id.cb_smoke_wind_mid);
        cb_cold_wind_small = (ImageButton) findViewById(R.id.cb_cold_wind_small);
        cb_cold_wind_mid = (ImageButton) findViewById(R.id.cb_cold_wind_mid);
        cb_cold_wind_large = (ImageButton) findViewById(R.id.cb_cold_wind_large);

        cb_new_wind_small = (ImageButton) findViewById(R.id.cb_new_wind_small);
        cb_new_wind_mid = (ImageButton) findViewById(R.id.cb_new_wind_mid);

        cb_warm_wind_small = (ImageButton) findViewById(R.id.cb_warm_wind_small);
        cb_warm_wind_mid = (ImageButton) findViewById(R.id.cb_warm_wind_mid);
        cb_warm_wind_large = (ImageButton) findViewById(R.id.cb_warm_wind_large);


        smokeWindViewSelected(false);
        smokeWindViewEnabled(false);
        coldWindViewEnabled(false);
        coldWindViewSelect(false);
        newWindViewSelect(false);
        newWindViewEnabled(false);
        warmWindViewSelect(false);
        warmWindViewEnabled(false);

//        refreshElectricState("111011111111102111111");
    }

    //吸烟选中按钮
    public void smokeWindViewSelected(boolean selected) {
        cb_smoke_wind_small.setSelected(selected);
        cb_smoke_wind_mid.setSelected(selected);
    }

    //吸烟可用按钮
    public void smokeWindViewEnabled(boolean enabled) {
        cb_smoke_wind_small.setEnabled(enabled);
        cb_smoke_wind_mid.setEnabled(enabled);
    }

    //冷风选中按钮
    public void coldWindViewSelect(boolean selected) {
        cb_cold_wind_small.setSelected(selected);
        cb_cold_wind_mid.setSelected(selected);
        cb_cold_wind_large.setSelected(selected);
    }

    //冷风2档
    public void coldWindView2() {
        cb_cold_wind_small.setSelected(true);
        cb_cold_wind_mid.setSelected(true);
        cb_cold_wind_large.setSelected(false);

    }

    //冷风可用按钮
    public void coldWindViewEnabled(boolean enabled) {
        cb_cold_wind_small.setEnabled(enabled);
        cb_cold_wind_mid.setEnabled(enabled);
        cb_cold_wind_large.setEnabled(enabled);
    }

    //新风选中按钮
    public void newWindViewSelect(boolean selected) {
        cb_new_wind_small.setSelected(selected);
        cb_new_wind_mid.setSelected(selected);
    }

    //新风可用按钮
    public void newWindViewEnabled(boolean enabled) {
        cb_new_wind_small.setEnabled(enabled);
        cb_new_wind_mid.setEnabled(enabled);
    }

    //暖风选中按钮
    public void warmWindViewSelect(boolean selected) {
        cb_warm_wind_small.setSelected(selected);
        cb_warm_wind_mid.setSelected(selected);
        cb_warm_wind_large.setSelected(selected);

    }

    //暖风2档
    public void warmWindView2() {
        cb_warm_wind_small.setSelected(true);
        cb_warm_wind_mid.setSelected(true);
    }

    //暖风可用按钮
    public void warmWindViewEnabled(boolean enabled) {
        cb_warm_wind_small.setEnabled(enabled);
        cb_warm_wind_mid.setEnabled(enabled);
        cb_warm_wind_large.setEnabled(enabled);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        sendBTMsg(mOrderUtils.getDLDGOrder(Integer.parseInt(buttonView.getContentDescription().toString()), isChecked));
        LogUtil.e(TAG, "getDLDGOrder----->" + (Integer.parseInt(buttonView.getContentDescription().toString()) + "----->" + isChecked));
    }

    AtmosphereChaneListener mAtmosphereChaneListener = null;//氛围灯

    public class AtmosphereChaneListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String color = "0";

            int upColor = PreferenceUtils.getInstance().getIntValue("up_fw_light") ;
            int downColor = PreferenceUtils.getInstance().getIntValue("down_fw_light") ;
            int middleColor = PreferenceUtils.getInstance().getIntValue("middle_fw_light") ;
            //int allColor = PreferenceUtils.getInstance().getIntValue("all_fw_light") ;

            switch (buttonView.getId()){
                case R.id.cb_lighting_top_atmosphere:
                    color = upColor + "" ;
                    break;
                case R.id.cb_lighting_mid_atmosphere:
                    color = middleColor + "" ;
                    break;
                case R.id.cb_lighting_down_atmosphere:
                    color = downColor + "" ;
                    break;
            }

            sendBTMsg(mOrderUtils.getRGBLightOrder(Integer.parseInt(buttonView.getContentDescription().toString()), isChecked,  1, color));
            LogUtil.e(TAG, "getRGBLightOrder" + (Integer.parseInt(buttonView.getContentDescription().toString()) + "----->" + isChecked));
        }
    }

    HJChangeListener mHJChangeListener = null;//环境

    public class HJChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //if (!TextUtils.isEmpty(buttonView.getContentDescription())) {
            switch (buttonView.getId()) {
                case R.id.cb_environment_smoke:
                    sendBTMsg(mOrderUtils.getXYOrder(isChecked));
                    smokeWindViewEnabled(isChecked);
                    smokeWindViewSelected(false);
                    if (isChecked) {
                        smokeWindViewSelected(true);
                    }
                    break;
                case R.id.cb_environment_cold:
                    sendBTMsg(mOrderUtils.getLFOrder(isChecked, 1));
                    coldWindViewEnabled(isChecked);
                    coldWindViewSelect(false);
                    if (isChecked) {
                        cb_cold_wind_small.setSelected(true);
                    }
                    LogUtil.e(TAG, "点击冷风一档按钮---->" + cb_cold_wind_small.isSelected());
                    break;
                case R.id.cb_environment_new_wind:
                    sendBTMsg(mOrderUtils.getXFOrder(isChecked, 1));
                    newWindViewEnabled(isChecked);
                    newWindViewSelect(false);
                    if (isChecked) {
                        cb_new_wind_small.setSelected(true);
                    }
                    break;
                case R.id.cb_environment_warm:
                    sendBTMsg(mOrderUtils.getLFOrder(isChecked, 1));
                    warmWindViewEnabled(isChecked);
                    warmWindViewSelect(false);
                    if (isChecked) {
                        cb_warm_wind_small.setSelected(true);
                    }
                    break;
            }
            LogUtil.e(TAG, "环境风速" + (Integer.parseInt(buttonView.getContentDescription().toString().split("-")[0]) + "----->" + isChecked));

        }
        // }
    }

    public void coldWind(View view) {
//        view.setSelected(!view.isSelected());
        view.setSelected(true);
        boolean selected = view.isSelected();
        if (!TextUtils.isEmpty(view.getContentDescription())) {
            String wind_name_position = view.getContentDescription().toString().split("-")[0];//风的名称位置
            String wind_open = view.getContentDescription().toString().split("-")[1];//风的开关
            String wind_speed = view.getContentDescription().toString().split("-")[2];//风速
            LogUtil.e(TAG, "获取的数据-----》" + wind_name_position + "------" + wind_open + "------" + wind_speed);
            if (wind_name_position.equals("1")) {//吸烟
                sendBTMsg(mOrderUtils.getXYOrder(selected));
                smokeWindViewSelected(true);
            } else if ("2".equals(wind_name_position)) {//冷风
                sendBTMsg(mOrderUtils.getLFOrder(selected, Integer.valueOf(wind_speed)));
                coldWindViewSelect(false);
                if ("1".equals(wind_speed)) {
                    cb_cold_wind_small.setSelected(true);
                } else if ("2".equals(wind_speed)) {
                    coldWindView2();
                } else if ("3".equals(wind_speed)) {
                    coldWindViewSelect(true);
                }
            } else if ("3".equals(wind_name_position)) {//新风
                sendBTMsg(mOrderUtils.getXFOrder(selected, Integer.valueOf(wind_speed)));
                newWindViewSelect(false);
                if ("1".equals(wind_speed)) {
                    cb_new_wind_small.setSelected(true);
                } else if ("2".equals(wind_speed)) {
                    newWindViewSelect(true);
                }

            } else if ("4".equals(wind_name_position)) {//暖风
                sendBTMsg(mOrderUtils.getNFOrder(selected, Integer.valueOf(wind_speed)));
                warmWindViewSelect(false);
                if ("1".equals(wind_speed)) {
                    cb_warm_wind_small.setSelected(true);
                } else if ("2".equals(wind_speed)) {
                    warmWindView2();
                } else if ("3".equals(wind_speed)) {
                    warmWindViewSelect(true);
                }
            }
        }
    }

    public void refreshElectricState(String msg) {
        char openState = '1';//开
        char[] rankState = {'0', '1', '2', '3', '4'};//档位
        cb_electric_coffee.setSelected(openState == msg.charAt(0));
        cb_electric_refrigerator.setSelected(openState == msg.charAt(1));
        cb_electric_audio.setSelected(openState == msg.charAt(2));
        cb_electric_table.setSelected(openState == msg.charAt(3));
        cb_lighting_bar.setSelected(openState == msg.charAt(4));
        cb_lighting_front.setSelected(openState == msg.charAt(5));
        cb_lighting_rear.setSelected(openState == msg.charAt(6));
        cb_lighting_read_one.setSelected(openState == msg.charAt(7));
        cb_lighting_read_two.setSelected(openState == msg.charAt(8));
        cb_electric_door.setSelected(openState == msg.charAt(9));
        cb_environment_smoke.setSelected(openState == msg.charAt(10));
        smokeWindViewSelected(openState == msg.charAt(10));
        cb_environment_new_wind.setSelected(openState == msg.charAt(11));
        if (openState == msg.charAt(11)) {
            if (msg.charAt(12) == rankState[1]) cb_new_wind_small.setSelected(true);
            else newWindViewSelect(true);
        }
        cb_environment_cold.setSelected(msg.charAt(13) == openState);
        if (msg.charAt(13) == openState) {
            if (msg.charAt(14) == rankState[1]) cb_cold_wind_small.setSelected(true);
            else if (msg.charAt(14) == rankState[2]) coldWindView2();
            else coldWindViewSelect(true);
        }
        cb_environment_warm_wind.setSelected(msg.charAt(15) == '1');
        if (msg.charAt(15) == '1') {
            if (msg.charAt(16) == rankState[1]) cb_warm_wind_small.setSelected(true);
            else if (msg.charAt(16) == rankState[2]) warmWindView2();
            else warmWindViewSelect(true);
        }
        cb_lighting_top_atmosphere.setSelected(msg.charAt(17) == openState);
        cb_lighting_mid_atmosphere.setSelected(msg.charAt(18) == openState);
        cb_lighting_down_atmosphere.setSelected(msg.charAt(19) == openState);
        cb_lighting_mbhtop.setSelected(msg.charAt(20) == openState);


    }
// TODO: 2018/5/16 添加动态显示蓝牙图标

    public void setAtmosphereMode(View v){
        //ToastUtils.showToast(this,"设置氛围灯");
        startActivityForResult(new Intent(this,AtmosphereLightActivity.class),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 1:
                    dealAtmospherelight();
                    break;
            }
        }
    }

    public void dealAtmospherelight(){
        int mode = PreferenceUtils.getInstance().getIntValue("atmosLightMode");
       // if(mode==0) mode=1;
        switch (mode){
            case 0:
                break;
            case 1:
                sendBTMsg(mOrderUtils.getRGBLightOrder(1,cb_lighting_top_atmosphere.isChecked(),1,PreferenceUtils.getInstance().getIntValue("up_fw_light")+""));
                sendBTMsg(mOrderUtils.getRGBLightOrder(2,cb_lighting_mid_atmosphere.isChecked(),1,PreferenceUtils.getInstance().getIntValue("middle_fw_light")+""));
                sendBTMsg(mOrderUtils.getRGBLightOrder(3,cb_lighting_down_atmosphere.isChecked(),1,PreferenceUtils.getInstance().getIntValue("down_fw_light")+""));
                break;
            case 2:
                sendBTMsg(mOrderUtils.getRGBLightOrder(0,true,2,PreferenceUtils.getInstance().getIntValue("all_fw_light")+""));
                break;
            case 3:
                sendBTMsg(mOrderUtils.getRGBLightOrder(0,true,3,PreferenceUtils.getInstance().getIntValue("all_fw_light")+""));
                break;
        }
    }
}
