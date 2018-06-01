package com.fvcs.cn.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.fvcs.cn.R;


public class PopViewUtils {
	private Context context ;
	private static PopViewUtils instance ;
	
	private PopupWindow setTimeInfo ;
	
	private PopViewUtils(Context context){
		this.context = context ;
	}
	
	public static synchronized void init(Context cxt){
	    if(instance == null){
	    	instance = new PopViewUtils(cxt);
	    }
	}
	
	public static PopViewUtils getInstance(){
		if(instance == null){
			throw new RuntimeException("please init first!");
		}
		return instance ;
	}

	
	public void showSetTimeInfo(View v){

		if(setTimeInfo == null){

			int width = AppUtils.getScreenWidth(context)[0] ;
			int wid = (width == 1920)? 640:(width/2);
			int hei = AppUtils.dp2px(context,200);

			LogUtil.e("TAG","-wid-->" + wid + " ,-hei-->" + hei);

			View layoutView = View.inflate(context, R.layout.pop_set_time, null);
			// TODO: 2016/5/17 创建PopupWindow对象，指定宽度和高度
//            setTimeInfo = new PopupWindow(layoutView, 640, 400);
			setTimeInfo = new PopupWindow(layoutView, wid, hei);
            // TODO: 2016/5/17 设置动画
            //window.setAnimationStyle(R.style.popup_window_anim);
            // TODO: 2016/5/17 设置背景颜色
            //setTimeInfo.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));
            // TODO: 2016/5/17 设置可以获取焦点
            setTimeInfo.setFocusable(true);
            // TODO: 2016/5/17 设置可以触摸弹出框以外的区域
            setTimeInfo.setOutsideTouchable(true);
            // TODO：更新popupwindow的状态
            setTimeInfo.update();
            setTimeInfo.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					setTimeInfo = null ;
				}
			});

			final LinearLayout lly_settime = (LinearLayout)layoutView.findViewById(R.id.lly_settime);
			final EditText et_psd = (EditText)layoutView.findViewById(R.id.et_psd);
			final EditText et_time = (EditText)layoutView.findViewById(R.id.et_time);
			final Button tv_unlock = (Button)layoutView.findViewById(R.id.btn_unlock);
			final Button btn_sure = (Button)layoutView.findViewById(R.id.btn_sure);

			et_time.setText(PreferenceUtils.getInstance().getIntValue("clickKeepTime")+"");

			tv_unlock.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String psd = et_psd.getText().toString();
					if("888888".equals(psd)){

						et_psd.setVisibility(View.GONE);
						tv_unlock.setVisibility(View.GONE);

						lly_settime.setVisibility(View.VISIBLE);
						btn_sure.setVisibility(View.VISIBLE);
					}else{
						ToastUtils.showToast(context,"密码错误，请重试");
					}
				}
			});
			btn_sure.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String time = et_time.getText().toString();
					if(TextUtils.isEmpty(time)){
						ToastUtils.showToast(context,"时间不能为空值");
					}else{
						PreferenceUtils.getInstance().saveIntValue("clickKeepTime",Integer.parseInt(time));
						OrderUtils.setKeepTime();
						ToastUtils.showToast(context,"设置点击时长为 " + time + " 秒");
						disSetTimeInfo();
					}
				}
			});
		}
		setTimeInfo.showAtLocation(v, Gravity.CENTER, 0, 0);
	}
	
	public void disSetTimeInfo(){
		if(setTimeInfo != null && setTimeInfo.isShowing()){
			setTimeInfo.dismiss();
		}
		setTimeInfo = null ;
	}
}
