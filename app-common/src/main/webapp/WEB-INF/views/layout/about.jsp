<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<div>
    <ul>
        <li>推荐使用1024*768及以上分辨率<br><br></li>
        <li>支持Chrome、Firefox、IE8.0及以上版本浏览器浏览系统<br><br></li>
        <div>网址：<a target="_blank" href="${fns:getAppProductURL()}">${fns:getAppProductName()}</a></div><br>
        <div>地址：<a target="_blank" href="${fns:getAppProductURL()}">${fns:getAppProductURL()}</a></div><br>
        <div align="center">版权所有 &copy; 2013-${fns:getDate('yyyy')} <a target="_blank" href="${fns:getAppProductURL()}">${fns:getAppProductContact()}</a></div>
    </ul>
</div>