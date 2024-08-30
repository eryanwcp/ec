package com.eryansky.core.rpc.consumer;

public class EcBaseContext {
  public static final String CONTEXTNAME_HTTP = "http";
  
  public static final String CONTEXTNAME_Service = "service";
  
  protected String type;
  
  protected String rows;
  
  protected String page;
  
  protected String orderbyfield;
  
  public String getRows() {
    return this.rows;
  }
  
  public void setRows(String rows) {
    this.rows = rows;
  }
  
  public String getPage() {
    return this.page;
  }
  
  public void setPage(String page) {
    this.page = page;
  }
  
  public String getOrderbyfield() {
    return this.orderbyfield;
  }
  
  public void setOrderbyfield(String orderbyfield) {
    this.orderbyfield = orderbyfield;
  }
  
  public String getType() {
    return this.type;
  }
}