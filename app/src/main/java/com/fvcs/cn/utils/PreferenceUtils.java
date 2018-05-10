package com.fvcs.cn.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** 
* @ClassName: PreferenceUtils 
* @Description: TODO(Preference操作工具类) 
* @author Bob 
* @date 2015年11月12日 上午10:30:06 
*  
*/
public class PreferenceUtils {
	
	public static final String PRE_NAME = "Furniture_Vehicle" ;
	public static final String BT_NAME = "bt_name" ;
	public static final String BT_ADDRESS = "bt_address" ;

	protected static final String TAG = "PreferenceUtils";
	
	private static PreferenceUtils instance ;
	private static SharedPreferences preferences ;
	private static SharedPreferences.Editor editor ;
	private PreferenceUtils(Context context){
		preferences = context.getSharedPreferences(PRE_NAME, Context.MODE_PRIVATE);
		editor = preferences.edit();
	}

	public static synchronized void init(Context cxt){
	    if(instance == null){
	    	instance = new PreferenceUtils(cxt);
	    }
	}
	
	public static PreferenceUtils getInstance(){
		if(instance == null){
			throw new RuntimeException("please init first!");
		}
		return instance ;
	}
	
	public void clearPreData(){
		editor.clear().commit();
	}
	
	public void saveStringValue(String key , String value){
		editor.putString(key, value).commit();
	}
	
	public String getStringValue(String key){
		return preferences.getString(key, "");
	}
	
	public boolean getBooleanValue(String key){
		return preferences.getBoolean(key, false);
	}
	
	public boolean saveBooleanValue(String key , boolean value){
		return editor.putBoolean(key, value).commit();
	}
	
	public int getIntValue(String key){
		return preferences.getInt(key, 0);
	}
	
	public boolean saveIntValue(String key , int value){
		return editor.putInt(key, value).commit();
	}
	

	
	public void saveBTName(String value){
		editor.putString(BT_NAME, value).commit();
	}
	
	public void saveBTAddress(String value){
		editor.putString(BT_ADDRESS, value).commit();
	}
	
	public String getBTName(){
		return preferences.getString(BT_NAME, "");
	}
	public String getBTAddress(){
		return preferences.getString(BT_ADDRESS, "");
	}
	

}
