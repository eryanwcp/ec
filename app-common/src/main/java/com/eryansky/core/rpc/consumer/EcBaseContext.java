package com.eryansky.core.rpc.consumer;

import com.eryansky.common.orm.Page;

public class EcBaseContext {
  public static final String CONTEXTNAME_HTTP = "http";
  
  protected String type;

  protected Page page;

  public EcBaseContext() {
    this.type = CONTEXTNAME_HTTP;
  }

  public Page getPage() {
    return this.page;
  }
  
  public void setPage(Page page) {
    this.page = page;
  }

  public String getType() {
    return this.type;
  }
}