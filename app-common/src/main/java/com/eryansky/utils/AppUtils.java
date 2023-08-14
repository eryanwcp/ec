/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.utils;

import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.utils.UserUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eryan
 * @date 2015-10-19 
 */
public class AppUtils {

    private static final Logger logger = LoggerFactory.getLogger(AppUtils.class);

    private static ServletContext servletContext;

    private AppUtils(){

    }

    public static void init(ServletContext sContext) {
        servletContext = sContext;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * 返回程序的物理安装路径
     *
     * @return String
     */
    public static String getAppAbsolutePath() {
        return servletContext.getRealPath("/");
    }

    public static String toJson(Object object){
        String json = JsonMapper.getInstance().toJson(object);
        return json;
    }

    /**
     * url and para separator *
     */
    public static final String URL_AND_PARA_SEPARATOR = "?";
    /**
     * parameters separator *
     */
    public static final String PARAMETERS_SEPARATOR = "&";
    /**
     * paths separator *
     */
    public static final String PATHS_SEPARATOR = "/";
    /**
     * equal sign *
     */
    public static final String EQUAL_SIGN = "=";

    /**
     * join paras
     *
     * @param parasMap paras map, key is para name, value is para value
     * @return join key and value with {@link #EQUAL_SIGN}, join keys with {@link #PARAMETERS_SEPARATOR}
     */
    public static String joinParas(Map<String, String> parasMap) {
        if (parasMap == null || parasMap.size() == 0) {
            return null;
        }

        StringBuilder paras = new StringBuilder();
        Iterator<Map.Entry<String, String>> ite = parasMap.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) ite.next();
            paras.append(entry.getKey()).append(EQUAL_SIGN).append(entry.getValue());
            if (ite.hasNext()) {
                paras.append(PARAMETERS_SEPARATOR);
            }
        }
        return paras.toString();
    }

    /**
     * join paras with encoded value
     *
     * @param parasMap
     * @return
     * @see #joinParas(Map)
     * @see StringUtils#utf8Encode(String)
     */
    public static String joinParasWithEncodedValue(Map<String, Object> parasMap) {
        StringBuilder paras = new StringBuilder("");
        if (parasMap != null && parasMap.size() > 0) {
            Iterator<Map.Entry<String, Object>> ite = parasMap.entrySet().iterator();
            try {
                while (ite.hasNext()) {
                    Map.Entry<String, Object> entry = (Map.Entry<String, Object>) ite.next();
                    paras.append(entry.getKey()).append(EQUAL_SIGN).append(entry.getValue() instanceof String[] ? StringUtils.utf8Encode(StringUtils.join((String[]) entry.getValue())):StringUtils.utf8Encode((String) entry.getValue()));
                    if (ite.hasNext()) {
                        paras.append(PARAMETERS_SEPARATOR);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
        return paras.toString();
    }

    /**
     * append a key and value pair to url
     *
     * @param url
     * @param paraKey
     * @param paraValue
     * @return
     */
    public static String appendParaToUrl(String url, String paraKey, String paraValue) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        if (!url.contains(URL_AND_PARA_SEPARATOR)) {
            sb.append(URL_AND_PARA_SEPARATOR);
        } else {
            sb.append(PARAMETERS_SEPARATOR);
        }
        return sb.append(paraKey).append(EQUAL_SIGN).append(paraValue).toString();
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String truncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param url url地址
     * @return url请求参数部分
     */
    public static Map<String, String> urlRequest(String url) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = truncateUrlPage(url);
        if (strUrlParam == null) {
            return mapRequest;
        }
        //每个键值为一组
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (StringUtils.isNotBlank(arrSplitEqual[0])) {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }


    //         \b 是单词边界(连着的两个(字母字符 与 非字母字符) 之间的逻辑上的间隔),字符串在编译时会被转码一次,所以是 "\\b"
    // \B 是单词内部逻辑间隔(连着的两个字母字符之间的逻辑上的间隔)
    private static String androidReg = "\\bandroid|Nexus\\b";
    private static String iosReg = "ip(hone|od|ad)";

    private static Pattern androidPat = Pattern.compile(androidReg, Pattern.CASE_INSENSITIVE);
    private static Pattern iosPat = Pattern.compile(iosReg, Pattern.CASE_INSENSITIVE);

    /**
     *
     * @param userAgent
     * @return
     */
    public static boolean likeAndroid(String userAgent){
        if(null == userAgent){
            userAgent = "";
        }
        // 匹配
        Matcher matcherAndroid = androidPat.matcher(userAgent);
        return matcherAndroid.find();
    }

    /**
     *
     * @param userAgent
     * @return
     */
    public static boolean likeIOS(String userAgent){
        if(null == userAgent){
            userAgent = "";
        }
        // 匹配
        Matcher matcherIOS = iosPat.matcher(userAgent);
        return matcherIOS.find();
    }


    /**
     * 查找父级节点
     * @param parentId 父ID
     * @param treeNodes 节点集合
     * @return
     */
    public static TreeNode getParentTreeNode(String parentId, List<TreeNode> treeNodes){
        return getParentTreeNode(parentId,null,treeNodes);
    }
    /**
     * 查找父级节点
     * @param parentId 父ID
     * @param type 节点类型
     * @param treeNodes 节点集合
     * @return
     */
    public static TreeNode getParentTreeNode(String parentId,String type, List<TreeNode> treeNodes){
        TreeNode t = null;
        for(TreeNode treeNode:treeNodes){
            String _type = (String)treeNode.getAttributes().get("nType");
            if(type != null && type.equals(_type) && parentId.equals(treeNode.getId())){
                t = treeNode;
                break;
            }else if(parentId.equals(treeNode.getId())){
                t = treeNode;
                break;
            }

        }
        return t;
    }
    /**
     * 按树形结构排列
     * @param treeNodes
     * @return
     */
    public static List<TreeNode> toTreeTreeNodes(List<TreeNode> treeNodes){
        return toTreeTreeNodes(treeNodes,true);
    }
    /**
     * 按树形结构排列
     * @param treeNodes
     * @param sameNodeType
     * @return
     */
    public static List<TreeNode> toTreeTreeNodes(List<TreeNode> treeNodes,boolean sameNodeType){
        if(Collections3.isEmpty(treeNodes)){
            return Collections.emptyList();
        }
        List<TreeNode> tempTreeNodes = Lists.newArrayList();
        Map<String,TreeNode> tempMap = Maps.newLinkedHashMap();

        for(TreeNode treeNode:treeNodes){
            tempMap.put(treeNode.getId(),treeNode);
            tempTreeNodes.add(treeNode);
        }


        Set<String> keyIds = tempMap.keySet();
        Set<String> removeKeyIds = Sets.newHashSet();
        Iterator<String> iteratorKey = keyIds.iterator();
        while (iteratorKey.hasNext()){
            String key = iteratorKey.next();
            TreeNode treeNode = null;
            for(TreeNode treeNode1:tempTreeNodes){
                if(treeNode1.getId().equals(key)){
                    treeNode = treeNode1;
                    break;
                }
            }

            if(StringUtils.isNotBlank(treeNode.getpId())){
                TreeNode pTreeNode = getParentTreeNode(treeNode.getpId(),sameNodeType ? (String)treeNode.getAttributes().get("nType"):null, tempTreeNodes);
                if(pTreeNode != null){
                    for(TreeNode treeNode2:tempTreeNodes){
                        if(treeNode2.getId().equals(pTreeNode.getId())){
                            pTreeNode.setState(TreeNode.STATE_CLOASED);
                            if(Collections3.isEmpty(treeNode.getChildren())){
                                treeNode.setState(TreeNode.STATE_OPEN);
                            }
                            treeNode2.addChild(treeNode);
                            removeKeyIds.add(treeNode.getId());
                            break;
                        }
                    }

                }
            }

        }

        //remove
        if(Collections3.isNotEmpty(removeKeyIds)){
            keyIds.removeAll(removeKeyIds);
        }

        List<TreeNode> result = Lists.newArrayList();
        keyIds = tempMap.keySet();
        iteratorKey = keyIds.iterator();
        while (iteratorKey.hasNext()){
            String _key = iteratorKey.next();
            TreeNode treeNode = null;
            for(TreeNode treeNode4:tempTreeNodes){
                if(treeNode4.getId().equals(_key)){
                    treeNode = treeNode4;
                    if(Collections3.isEmpty(treeNode.getChildren())){
                        treeNode.setState(TreeNode.STATE_OPEN);
                    }
                    result.add(treeNode);
                    break;
                }
            }

        }
        return result;
    }


    /**
     * 得到分页后的数据
     *
     * @param a
     * @param pageNo 页码
     * @param pageSize 页大小热水
     * @return 分页后结果
     */
    public static <T> List<T> getPagedList(List<T> a,int pageNo,int pageSize) {
        int fromIndex = (pageNo - 1) * pageSize;
        if (fromIndex >= a.size()) {
            return Collections.emptyList();
        }

        int toIndex = pageNo * pageSize;
        if (toIndex >= a.size()) {
            toIndex = a.size();
        }
        return a.subList(fromIndex, toIndex);
    }

    /**
     * emoji表情替换
     *
     * @param source 原字符串
     * @param slipStr emoji表情替换成的字符串
     * @return 过滤后的字符串
     */
    public static String filterEmoji(String source,String slipStr) {
        if(StringUtils.isNotBlank(source)){
            return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", slipStr);
        }else{
            return source;
        }
    }

    /**
     * 基本状态
     * @return
     */
    public static List<StatusState> getStatusStates(){
        List<StatusState> list = new ArrayList<>(3);
        list.add(StatusState.AUDIT);
        list.add(StatusState.NORMAL);
        list.add(StatusState.DELETE);
        return list;
    }

    /**
     * 基本状态
     * @return
     */
    public static List<StatusState> getSimpleStatusStates(){
        List<StatusState> list = new ArrayList<>(2);
        list.add(StatusState.NORMAL);
        list.add(StatusState.LOCK);
        return list;
    }

    /**
     * 是否
     * @return
     */
    public static YesOrNo[] getYesOrNo(){
        return YesOrNo.values();
    }

    public static String getAppURL() {
        String value = AppConstants.getAppURL();
        if(StringUtils.isNotBlank(value)){
            return value;
        }
        try {
            return WebUtils.getAppURL(SpringMVCHolder.getRequest());
        } catch (Exception e) {
        }
        return value;
    }

    /**
     * 
     * @return
     */
    public static String getClientAppURL() {
        try {
            return WebUtils.getAppURL(SpringMVCHolder.getRequest());
        } catch (Exception e) {
        }
        String value = AppConstants.getAppURL();
        if(StringUtils.isNotBlank(value)){
            return value;
        }
        return value;
    }

    /**
     *
     * @return
     */
    public static String getAdaptiveClientAppURL() {
        try {
            return WebUtils.getAppURL(SpringMVCHolder.getRequest(),true);
        } catch (Exception e) {
        }
        String value = AppConstants.getAppURL();
        if(StringUtils.isNotBlank(value)){
            return value;
        }
        return value;
    }

    /**
     * Map转Properties
     * @param map
     * @return
     */
    public static Properties mapToProperties(Map<String, Object> map) {
        if(null == map){
            return null;
        }
        Properties properties = new Properties();
        map.forEach(properties::put);
        return properties;
    }

    /**
     * 获取工程路径
     *
     * @return
     */
    public static String getProjectPath() {
        try {
            File file = new DefaultResourceLoader().getResource("").getFile();
            if (file != null) {
                while (true) {
                    File f = new File(file.getPath() + File.separator + "src" + File.separator + "main");
                    if (f == null || f.exists()) {
                        break;
                    }
                    if (file.getParentFile() != null) {
                        file = file.getParentFile();
                    } else {
                        break;
                    }
                }
                return file.toString();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 创建修改密码URL
     * @param request
     * @return
     */
    public static String createSecurityUpdatePasswordUrl(HttpServletRequest request){
        return createSecurityUpdatePasswordUrl(request,null,false,false,null);
    }


    /**
     * 创建修改密码URL
     * @param request
     * @return
     */
    public static String createLocalSecurityUpdatePasswordUrl(HttpServletRequest request,String token){
        return createSecurityUpdatePasswordUrl(request,token,true,false,null);
    }


    /**
     * 创建修改密码URL
     * @param request
     * @return
     */
    public static String createExtendSecurityUpdatePasswordUrl(HttpServletRequest request,String token){
        return createSecurityUpdatePasswordUrl(request,token,true,true,AppUtils.getClientAppURL());
    }

    /**
     * 创建修改密码URL
     * @param request
     * @param token
     * @param toExtend 是否跳转外部URL
     * @param toUrl 本地URL地址
     * @return
     */
    public static String createSecurityUpdatePasswordUrl(HttpServletRequest request,String token,boolean fromLogin,boolean toExtend,String toUrl){
        StringBuilder url = new StringBuilder();
        boolean isMobile = UserAgentUtils.isMobile(request);
        url.append(isMobile ? AppConstants.getSecurityUpdatePasswordUrlMobile():AppConstants.getSecurityUpdatePasswordUrlPc());
        if (!url.toString().startsWith("http")) {
            url.insert(0,AppUtils.getClientAppURL());
        }
        url.append(StringUtils.contains(url.toString(),"?") ? "&":"?");
        if(StringUtils.isNotBlank(token)){
            url.append("token=").append(token);
        }
        if(fromLogin){
            url.append("&fromLogin=true");
        }
        if(toExtend){
            url.append("&fromExtend=true&extendUrl=").append(null != toUrl ? toUrl : AppUtils.getClientAppURL());
        }
        return url.toString();
    }


}
