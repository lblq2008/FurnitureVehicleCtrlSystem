package com.fvcs.cn.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

/**
 * 蓝牙信息处理
 */
public class AccpetBluetoothMsgThread extends Thread {
	private String TAG = "AccpetBluetoothMsgThread";
	private InputStream is;
	private boolean isRun = false ;
	private Handler handler ;
	private BluetoothSocket clientSocket;

	private OutputStream outputStream ;
	
	public AccpetBluetoothMsgThread(Handler handler,
			BluetoothSocket clientSocket) {
		super();
		this.handler = handler;
		this.clientSocket = clientSocket;
	}

	public void setIsRun(boolean run){
		isRun = run ;
	}
	
	public boolean isRun(){
		return isRun ;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		LogUtil.e(TAG, "----开始接收底层消息---");
		try {
				is = clientSocket.getInputStream();
				while (isRun) {
					byte[] buffer = new byte[27];
					int count = is.read(buffer);
					LogUtil.e(TAG,"count: "+ count);

					Message message = Message.obtain();
					//message.obj = byte2HexStr(buffer);
					//message.obj = new String(buffer,"UTF-8");

					message.obj = getStringFromByte(buffer);

					message.what = 3 ;
//					message.obj = new String(buffer);
					handler.sendMessage(message);
				}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//77 32	14	30	31	31	31	30	31	31	31	31	31	31	30	31	31	31	31	31	31  30 30 31 38	0d0a


	public String getStringFromByte(byte[] data){
		try {
			String con = new String(data,"UTF-8");
			if(con.length()>=26){
				return con.substring(3,con.length()-3);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null ;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		if(is != null){
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(clientSocket != null){
			try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**  
	 * bytes转换成十六进制字符串  
	 * @param  b byte数组
	 * @return String 每个Byte值之间空格分隔  
	 */    
	public static String byte2HexStr(byte[] b)    
	{    
	    String stmp="";    
	    StringBuilder sb = new StringBuilder("");    
	    for (int n=0;n<b.length;n++)    
	    {    
	    	//LogUtil.e("ABMT", n + "--->" + b[n] + "");
	        stmp = Integer.toHexString(b[n] & 0xFF);    
	        sb.append((stmp.length()==1)? "0" + stmp : stmp);    
	        sb.append("");    
	    }    
	    return sb.toString().toUpperCase().trim();    
	}

    /**
     * 蓝牙发送信息
     * @param msg
     */
	public void sendBTMsg(String msg){
        if(outputStream == null && clientSocket != null){
            try {
                outputStream = clientSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(outputStream != null) {
            try {
                outputStream.write(msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	/**
	 * 蓝牙发送信息
	 * @param msg
	 */
	public void sendBTMsgBytes(byte[] msg){
		if(outputStream == null && clientSocket != null){
			try {
				outputStream = clientSocket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(outputStream != null) {
			try {
				outputStream.write(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
