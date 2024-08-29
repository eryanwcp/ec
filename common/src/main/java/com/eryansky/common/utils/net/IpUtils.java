/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.net;

import com.eryansky.common.utils.NumberUtil;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.google.common.net.InetAddresses;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 *
 * 主要包含int, String/IPV4String, InetAdress/Inet4Address之间的互相转换；客户端IP地址获取.
 *
 * 先将字符串传换为byte[]再用InetAddress.getByAddress(byte[])，避免了InetAddress.getByName(ip)可能引起的DNS访问.
 *
 * InetAddress与String的转换其实消耗不小，如果是有限的地址，建议进行缓存.
 * @author Eryan
 * @date : 2014-04-19 17:38
 */
public class IpUtils {

    private static final String IP_UNKNOW = "unknown";

    private IpUtils() {
    }

    /**
     * 获取客户端IP
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return IP_UNKNOW;
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_VIA");
        }
        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("REMOTE_ADDR");
        }

        if (ip == null || ip.length() == 0 || IP_UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (StringUtils.isNotBlank(ip)){
            ip = EncodeUtils.xssFilter(ip);
        }
        if (StringUtils.isBlank(ip)){
            ip = IP_UNKNOW;
        }
        return ip;
    }

    /**
     * 获取客户端IP （有多个时，仅取第一个）
     * @param request
     * @return
     */
    public static String getIpAddr0(HttpServletRequest request) {
        String ip = getIpAddr(request);
        if (StringUtils.isNotBlank(ip) && !IP_UNKNOW.equalsIgnoreCase(ip)){
            ip = StringUtils.split(ip, ",")[0];
        }
        if (StringUtils.isBlank(ip)){
            ip = IP_UNKNOW;
        }
        return ip;
    }

    /**
     * 获取本地有效IP
     * @return
     */
    public static String getActivityLocalIp(){
        InetAddress inetAddress;
        String local = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            local = InetAddresses.toAddrString(inetAddress);
        } catch (Exception e) {
        }
        return "127.0.0.1".equals(local) ? "":local;
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
     * 从int转换为Inet4Address(仅支持IPV4)
     */
    public static Inet4Address fromInt(int address) {
        return InetAddresses.fromInteger(address);
    }

    /**
     * 从String转换为InetAddress.
     *
     * IpString可以是ipv4 或 ipv6 string, 但不可以是域名.
     *
     * 先字符串传换为byte[]再调getByAddress(byte[])，避免了调用getByName(ip)可能引起的DNS访问.
     */
    public static InetAddress fromIpString(String address) {
        return InetAddresses.forString(address);
    }

    /**
     * 从IPv4String转换为InetAddress.
     *
     * IpString如果确定ipv4, 使用本方法减少字符分析消耗 .
     *
     * 先字符串传换为byte[]再调getByAddress(byte[])，避免了调用getByName(ip)可能引起的DNS访问.
     */
    public static Inet4Address fromIpv4String(String address) {
        byte[] bytes = ip4StringToBytes(address);
        if (bytes == null) {
            return null;
        } else {
            try {
                return (Inet4Address) Inet4Address.getByAddress(bytes);
            } catch (UnknownHostException e) {
                throw new AssertionError(e);
            }
        }
    }

    /**
     * int转换到IPV4 String, from Netty NetUtil
     */
    public static String intToIpv4String(int i) {
        return new StringBuilder(15).append((i >> 24) & 0xff).append('.').append((i >> 16) & 0xff).append('.')
                .append((i >> 8) & 0xff).append('.').append(i & 0xff).toString();
    }

    /**
     * Ipv4 String 转换到int
     */
    public static int ipv4StringToInt(String ipv4Str) {
        byte[] byteAddress = ip4StringToBytes(ipv4Str);
        if (byteAddress == null) {
            return 0;
        } else {
            return NumberUtil.toInt(byteAddress);
        }
    }

    /**
     * Ipv4 String 转换到byte[]
     */
    private static byte[] ip4StringToBytes(String ipv4Str) {
        if (ipv4Str == null) {
            return null;
        }

        List<String> it = StringUtils.split(ipv4Str, '.', 4);
        if (it.size() != 4) {
            return null;
        }

        byte[] byteAddress = new byte[4];
        for (int i = 0; i < 4; i++) {
            int tempInt = Integer.parseInt(it.get(i));
            if (tempInt > 255) {
                return null;
            }
            byteAddress[i] = (byte) tempInt;
        }
        return byteAddress;
    }

