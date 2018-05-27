package com.fvcs.cn;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.fvcs.cn.utils.ColorPickerDialog;
import com.fvcs.cn.utils.LogUtil;
import com.fvcs.cn.utils.PreferenceUtils;
import com.fvcs.cn.utils.ToastUtils;

public class AtmosphereLightActivity extends AppCompatActivity {

    private static final  String TAG = "AtmosphereLightActivity";
    private LinearLayout lly_danse , lly_huxi ;
    private RadioGroup rg_mode ;
    private ColorPickerDialog dialog ;

    private int selectColor = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        setContentView(R.layout.activity_atmosphere_light);
        initViews();
    }

    private void initViews() {
        lly_danse = (LinearLayout)findViewById(R.id.lly_danse);
        lly_huxi = (LinearLayout)findViewById(R.id.lly_huxi);
        rg_mode = (RadioGroup)findViewById(R.id.rg_mode);
        rg_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                lly_danse.setVisibility(View.GONE);
                lly_huxi.setVisibility(View.GONE);
                int mode = 1 ;
                switch (checkedId){
                    case R.id.rb_danse:
                        lly_danse.setVisibility(View.VISIBLE);
                        mode = 1;
                        break;
                    case R.id.rb_huxi:
                        lly_huxi.setVisibility(View.VISIBLE);
                        mode = 2 ;
                        break;
                    case R.id.rb_xunhuan:
                        mode = 3 ;
                        break;
                }
                PreferenceUtils.getInstance().saveIntValue("atmosLightMode",mode);
                PreferenceUtils.getInstance().saveIntValue("atmosCheckedId",checkedId);
            }
        });

        int mode = PreferenceUtils.getInstance().getIntValue("atmosLightMode");
        if(mode !=0)
            rg_mode.check(PreferenceUtils.getInstance().getIntValue("atmosCheckedId"));
        else
            rg_mode.check(R.id.rb_danse);

        int upColor = PreferenceUtils.getInstance().getIntValue("up_fw_light") ;
        int downColor = PreferenceUtils.getInstance().getIntValue("down_fw_light") ;
        int middleColor = PreferenceUtils.getInstance().getIntValue("middle_fw_light") ;
        int allColor = PreferenceUtils.getInstance().getIntValue("all_fw_light") ;

        ((Button)findViewById(R.id.btn_up_light)).setBackgroundColor(upColor==0?Color.WHITE:upColor);
        ((Button)findViewById(R.id.btn_middle_light)).setBackgroundColor(middleColor==0?Color.WHITE:middleColor);
        ((Button)findViewById(R.id.btn_down_light)).setBackgroundColor(downColor==0?Color.WHITE:downColor);
        ((Button)findViewById(R.id.btn_all_light)).setBackgroundColor(allColor==0?Color.WHITE:allColor);
    }

    public void choseColor(final View view){
        final String con = view.getContentDescription().toString();
        if(TextUtils.isEmpty(con)) return;
        //view.setBackgroundColor();
        //ToastUtils.showToast(this,con);
        int initColor = PreferenceUtils.getInstance().getIntValue(con);
        dialog = new ColorPickerDialog(this, initColor,"颜色选择器", new ColorPickerDialog.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                selectColor = color;
                LogUtil.e(TAG,"color-->" + color);

                ((Button)view).setBackgroundColor(color);

                int red = Color.green(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                LogUtil.e(TAG,"red-->" + red);
                LogUtil.e(TAG,"green-->" + green);
                LogUtil.e(TAG,"blue-->" + blue);

                PreferenceUtils.getInstance().saveIntValue(con,color);

            }
        });
        dialog.show();
    }

    public void onSure(View v){
        setResult(RESULT_OK);
        finish();
    }
}
