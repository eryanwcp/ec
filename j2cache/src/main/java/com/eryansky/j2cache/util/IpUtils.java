/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.j2cache.util;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;

/**
 *
 * IP地址.
 * @author Eryan
 * @date : 2018-09-13
 */
public class IpUtils {

    private static final String LOCAL_IP = getActivityLocalIp();

    private IpUtils() {
    }

    public static String getActivityLocalIp() {
        InetAddress inetAddress;
        String local = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            local = InetAddresses.toAddrString(inetAddress);
        } catch (Exception e) {
        }
        return "127.0.0.1".equals(local) ? "" : local;
    }

    /**
     * 从InetAddress转化到int, 传输和存储时, 用int代表InetAddress是最小的开销.
     *
     * InetAddress可以是IPV4或IPV6，都会转成IPV4.
     *
     * @see com.google.common.net.InetAddresses#coerceToInteger(InetAddress)
     */
    public static int toInt(InetAddress address) {
        return InetAddresses.coerceToInteger(address);
    }

    /**
     * InetAddress转换为String.
     *
     * InetAddress可以是IPV4或IPV6. 其中IPV4直接调用getHostAddress()
     *
     * @see com.google.common.net.InetAddresses#toAddrString(InetAddress)
     */
    public static String toIpString(InetAddress address) {
        return InetAddresses.toAddrString(address);
    }


    /**
     * 检测IP是否匹配
     *
     * @param pattern *.*.*.* , 192.168.1.0-255 , *
     * @param address
     *            - 192.168.1.1<BR>
     *            <code>address = 10.2.88.12  pattern = *.*.*.*   result: true<BR>
     *                address = 10.2.88.12  pattern = *   result: true<BR>
     *                address = 10.2.88.12  pattern = 10.2.88.12-13   result: true<BR>
     *                address = 10.2.88.12  pattern = 10.2.88.13-125   result: false<BR></code>
     * @return true if address match pattern
     */
    public static boolean checkIPMatching(String pattern, String address) {
        if (pattern.equals("*.*.*.*") || pattern.equals("*"))
            return true;

        String[] mask = pattern.split("\\.");
        String[] ip_address = address.split("\\.");
        for (int i = 0; i < mask.length; i++) {
            if (mask[i].equals("*") || mask[i].equals(ip_address[i]))
                continue;
            else if (mask[i].contains("-")) {
                int min = Integer.parseInt(mask[i].split("-")[0]);
                int max = Integer.parseInt(mask[i].split("-")[1]);
                // if (max > 255) max = 255;
                int ip = Integer.parseInt(ip_address[i]);
                if (ip < min || ip > max)
                    return false;
            } else
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(IpUtils.checkIPMatching("192.168.1","192.168.1.1"));
    }

}