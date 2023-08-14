/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.utils;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.ThreadUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.hyperic.sigar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
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
    private static long initTime;
    private static boolean init = false;
    static {
        init();
    }

    private static void  init(){
        initTime = System.currentTimeMillis();
        resetClasspath();
        Sigar sigar = null;
        try {

            sigar = new Sigar();
            FileSystem[] fslist = sigar.getFileSystemList();
            FileSystemUsage usage = null;
            for (int i = 0; i < fslist.length; i++) {
                FileSystem fs = fslist[i];
                if (fs.getType() != 2)
                    continue;
                usage = sigar.getFileSystemUsage(fs.getDirName());
                diskWritesAndReadsOnInit.put(fs.getDevName(), usage.getDiskReadBytes() + "|" + usage.getDiskWriteBytes());
            }
            init = true;
        } catch (Exception e) {
            logger.error("java.library.path:{}",System.getProperty("java.library.path"));
            logger.error(e.getMessage(),e);
            init = false;
        } finally {
            if (sigar != null){
                sigar.close();
            }
        }
    }

    /**
     * ， 重新设置CLASSPATH,加入sigar，以支持dll,so等文件的加入与读取
     */
    private static void resetClasspath() {
        String sigarClasspath = SigarUtil.class.getResource("/").getPath() + "sigar";
        try {
            addLibraryDir(sigarClasspath);
            logger.info("reset java.library.path={}",System.getProperty("java.library.path"));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

    }

    private static void addLibraryDir(String libraryPath) throws Exception {
        Field userPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        userPathsField.setAccessible(true);
        String[] paths = (String[]) userPathsField.get(null);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            if (libraryPath.equals(paths[i])) {
                continue;
            }
//            sb.append(paths[i]).append(';');
            sb.append(paths[i]).append(File.pathSeparator);
        }
        sb.append(libraryPath);
        System.setProperty("java.library.path", sb.toString());
        final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);
    }

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
        status.setJvmMaxMem(rt.maxMemory()/ (1024 * 1024));
        Properties props = System.getProperties();
        status.setServerOs(props.getProperty("os.name") + " " + props.getProperty("os.arch") + " " + props.getProperty("os.version"));
        status.setJavaHome(props.getProperty("java.home"));
        status.setJavaVersion(props.getProperty("java.version"));
        status.setJavaTmpPath(props.getProperty("java.io.tmpdir"));

        Sigar sigar = null;
        try {
            if(!init){
                init();
            }
            sigar = new Sigar();
            getServerCpuInfo(sigar, status);
            getServerDiskInfo(sigar, status);
            getServerMemoryInfo(sigar, status);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }finally {
            if (sigar != null){
                sigar.close();
            }
        }

        return status;
    }

    public static void getServerCpuInfo(Sigar sigar, ServerStatus status) throws SigarException {
        CpuInfo[] infos = sigar.getCpuInfoList();
        CpuPerc[] cpuList = sigar.getCpuPercList();
        double totalUse = 0L;
        for (int i = 0; i < infos.length; i++) {
            CpuPerc perc = cpuList[i];
            ServerStatus.CpuInfoVo cpuInfo = new ServerStatus.CpuInfoVo();
            cpuInfo.setId(infos[i].hashCode() + "");
            cpuInfo.setCacheSize(infos[i].getCacheSize());
            cpuInfo.setModel(infos[i].getModel());
            cpuInfo.setUsed(CpuPerc.format(perc.getCombined()));
            cpuInfo.setUsedOrigVal(perc.getCombined());
            cpuInfo.setIdle(CpuPerc.format(perc.getIdle()));
            cpuInfo.setTotalMHz(infos[i].getMhz());
            cpuInfo.setVendor(infos[i].getVendor());
            status.getCpuInfos().add(cpuInfo);
            totalUse += perc.getCombined();
        }
        String cpuu = CpuPerc.format(totalUse / status.getCpuInfos().size());
        cpuu = cpuu.substring(0,cpuu.length()-1);
        status.setCpuUsage(cpuu);
    }


    public static void getServerMemoryInfo(Sigar sigar, ServerStatus status) throws SigarException {
        Mem mem = sigar.getMem();
        status.setTotalMem(mem.getTotal() / (1024 * 1024));
        status.setUsedMem(mem.getUsed() / (1024 * 1024));
        status.setFreeMem(mem.getFree() / (1024 * 1024));
        // 交换区
        Swap swap = sigar.getSwap();
        status.setTotalSwap(swap.getTotal() / (1024 * 1024));
        status.setUsedSwap(swap.getUsed() / (1024 * 1024));
        status.setFreeSwap(swap.getFree() / (1024 * 1024));
    }

    public static void getServerDiskInfo(Sigar sigar, ServerStatus status) throws SigarException {
        FileSystem[] fslist = sigar.getFileSystemList();
        FileSystemUsage usage = null;
        for (int i = 0; i < fslist.length; i++) {
            FileSystem fs = fslist[i];
            switch (fs.getType()) {
                case 0: // TYPE_UNKNOWN ：未知
                case 1: // TYPE_NONE
                case 3:// TYPE_NETWORK ：网络
                case 4:// TYPE_RAM_DISK ：闪存
                case 5:// TYPE_CDROM ：光驱
                case 6:// TYPE_SWAP ：页面交换
                    break;
                case 2: // TYPE_LOCAL_DISK : 本地硬盘
                    ServerStatus.DiskInfoVo disk = new ServerStatus.DiskInfoVo();
                    disk.setDevName(fs.getDevName());
                    disk.setDirName(fs.getDirName());
                    usage = sigar.getFileSystemUsage(fs.getDirName());
                    disk.setTotalSize(usage.getTotal() / (1024 * 1024));
                    // disk.setFreeSize(usage.getFree()/(1024*1024));
                    disk.setAvailSize(usage.getAvail() / (1024 * 1024));
                    disk.setUsedSize(usage.getUsed() / (1024 * 1024));
                    disk.setUsePercent(usage.getUsePercent() * 100D + "%");
                    disk.setTypeName(fs.getTypeName());
                    disk.setSysTypeName(fs.getSysTypeName());

                    String val = diskWritesAndReadsOnInit.get(fs.getDevName());
                    if (val != null) {
                        long timePeriod = (System.currentTimeMillis() - initTime) / 1000;
                        if(timePeriod == 0){
                            ThreadUtils.sleep(1000L);
                            timePeriod = (System.currentTimeMillis() - initTime) / 1000;
                        }
                        long origRead = Long.parseLong(val.split("\\|")[0]);
                        long origWrite = Long.parseLong(val.split("\\|")[1]);
                        disk.setDiskReadRate((usage.getDiskReadBytes() - origRead) / timePeriod);
                        disk.setDiskWriteRate((usage.getDiskWriteBytes() - origWrite) / timePeriod);
                    }

                    status.getDiskInfos().add(disk);

            }
        }
    }
}
