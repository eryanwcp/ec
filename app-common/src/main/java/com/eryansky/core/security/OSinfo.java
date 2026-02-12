/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *          Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security;

/**
 * 操作系统类：
 * 获取System.getProperty("os.name")对应的操作系统
 * Author: eryan
 * Date: 2014-02-19 10:33
 */
public class OSinfo {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static final OSinfo _instance = new OSinfo();

    private EPlatform platform;

    private OSinfo(){}

    public static boolean isLinux(){
        return OS.contains("linux");
    }

    public static boolean isMacOS(){
        return OS.contains("mac") &&OS.indexOf("os")>0&& !OS.contains("x");
    }

    public static boolean isMacOSX(){
        return OS.contains("mac") &&OS.indexOf("os")>0&&OS.indexOf("x")>0;
    }

    public static boolean isWindows(){
        return OS.contains("windows");
    }

    public static boolean isOS2(){
        return OS.contains("os/2");
    }

    public static boolean isSolaris(){
        return OS.contains("solaris");
    }

    public static boolean isSunOS(){
        return OS.contains("sunos");
    }

    public static boolean isMPEiX(){
        return OS.contains("mpe/ix");
    }

    public static boolean isHPUX(){
        return OS.contains("hp-ux");
    }

    public static boolean isAix(){
        return OS.contains("aix");
    }

    public static boolean isOS390(){
        return OS.contains("os/390");
    }

    public static boolean isFreeBSD(){
        return OS.contains("freebsd");
    }

    public static boolean isIrix(){
        return OS.contains("irix");
    }

    public static boolean isDigitalUnix(){
        return OS.contains("digital") && OS.indexOf("unix")>0;
    }

    public static boolean isNetWare(){
        return OS.contains("netware");
    }

    public static boolean isOSF1(){
        return OS.contains("osf1");
    }

    public static boolean isOpenVMS(){
        return OS.contains("openvms");
    }

    /**
     * 获取操作系统名字
     * @return 操作系统名
     */
    public static EPlatform getOSname(){
        if(isAix()){
            _instance.platform = EPlatform.AIX;
        }else if (isDigitalUnix()) {
            _instance.platform = EPlatform.Digital_Unix;
        }else if (isFreeBSD()) {
            _instance.platform = EPlatform.FreeBSD;
        }else if (isHPUX()) {
            _instance.platform = EPlatform.HP_UX;
        }else if (isIrix()) {
            _instance.platform = EPlatform.Irix;
        }else if (isLinux()) {
            _instance.platform = EPlatform.Linux;
        }else if (isMacOS()) {
            _instance.platform = EPlatform.Mac_OS;
        }else if (isMacOSX()) {
            _instance.platform = EPlatform.Mac_OS_X;
        }else if (isMPEiX()) {
            _instance.platform = EPlatform.MPEiX;
        }else if (isNetWare()) {
            _instance.platform = EPlatform.NetWare_411;
        }else if (isOpenVMS()) {
            _instance.platform = EPlatform.OpenVMS;
        }else if (isOS2()) {
            _instance.platform = EPlatform.OS2;
        }else if (isOS390()) {
            _instance.platform = EPlatform.OS390;
        }else if (isOSF1()) {
            _instance.platform = EPlatform.OSF1;
        }else if (isSolaris()) {
            _instance.platform = EPlatform.Solaris;
        }else if (isSunOS()) {
            _instance.platform = EPlatform.SunOS;
        }else if (isWindows()) {
            _instance.platform = EPlatform.Windows;
        }else{
            _instance.platform = EPlatform.Others;
        }
        return _instance.platform;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(OSinfo.getOSname());
        System.out.println(OSinfo.isWindows());
    }

    /**
     * 平台
     * @author isea533
     */
    public enum EPlatform {
        Any("any"),
        Linux("Linux"),
        Mac_OS("Mac OS"),
        Mac_OS_X("Mac OS X"),
        Windows("Windows"),
        OS2("OS/2"),
        Solaris("Solaris"),
        SunOS("SunOS"),
        MPEiX("MPE/iX"),
        HP_UX("HP-UX"),
        AIX("AIX"),
        OS390("OS/390"),
        FreeBSD("FreeBSD"),
        Irix("Irix"),
        Digital_Unix("Digital Unix"),
        NetWare_411("NetWare"),
        OSF1("OSF1"),
        OpenVMS("OpenVMS"),
        Others("Others");

        private EPlatform(String desc){
            this.description = desc;
        }

        public String toString(){
            return description;
        }

        private final String description;
    }
}