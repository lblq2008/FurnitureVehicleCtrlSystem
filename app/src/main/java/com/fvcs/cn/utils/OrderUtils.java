package com.fvcs.cn.utils;

/**
 * 命令生成的工具类
 * Created by liubo on 2018/5/8.
 */

public class OrderUtils {

    /**
     * 小桌板的命令
     * true 开    false 关
     * @param open
     */
    public static String getXZBOrder(boolean open){
        String order = "" ;
        return order ;
    }

    /**
     * 咖啡机的命令
     * true 开    false 关
     * @param open
     */
    public static String getCFJOrder(boolean open){
        String order = "" ;
        return order ;
    }

    /**
     * 影音娱乐的命令
     * true 开    false 关
     * @param open
     */
    public static String getYYYLOrder(boolean open){
        String order = "" ;
        return order ;
    }

    /**
     * 电冰箱的命令
     * true 开    false 关
     * @param open
     */
    public static String getDBXOrder(boolean open){
        String order = "" ;
        return order ;
    }

    /**
     * 电视机的命令
     * true 升    false 降
     * @param open
     */
    public static String getDSJOrder(boolean open){
        String order = "" ;
        return order ;
    }

    /**
     * 窗帘的命令
     * true 升    false 降
     * pos 0：全体 1：前左  2：前右  3：中左  4：中右  5：后左  6：后右
     * @param open
     */
    public static String getOrder(int pos ,boolean open){
        String order = "" ;
        return order ;
    }


    /**
     * 单路灯光的命令
     * true 开    false 关
     * 1：吧台灯  2：前射灯  3：后射灯  4：阅读灯一  5：阅读灯二
     * @param open
     */
    public static String getDLDGOrder(boolean open){
        String order = "" ;
        return order ;
    }

    /**
     * RGB灯的控制的命令
     * true 开    false 关
     * pos:   1：上氛围灯  2：中氛围灯  3：下氛围灯  4：顶灯
     * type: 颜色的类型
     * @param open
     */
    public static String getRGBLightOrder(int pos , boolean open , int type){
        String order = "" ;
        return order ;
    }

    /**
     * 吸烟的命令
     * true 开    false 关
     * @param open
     */
    public static String getXYOrder(boolean open){
        String order = "" ;
        return order ;
    }

    /**
     * 新风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public static String getXOrder(boolean open , int gear){
        String order = "" ;
        return order ;
    }

    /**
     * 冷风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public static String getLFOrder(boolean open , int gear){
        String order = "" ;
        return order ;
    }

    /**
     * 暖风的命令
     * true 开    false 关
     * gear 档位
     * @param open
     */
    public static String getNFOrder(boolean open , int gear){
        String order = "" ;
        return order ;
    }

}
