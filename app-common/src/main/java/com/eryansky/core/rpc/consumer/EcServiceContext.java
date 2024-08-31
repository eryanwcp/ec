package com.eryansky.core.rpc.consumer;

import java.io.Serializable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.eryansky.core.security.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcServiceContext implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(EcServiceContext.class);
  
  private HttpServletRequest request;
  
  private HttpServletResponse response;
  
  private SessionInfo sessionInfo;
  
  public EcServiceContext() {
  }
  
  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }
  
  public HttpServletRequest getRequest() {
    return this.request;
  }
  
  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }
  
  public HttpServletResponse getResponse() {
    return this.response;
  }


  public SessionInfo getSessionInfo() {
    return sessionInfo;
  }

  public void setSessionInfo(SessionInfo sessionInfo) {
    this.sessionInfo = sessionInfo;
  }


}