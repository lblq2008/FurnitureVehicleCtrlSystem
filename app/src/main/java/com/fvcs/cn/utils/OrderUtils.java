package com.fvcs.cn.utils;

import android.text.TextUtils;

/**
 * 命令生成的工具类
 * Created by liubo on 2018/5/8.
 */

public class OrderUtils {

    public byte SOH = 0x78 ;
    public byte[] EOH = {0x0d,0x0a};


    OnBottomMachineOrderListener listener ;

    public OnBottomMachineOrderListener getListener() {
        return listener;
    }

    public void setListener(OnBottomMachineOrderListener listener) {
        this.listener = listener;
    }

    /**
     * 解析校验命令，并重新组装
     * @param msg
     */
    public void dealReceiveMsg(String msg){
        if(TextUtils.isEmpty(msg)) return ;
        //解析校验命令

        listener.onDealReceiceMsg(msg);
    }

    /**
     * 小桌板的命令
     * true 开    false 关
     * @param open
     */
    public byte[] getXZBOrder(boolean open){

        return null ;
    }

    /**
     * 咖啡机的命令
     * true 开    false 关
     * @param open
     */
    public byte[] getCFJOrder(boolean open){
        byte no = 0x01 ;
        byte state = (byte)(open?0x31:0x30);
        return getDanLuCtrl(no,state) ;
    }

    /**
     * 单路控制命令
     * @param no 设备号
     * @param state 状态
     * @return
     */
    public byte[] getDanLuCtrl(byte no, byte state){
        byte[] ord = new byte[9];
        ord[0] = SOH ;
        ord[1] = 0x31;
        ord[2] = 0x06;
        ord[3] = 0x10;
        ord[4] = no;
        ord[5] = state;
        ord[7] = EOH[0];
        ord[8] = EOH[1];
        ord[6] = getHexCheck(ord);
        return ord ;
    }

    /**
     * 双路控制命令
     * @param no 设备号
     * @param state 状态
     * @param time 时间
     * @return
     */
    public byte[] getShuangLuCtrl(byte no, byte state, byte time){
        byte[] ord = new byte[10];
        ord[0] = SOH ;
        ord[1] = 0x31;
        ord[2] = 0x07;
        ord[3] = 0x20;
        ord[4] = no;
        ord[5] = state;
        ord[6] = time ;
        ord[8] = EOH[0];
        ord[9] = EOH[1];
        ord[7] = getHexCheck(ord);
        return ord ;
    }

    /**
     * 三路控制命令
     * @param no 设备号
     * @param state 状态
     * @param gear 档位
     * @return
     */
    public byte[] getSanLuCtrl(byte no, byte state, byte gear){
        byte[] ord = new byte[10];
        ord[0] = SOH ;
        ord[1] = 0x31;
        ord[2] = 0x07;
        ord[3] = 0x30;
        ord[4] = no;
        ord[5] = state;
        ord[6] = gear ;
        ord[8] = EOH[0];
        ord[9] = EOH[1];
        ord[7] = getHexCheck(ord);
        return ord ;
    }

    /**
     * 四路控制命令
     * @param no 设备号
     * @param state 状态
     * @param mode 呼吸模式
     * @param rgb RGB值
     * @return
     */
    public byte[] getSiLuCtrl(byte no, byte state, byte mode , byte[] rgb){
        byte[] ord = new byte[13];
        ord[0] = SOH ;
        ord[1] = 0x31;
        ord[2] = 0x0a;
        ord[3] = 0x40;
        ord[4] = no;
        ord[5] = state;
        ord[6] = mode ;

        ord[7] = rgb[0];
        ord[8] = rgb[1];
        ord[9] = rgb[2] ;

        ord[11] = EOH[0];
        ord[12] = EOH[1];
        ord[10] = getHexCheck(ord);
        return ord ;
    }



    /**
     * 计算和校验 除帧头帧尾的和的低八位
     * @param ord
     * @return
     */
    public byte getHexCheck(byte[] ord){
        int sum = 0 ;
        for (int i = 1; i <ord.length-3 ; i++) {
            sum = sum + ord[i];
        }
        sum = sum % 0xff;
        return (byte)sum;
    }

    /**
     * 影音娱乐的命令
     * true 开    false 关
     * @param open
     */
    public byte[] getYYYLOrder(boolean open){
        String order = "" ;
        return null ;
    }

    /**
     * 电冰箱的命令
     * true 开    false 关
     * @param open
     */
    public byte[] getDBXOrder(boolean open){
        String order = "" ;
        return null ;
    }

    /**
     * 电视机的命令
     * true 升    false 降
     * @param open
     */
    public byte[] getDSJOrder(boolean open){
        String order = "" ;
        return null ;
    }

    /**
     * 窗帘的命令
     * true 升    false 降
     * pos 0：全体 1：前左  2：前右  3：中左  4：中右  5：后左  6：后右
     * @param open
     */
    public byte[] getOrder(int pos ,boolean open){
        String order = "" ;
        return null ;
    }


    /**
     * 单路灯光的命令
     * true 开    false 关
     * 1：吧台灯  2：前射灯  3：后射灯  4：阅读灯一  5：阅读灯二
     * @param open
     */
    public byte[] getDLDGOrder(boolean open){
        String order = "" ;
        return null ;
    }

    /**
     * RGB灯的控制的命令
     * true 开    false 关
     * pos:   1：上氛围灯  2：中氛围灯  3：下氛围灯  4：顶灯
     * type: 颜色的类型
     * @param open
     */
    public byte[] getRGBLightOrder(int pos , boolean open , int type){
        String order = "" ;
        return null ;
    }

    /**
     * 吸烟的命令
     * true 开    false 关
     * @param open
     */
    public byte[] getXYOrder(boolean open){
        String order = "" ;
        return null ;
    }

    /**
     * 新风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public byte[] getXOrder(boolean open , int gear){
        String order = "" ;
        return null ;
    }

    /**
     * 冷风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public byte[] getLFOrder(boolean open , int gear){
        String order = "" ;
        return null ;
    }

    /**
     * 暖风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public byte[] getNFOrder(boolean open , int gear){
        String order = "" ;
        return null ;
    }

    public interface OnBottomMachineOrderListener{
        void onDealReceiceMsg(String msg);
    }
}
