/**
 * Copyright (c) 2015-2018, Winter Lau (javayou@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eryansky.j2cache.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.*;

/**
 * 实现对 Session 的自定义管理
 * @author Winter Lau (javayou@gmail.com)
 */
public class J2CacheSession implements HttpSession {

    private final Logger logger = LoggerFactory.getLogger(J2CacheSession.class);

    private SessionObject session;
    private boolean newSession = true;

    private final ServletContext servletContext;
    private final CacheFacade cache;

    private volatile boolean invalid;

    public J2CacheSession(ServletContext servletContext, String id, CacheFacade cache) {
        this.servletContext = servletContext;
        this.cache = cache;
        this.session = new SessionObject();
        this.session.setId(id);
        this.session.setCreated_at(System.currentTimeMillis());
    }

    public SessionObject getSessionObject() {
        return session;
    }

    public void setSessionObject(SessionObject session) {
        this.session = session;
    }

    @Override
    public long getCreationTime() {
        return session.getCreated_at();
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public long getLastAccessedTime() {
        return session.getLastAccess_at();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.session.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.session.getMaxInactiveInterval();
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        this.checkValid();
        return session.getAttributes().keys();
    }

    @Override
    public Object getAttribute(String name) {
        try {
            checkValid();
        } catch (IllegalStateException e) {
            logger.debug(e.getMessage());
            return null;
        }
        return session.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.checkValid();
        this.session.put(name, value);
        cache.setSessionAttribute(session, name);
    }

    @Override
    public void removeAttribute(String name) {
        this.checkValid();
        this.session.remove(name);
        cache.removeSessionAttribute(session, name);
    }

    @Override
    public void invalidate() {
        invalid = true;
        this.session.getAttributes().clear();
        cache.deleteSession(getId());
    }

    public boolean isNew() {
        return newSession;
    }

    public void setNew(boolean isNew) {
        this.newSession = isNew;
    }

    protected void checkValid() throws IllegalStateException {
        if (invalid) {
            throw new IllegalStateException("http session has invalidate");
        }
    }

    public boolean isInvalid() {
        return invalid;
    }

    @Deprecated
    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Deprecated
    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Deprecated
    @Override
    public void putValue(String name, Object value) {
        this.setAttribute(name, value);
    }

    @Deprecated
    @Override
    public String[] getValueNames() {
        this.checkValid();
        return Collections.list(session.getAttributes().keys()).stream().toArray(String[]::new);
    }

    @Deprecated
    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

}
