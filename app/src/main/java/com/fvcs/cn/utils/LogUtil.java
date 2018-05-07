package com.fvcs.cn.utils;

import android.util.Log;

/**
 * 日志管理
 * @ClassName LogUtils
 * @author Bob
 * @date 2015-1-9 上午11:39:50 
 */
public class LogUtil {
	
	public static int ALLLOG = 0;//打印全部日志
	public static int VERBOSE = 1;
	public static int DEBUGE = 2;
	public static int INFO = 3;
	public static int WARN = 4;
	public static int ERROR = 5;
	public static int NOTHING = 6;//不打印日
	public static int LEVEL = ALLLOG;

	public static void v(String tag, String msg) {
		if (LEVEL <= VERBOSE) {
			Log.v(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (LEVEL <= DEBUGE) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (LEVEL <= INFO) {
			Log.i(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (LEVEL <= WARN) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (LEVEL <= ERROR) {
			Log.e(tag, msg);
		}
	}
}