    /**
     * 是否是本地地址
     * @param ip
     * @return
     */
    public static boolean isLocalAddr(String ip){
        return StringUtils.inString(ip, "127.0.0.1", "0:0:0:0:0:0:0:1");
    }

    /**
     * 判断IP地址为内网IP还是公网IP
     *
     * tcp/ip协议中，专门保留了三个IP地址区域作为私有地址，其地址范围如下：
     * 10.0.0.0/8：10.0.0.0～10.255.255.255
     * 172.16.0.0/12：172.16.0.0～172.31.255.255
     * 192.168.0.0/16：192.168.0.0～192.168.255.255
     *
     * @param ip
     * @return
     */
    public static boolean isInternalAddr(String ip) {

        if (isLocalAddr(ip)){
            return true;
        }

        byte[] addr = textToNumericFormatV4(ip);

        final byte b0 = addr[0];
        final byte b1 = addr[1];
        //10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        //172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        //192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0) {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                    return true;
                }
            case SECTION_5:
                switch (b1) {
                    case SECTION_6:
                        return true;
                }
            default:
                return false;
        }
    }

    public static byte[] textToNumericFormatV4(String paramString) {
        if (paramString.isEmpty()) {
            return null;
        }
        byte[] arrayOfByte = new byte[4];
        String[] arrayOfString = paramString.split("\\.", -1);
        try {
            long l;
            int i;
            switch (arrayOfString.length) {
                case 1:
                    l = Long.parseLong(arrayOfString[0]);
                    if ((l < 0L) || (l > 4294967295L)) {
                        return null;
                    }
                    arrayOfByte[0] = ((byte) (int) (l >> 24 & 0xFF));
                    arrayOfByte[1] = ((byte) (int) ((l & 0xFFFFFF) >> 16 & 0xFF));
                    arrayOfByte[2] = ((byte) (int) ((l & 0xFFFF) >> 8 & 0xFF));
                    arrayOfByte[3] = ((byte) (int) (l & 0xFF));
                    break;
                case 2:
                    l = Integer.parseInt(arrayOfString[0]);
                    if ((l < 0L) || (l > 255L)) {
                        return null;
                    }
                    arrayOfByte[0] = ((byte) (int) (l & 0xFF));
                    l = Integer.parseInt(arrayOfString[1]);
                    if ((l < 0L) || (l > 16777215L)) {
                        return null;
                    }
                    arrayOfByte[1] = ((byte) (int) (l >> 16 & 0xFF));
                    arrayOfByte[2] = ((byte) (int) ((l & 0xFFFF) >> 8 & 0xFF));
                    arrayOfByte[3] = ((byte) (int) (l & 0xFF));
                    break;
                case 3:
                    for (i = 0; i < 2; i++) {
                        l = Integer.parseInt(arrayOfString[i]);
                        if ((l < 0L) || (l > 255L)) {
                            return null;
                        }
                        arrayOfByte[i] = ((byte) (int) (l & 0xFF));
                    }
                    l = Integer.parseInt(arrayOfString[2]);
                    if ((l < 0L) || (l > 65535L)) {
                        return null;
                    }
                    arrayOfByte[2] = ((byte) (int) (l >> 8 & 0xFF));
                    arrayOfByte[3] = ((byte) (int) (l & 0xFF));
                    break;
                case 4:
                    for (i = 0; i < 4; i++) {
                        l = Integer.parseInt(arrayOfString[i]);
                        if ((l < 0L) || (l > 255L)) {
                            return null;
                        }
                        arrayOfByte[i] = ((byte) (int) (l & 0xFF));
                    }
                    break;
                default:
                    return null;
            }
        } catch (NumberFormatException localNumberFormatException) {
            return null;
        }
        return arrayOfByte;
    }

    public static byte[] textToNumericFormatV6(String paramString) {
        if (paramString.length() < 2) {
            return null;
        }
        char[] arrayOfChar = paramString.toCharArray();
        byte[] arrayOfByte1 = new byte[16];

        int m = arrayOfChar.length;
        int n = paramString.indexOf("%");
        if (n == m - 1) {
            return null;
        }
        if (n != -1) {
            m = n;
        }
        int i = -1;
        int i1 = 0;
        int i2 = 0;
        if ((arrayOfChar[i1] == ':') && (arrayOfChar[(++i1)] != ':')) {
            return null;
        }
        int i3 = i1;
        int j = 0;
        int k = 0;
        int i4;
        while (i1 < m) {
            char c = arrayOfChar[(i1++)];
            i4 = Character.digit(c, 16);
            if (i4 != -1) {
                k <<= 4;
                k |= i4;
                if (k > 65535) {
                    return null;
                }
                j = 1;
            } else if (c == ':') {
                i3 = i1;
                if (j == 0) {
                    if (i != -1) {
                        return null;
                    }
                    i = i2;
                } else {
                    if (i1 == m) {
                        return null;
                    }
                    if (i2 + 2 > 16) {
                        return null;
                    }
                    arrayOfByte1[(i2++)] = ((byte) (k >> 8 & 0xFF));
                    arrayOfByte1[(i2++)] = ((byte) (k & 0xFF));
                    j = 0;
                    k = 0;
                }
            } else if ((c == '.') && (i2 + 4 <= 16)) {
                String str = paramString.substring(i3, m);

                int i5 = 0;
                int i6 = 0;
                while ((i6 = str.indexOf('.', i6)) != -1) {
                    i5++;
                    i6++;
                }
                if (i5 != 3) {
                    return null;
                }
                byte[] arrayOfByte3 = textToNumericFormatV4(str);
                if (arrayOfByte3 == null) {
                    return null;
                }
                for (int i7 = 0; i7 < 4; i7++) {
                    arrayOfByte1[(i2++)] = arrayOfByte3[i7];
                }
                j = 0;
            } else {
                return null;
            }
        }
        if (j != 0) {
            if (i2 + 2 > 16) {
                return null;
            }
            arrayOfByte1[(i2++)] = ((byte) (k >> 8 & 0xFF));
            arrayOfByte1[(i2++)] = ((byte) (k & 0xFF));
        }
        if (i != -1) {
            i4 = i2 - i;
            if (i2 == 16) {
                return null;
            }
            for (i1 = 1; i1 <= i4; i1++) {
                arrayOfByte1[(16 - i1)] = arrayOfByte1[(i + i4 - i1)];
                arrayOfByte1[(i + i4 - i1)] = 0;
            }
            i2 = 16;
        }
        if (i2 != 16) {
            return null;
        }
        byte[] arrayOfByte2 = convertFromIPv4MappedAddress(arrayOfByte1);
        if (arrayOfByte2 != null) {
            return arrayOfByte2;
        }
        return arrayOfByte1;
    }

    public static boolean isIPv4LiteralAddress(String paramString) {
        return textToNumericFormatV4(paramString) != null;
    }

    public static boolean isIPv6LiteralAddress(String paramString) {
        return textToNumericFormatV6(paramString) != null;
    }

    public static byte[] convertFromIPv4MappedAddress(byte[] paramArrayOfByte) {
        if (isIPv4MappedAddress(paramArrayOfByte)) {
            byte[] arrayOfByte = new byte[4];
            System.arraycopy(paramArrayOfByte, 12, arrayOfByte, 0, 4);
            return arrayOfByte;
        }
        return null;
    }

    private static boolean isIPv4MappedAddress(byte[] paramArrayOfByte) {
        if (paramArrayOfByte.length < 16) {
            return false;
        }
        if ((paramArrayOfByte[0] == 0) && (paramArrayOfByte[1] == 0) && (paramArrayOfByte[2] == 0) && (paramArrayOfByte[3] == 0)
                && (paramArrayOfByte[4] == 0) && (paramArrayOfByte[5] == 0) && (paramArrayOfByte[6] == 0) && (paramArrayOfByte[7] == 0)
                && (paramArrayOfByte[8] == 0) && (paramArrayOfByte[9] == 0) && (paramArrayOfByte[10] == -1) && (paramArrayOfByte[11] == -1)) {
            return true;
        }
        return false;
    }
}