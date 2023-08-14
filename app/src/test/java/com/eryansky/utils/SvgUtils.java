/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.utils;


import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.io.FileUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * svg
 * @author Eryan
 * @date 2020-01-03
 */
public class SvgUtils {

    private static final String DIR = "docs/icons/svg";

    public static void main(String[] args) {
        String targetDir = "docs/icons/target";
        FileUtils.deleteDirectory(new File(targetDir));
        FileUtils.createDirectory(targetDir);
        r(new File(DIR));
        File[] fileNames = new File(targetDir).listFiles(new StaticFileFilter());
        List<Map<String,String>> jsonList = Lists.newArrayList();
        for(File f:fileNames){
            Map<String,String> map = Maps.newHashMap();
            map.put("text", StringUtils.substringBefore(f.getName(),".svg"));
            map.put("value", StringUtils.substringBefore(f.getName(),".svg"));
            jsonList.add(map);
        }
        try {
            StreamUtils.copy(JsonMapper.toJsonString(jsonList), StandardCharsets.UTF_8,new FileOutputStream("app/src/main/resources/static/js/json/icon-app.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void r(File fileOrDir){
        File[] fileNames = fileOrDir.listFiles(new StaticFileFilter());
        for(File f:fileNames){
            if(f.isDirectory()){
                r(f);
            }else{
                String fileName = f.getAbsolutePath();
//                f.renameTo(new File(StringUtils.replaceAll(fileName,"mobile_","")));
                System.out.println(fileName);
                String nFileName = StringUtils.replace(StringUtils.substringAfterLast(fileName,"\\icons\\svg\\"),"\\","_");
                String nFileDir = StringUtils.substringBefore(fileName,"\\svg\\");
                String nFile = nFileDir +"\\target\\"+ nFileName;
                FileUtils.copyFile(f,new File(nFile));
                System.out.println(nFile);

            }


        }
    }

    static class StaticFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory())
                return true;
            else {
                String name = pathname.getName();
                return name.endsWith(".svg");
            }
        }
    }


}


