package com.fvcs.cn.utils;

import android.text.TextUtils;

/**
 * 命令生成的工具类
 * Created by liubo on 2018/5/8.
 */

public class OrderUtils {

    public byte SOH = 0x78 ;
    public byte[] EOH = {0x0d,0x0a};
    public byte TIME = 0x05 ;


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
        byte no = 0x04 ;
        byte state = (byte)(open?0x31:0x30);
        return getDanLuCtrl(no,state) ;
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
     * 影音娱乐的命令
     * true 开    false 关
     * @param open
     */
    public byte[] getYYYLOrder(boolean open){
        byte no = 0x03 ;
        byte state = (byte)(open?0x31:0x30);
        return getDanLuCtrl(no,state) ;
    }

    /**
     * 电冰箱的命令
     * true 开    false 关
     * @param open
     */
    public byte[] getDBXOrder(boolean open){
        byte no = 0x02 ;
        byte state = (byte)(open?0x31:0x30);
        return getDanLuCtrl(no,state) ;
    }

    /**
     * 电视机的命令
     * true 升    false 降
     * @param open
     */
    public byte[] getDSJOrder(boolean open){
        byte no = 0x08 ;
        byte state = (byte)(open?0x31:0x30);
        return getShuangLuCtrl(no,state,TIME) ;
    }

    public byte[] CTNos = {0x01,0x02,0x03,0x04,0x05,0x06,0x07};
    /**
     * 窗帘的命令
     * true 升    false 降
     * pos 0：全体 1：前左  2：前右  3：中左  4：中右  5：后左  6：后右
     * @param open
     */
    public byte[] getCLOrder(int pos ,boolean open){
        byte state = (byte)(open?0x31:0x30);
        byte no = CTNos[pos] ;
        return getShuangLuCtrl(no,state,TIME) ;
    }

 //   * 0:顶灯  1：吧台灯  2：前射灯  3：后射灯  4：阅读灯一  5：阅读灯二

//    0x01	咖啡机
//    0x02	电冰箱
//    0x03	影音娱乐
//    0x04	小桌板
//    0x05	吧台灯
//    0x06	前射灯
//    0x07	后射灯
//    0x08	阅读灯一
//    0x09	阅读灯二
//    0x0A	电动门
//    0x0B	顶灯

    public byte[] DLDevicesNo = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B};

    /**
     * 单路灯光的命令
     * @param open true 开    false 关
     * @param pos 设备号
     * 0:未定义 1:咖啡机  2：电冰箱  3：影音娱乐  4：小桌板  5：吧台灯  6：前射灯  7：后射灯  8：阅读灯一  9：阅读灯二 10：电动门  11：顶灯
     */
    public byte[] getDLDGOrder(int pos,boolean open){
        byte state = (byte)(open?0x31:0x30);
        byte no = DLDevicesNo[pos] ;
        return getDanLuCtrl(no,state) ;
    }

    /**
     * RGB灯的控制的命令
     * @param  open true 开    false 关
     * @param  pos  1：上氛围灯  2：中氛围灯  3：下氛围灯
     * @param  breath 模式 1:呼吸  2：不呼吸
     * @param  mode 1: 单色  2：循环色
     * @param open
     */
    public byte[] getRGBLightOrder(int pos , boolean open , int breath ,int mode ,String color){
        byte state = (byte)(open?0x31:0x30);
        byte no = DLDevicesNo[pos] ;
        byte brth = Gears[breath] ;
        byte md = Gears[mode] ;
        byte[] colors = new byte[3];
        return getSiLuCtrl(no,state,brth,md,colors);
    }


    /**
     * 吸烟的命令
     * @param open  true 开    false 关
     */
    public byte[] getXYOrder(boolean open){
        byte no = 0x01 ;
        byte state = (byte)(open?0x31:0x30);
        byte gear = 0x32 ;
        return getSanLuCtrl(no,state,gear);
    }

    public byte[] Gears = {0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39};
    /**
     * 新风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public byte[] getXOrder(boolean open , int gear){
        byte no = 0x02 ;
        byte state = (byte)(open?0x31:0x30);
        return getSanLuCtrl(no,state,Gears[gear]);
    }

    /**
     * 冷风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public byte[] getLFOrder(boolean open , int gear){
        byte no = 0x03 ;
        byte state = (byte)(open?0x31:0x30);
        return getSanLuCtrl(no,state,Gears[gear]);
    }

    /**
     * 暖风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public byte[] getNFOrder(boolean open , int gear){
        byte no = 0x04 ;
        byte state = (byte)(open?0x31:0x30);
        return getSanLuCtrl(no,state,Gears[gear]);
    }

    public interface OnBottomMachineOrderListener{
        void onDealReceiceMsg(String msg);
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
     * @param breath 呼吸模式
     * @param mode 单色&循环色
     * @param rgb RGB值
     * @return
     */
    public byte[] getSiLuCtrl(byte no, byte state, byte breath, byte mode , byte[] rgb){
        byte[] ord = new byte[14];
        ord[0] = SOH ;
        ord[1] = 0x31;
        ord[2] = 0x0a;
        ord[3] = 0x40;
        ord[4] = no;
        ord[5] = state;
        ord[6] = breath ;
        ord[7] = mode ;

        ord[8] = rgb[0];
        ord[9] = rgb[1];
        ord[10] = rgb[2] ;

        ord[12] = EOH[0];
        ord[13] = EOH[1];
        ord[11] = getHexCheck(ord);
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
}
