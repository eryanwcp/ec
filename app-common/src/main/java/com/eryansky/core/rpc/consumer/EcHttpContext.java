package com.eryansky.core.rpc.consumer;

import com.eryansky.core.security.SecurityUtils;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class EcHttpContext extends EcBaseContext implements Serializable {

  private static final ThreadLocal<EcHttpContext> currentEcpHttpContext = new ThreadLocal<EcHttpContext>() {
      protected EcHttpContext initialValue() {
        return new EcHttpContext();
      }
    };
  
  private HashMap<String, Object> parameters = new HashMap<>();
  
  private HashMap<String, Object> attributes = new HashMap<>();
  
  private HttpServletRequest request;
  
  private HttpServletResponse response;
  
  private String target;
  
  private String method;
  
  private boolean disPrintOut = false;
  
  private String printOut;
  
  private boolean success;
  
  public static EcHttpContext getInstance() {
    return currentEcpHttpContext.get();
  }
  
  public static void removeInstance() {
    currentEcpHttpContext.remove();
  }
  
  public EcHttpContext() {
    this.type = "http";
  }
  
  public void setHttp(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
    if (null != this.request) {
      this.rows = request.getParameter("rows");
      this.page = request.getParameter("page");
      this.orderbyfield = request.getParameter("orderbyfield");
      HashMap<Object, Object> param = new HashMap<>();
      Enumeration<String> names = request.getParameterNames();
      while (names.hasMoreElements()) {
        String pname = names.nextElement();
        String pvalue = request.getParameter(pname);
        param.put(pname, pvalue);
      } 
      addParamterMap((HashMap)param);
    } 
  }
  
  public void setService(String target, String method) {
    this.target = target;
    this.method = method;
  }
  
  public void setRequest(HttpServletRequest request) {
    this.request = request;
    if (null != this.request) {
      this.rows = request.getParameter("rows");
      this.page = request.getParameter("page");
      this.orderbyfield = request.getParameter("orderbyfield");
    } 
  }
  
  public boolean isPageQuery() {
    if (null != this.rows && !"".equals(this.rows) && !"-1".equals(this.rows))
      return true; 
    return false;
  }
  
  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }
  
  public HttpServletRequest getRequest() {
    return this.request;
  }
  
  public HttpServletResponse getResponse() {
    return this.response;
  }
  
  public boolean containsKey(String name) {
    return this.parameters.containsKey(name);
  }
  
  public void addParamter(String key, Object value) {
    this.parameters.put(key, value);
  }
  
  public void addParamterMap(HashMap<String, Object> map) {
    if (null == map)
      return; 
    Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      this.parameters.put(entry.getKey(), entry.getValue());
    } 
  }
  
  public String getString(String name) {
    Object param = get(name);
    if (null == param)
      param = ""; 
    return param.toString();
  }
  
  public Object get(String name) {
    return this.parameters.get(name);
  }
  
  public String getParameter(String name) {
    return getRequest().getParameter(name);
  }
  
  public HashMap<String, String> getParameterMap() {
    HashMap<String, String> param = new HashMap<>();
    Enumeration<String> names = getRequest().getParameterNames();
    while (names.hasMoreElements()) {
      String pname = names.nextElement();
      String pvalue = getRequest().getParameter(pname);
      param.put(pname, pvalue);
    } 
    return param;
  }
  
  public HashMap<String, Object> getParameterMapObject() {
    HashMap<String, Object> param = new HashMap<>();
    Enumeration<String> names = getRequest().getParameterNames();
    while (names.hasMoreElements()) {
      String pname = names.nextElement();
      String pvalue = getRequest().getParameter(pname);
      param.put(pname, pvalue);
    } 
    return param;
  }
  
  public EcServiceContext getServiceContext() {
    EcServiceContext serviceContext = new EcServiceContext();
    serviceContext.setRequest(this.request);
    if (null != this.request) {
      String token = this.request.getHeader("token");
      serviceContext.setSessionInfo(SecurityUtils.getCurrentSessionInfo());
    }
    serviceContext.setResponse(this.response);
    return serviceContext;
  }
  
  public void setAttribute(String name, Object object) {
    this.attributes.put(name, object);
  }
  
  public Object getAttribute(String name) {
    return this.attributes.get(name);
  }
  
  public void removeAttribute(String name) {
    this.attributes.remove(name);
  }
  
  public String getTarget() {
    return this.target;
  }
  
  public void setTarget(String target) {
    this.target = target;
  }
  
  public String getMethod() {
    return this.method;
  }
  
  public void setMethod(String method) {
    this.method = method;
  }
  
  public String getPrintOut() {
    return this.printOut;
  }
  
  public void setPrintOut(String printOut) {
    this.printOut = printOut;
  }
  
  public boolean isSuccess() {
    return this.success;
  }
  
  public void setSuccess(boolean success) {
    this.success = success;
  }
  
  public boolean isPrintOut() {
    if (this.disPrintOut)
      return false; 
    return true;
  }
  
  public void disPrintOut() {
    this.disPrintOut = true;
  }
}
