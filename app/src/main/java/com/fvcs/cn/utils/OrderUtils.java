package com.fvcs.cn.utils;

import android.graphics.Color;
import android.text.TextUtils;

/**
 * 命令生成的工具类
 * Created by liubo on 2018/5/8.
 */

public class OrderUtils {

    public byte SOH = 0x78 ;
    public byte[] EOH = {0x0d,0x0a};
    public static byte TIME = 0x05 ;

    public static void setKeepTime(){
        int t = PreferenceUtils.getInstance().getIntValue("clickKeepTime");
        if(t == 0){
            TIME = 0x05;
        }else{
            TIME = (byte) t;
        }
    }


    OnBottomMachineOrderListener listener ;

    public OnBottomMachineOrderListener getListener() {
        return listener;
    }

    public void setListener(OnBottomMachineOrderListener listener) {
        this.listener = listener;
    }

//      0	  1	       2	   3	  4	      5	      6	       7	  8	      9	       10	  11	  12	   13	  14	  15	  16	  17	  18	  19	20
//    咖啡机	电冰箱	影音娱乐	小桌板	吧台灯	前射灯	后射灯	阅读灯一	阅读灯二	电动门	吸烟系统	新风系统	新风档位	冷风系统	冷风档位	暖风系统	暖风档位	上氛围灯	中氛围灯	下氛围灯	顶灯

//0：代表关  1代表开   1234等代表档位
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

//    public byte[] CTNos = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08};
public byte[] CTNos = {0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,0x29,0x2A};
    /**
     * 窗帘的命令
     * true 升    false 降
     * pos 0：全体 1：前左  2：前右  3：中左  4：中右  5：后左  6：后右  7:电视机 8：小桌板  9:电冰箱前后
     * @param open
     */
    public byte[] getCLOrder(int pos ,boolean open){
        byte state = (byte)(open?0x31:0x30);
        byte no = CTNos[pos] ;
        return getShuangLuCtrl(no,state,TIME) ;
    }

 //   * 0:顶灯  1：吧台灯  2：前射灯  3：后射灯  4：阅读灯一  5：阅读灯二
//    0x11	咖啡机   1
//    0x12	电冰箱   2
//    0x13	影音娱乐  3
//    0x14	迈巴赫顶灯  4
//    0x15	吧台灯  5
//    0x16	前射灯  6
//    0x17	后射灯  7
//    0x18	阅读灯一 8
//    0x19	阅读灯二 9
//    0x1a	电动门   10
//    0x1b	上气氛灯  11
//    0x1c	中气围灯  12
//    0x1d	上气围灯  13
//    0x1e	逆变器    14


    //public byte[] DLDevicesNo = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B};
    //                             0   1    2     3   4    5    6    7    8    9    10    11  12   13   14   15  16
    public byte[] DLDevicesNo = {0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x1A,0x1B,0x1C,0x1D,0x1E,0x2A,0x2B};
    //                                咖啡机
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

//    /**
//     * RGB灯的控制的命令
//     * @param  open true 开    false 关
//     * @param  pos  1：上氛围灯  2：中氛围灯  3：下氛围灯
//     * @param  breath 模式 1:呼吸  2：不呼吸
//     * @param  mode 1: 单色  2：循环色
//     * @param open
//     */
//    public byte[] getRGBLightOrder(int pos , boolean open , int breath ,int mode ,String color){
//        byte state = (byte)(open?0x31:0x30);
//        byte no = DLDevicesNo[pos] ;
//        byte brth = Gears[breath] ;
//        byte md = Gears[mode] ;
//        byte[] colors = new byte[3];
//        return getSiLuCtrl(no,state,brth,md,colors);
//    }

    /**
     * RGB灯的控制的命令
     * @param  open true 开    false 关
     * @param  pos  1：上氛围灯  2：中氛围灯  3：下氛围灯
     * @param  mode 1: 单色  2：呼吸   3：循环色
     * @param open
     */
    public byte[] getRGBLightOrder(int pos , boolean open ,int mode ,String color){
        byte state = (byte)(open?0x31:0x30);
        byte no = DLDevicesNo[pos] ;
        byte md = Gears[mode] ;
        byte[] colors = getByteColor(pos,color);
        return getSiLuCtrl(no,state,md,colors);
    }

    public byte[] getByteColor(int mode ,String color){
        int c = Integer.parseInt(color);
        byte[] colors = new byte[3];
        colors[0] = (byte)Color.red(c);
        colors[1] = (byte)Color.green(c);
        colors[2] = (byte)Color.blue(c);
        return colors;
    }

//    public byte[] getByteColor(int mode ,String color){
//        byte[] colors = new byte[3];
//        switch (mode){
//            case 1:
//                colors = new byte[]{0x15, (byte) 0xc5, (byte) 0xb4};
//                break;
//            case 2:
//                colors = new byte[]{0x00, (byte) 0xff, (byte) 0xaa};
//                break;
//            case 3:
//                colors = new byte[]{0x15, (byte) 0xc5, (byte) 0xb4};
//                break;
//
//        }
//        return colors;
//    }


    /**
     * 吸烟的命令
     * @param open  true 开    false 关
     */
    public byte[] getXYOrder(boolean open){
        byte no = 0x31 ;
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
    public byte[] getXFOrder(boolean open , int gear){
        byte no = 0x32 ;
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
        byte no = 0x33 ;
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
        byte no = 0x34 ;
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
     * @param mode 单色&循环色
     * @param rgb RGB值
     * @return
     */
    public byte[] getSiLuCtrl(byte no, byte state,  byte mode , byte[] rgb){
        byte[] ord = new byte[13];
        ord[0] = SOH ;
        ord[1] = 0x31;
        ord[2] = 0x0a;
        ord[3] = 0x40;
        ord[4] = no;
        ord[5] = state;
       // ord[6] = breath ;
        ord[6] = mode ;

        ord[7] = rgb[0];
        ord[8] = rgb[1];
        ord[9] = rgb[2] ;

        ord[11] = EOH[0];
        ord[12] = EOH[1];
        ord[10] = getHexCheck(ord);
        return ord ;
    }

//    /**
//     * 四路控制命令
//     * @param no 设备号
//     * @param state 状态
//     * @param breath 呼吸模式
//     * @param mode 单色&循环色
//     * @param rgb RGB值
//     * @return
//     */
//    public byte[] getSiLuCtrl(byte no, byte state, byte breath, byte mode , byte[] rgb){
//        byte[] ord = new byte[14];
//        ord[0] = SOH ;
//        ord[1] = 0x31;
//        ord[2] = 0x0a;
//        ord[3] = 0x40;
//        ord[4] = no;
//        ord[5] = state;
//        ord[6] = breath ;
//        ord[7] = mode ;
//
//        ord[8] = rgb[0];
//        ord[9] = rgb[1];
//        ord[10] = rgb[2] ;
//
//        ord[12] = EOH[0];
//        ord[13] = EOH[1];
//        ord[11] = getHexCheck(ord);
//        return ord ;
//    }


    /**
     * 计算和校验 除帧头帧尾的和的低八位
     * @param ord
     * @return
     */
    public byte getHexCheck(byte[] ord){
        int sum = 0x00 ;
        for (int i = 1; i <ord.length-3 ; i++) {
            sum = sum + ord[i];
        }
        sum = sum % 0x100;
        return (byte)sum;
    }
}
