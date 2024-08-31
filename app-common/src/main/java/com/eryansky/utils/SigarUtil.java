/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.utils;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author: eryan
 * @date: 2013-11-27 下午9:01
 */
public class SigarUtil {

    private static final Logger logger = LoggerFactory.getLogger(SigarUtil.class);

    public static void main(String[] args) {
        try {
            System.out.println(JsonMapper.nonDefaultMapper().toJson(SigarUtil.getServerStatus()));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    // 磁盘读写初始数据 用于计算读写速率
    private static Map<String, String> diskWritesAndReadsOnInit = new HashMap<>();


    /**
     * 返回服务系统信息
     * @throws Exception
     */
    public static ServerStatus getServerStatus() throws Exception {
        ServerStatus status = new ServerStatus();
        status.setServerTime(DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));
        status.setServerName(System.getenv().get("COMPUTERNAME"));
        if (StringUtils.isBlank(status.getServerName())) {
            try {
                status.setServerName(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                status.setServerName("unknown");
            }
        }

        Runtime rt = Runtime.getRuntime();
        status.setIp(IpUtils.getActivityLocalIp());
        status.setJvmTotalMem(rt.totalMemory() / (1024 * 1024));
        status.setJvmFreeMem(rt.freeMemory() / (1024 * 1024));
        status.setJvmMaxMem(rt.maxMemory() / (1024 * 1024));
        Properties props = System.getProperties();
        status.setServerOs(props.getProperty("os.name") + " " + props.getProperty("os.arch") + " " + props.getProperty("os.version"));
        status.setJavaHome(props.getProperty("java.home"));
        status.setJavaVersion(props.getProperty("java.version"));
        status.setJavaTmpPath(props.getProperty("java.io.tmpdir"));
        SystemInfo systemInfo = null;
        try {
            systemInfo = new SystemInfo();
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            CentralProcessor cpu = hal.getProcessor();
            Map<String,Object> map = Maps.newHashMap();
            map.put("hal",hal);
            map.put("cpu",cpu);
            status.setContent(JsonMapper.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(map));
            getServerCpuInfo(systemInfo, status);
            getServerDiskInfo(systemInfo, status);
            getServerMemoryInfo(systemInfo, status);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return status;
    }

    public static void getServerCpuInfo(SystemInfo sigar, ServerStatus status) {

    }


    public static void getServerMemoryInfo(SystemInfo sigar, ServerStatus status) {

    }

    public static void getServerDiskInfo(SystemInfo sigar, ServerStatus status) {

    }
}
