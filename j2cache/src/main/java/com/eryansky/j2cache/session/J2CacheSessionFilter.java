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

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.j2cache.J2Cache;
import com.eryansky.j2cache.lock.DefaultLockCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * 实现基于 J2Cache 的分布式的 Session 管理
 * @author Winter Lau (javayou@gmail.com)
 */
public class J2CacheSessionFilter implements Filter {

    private CacheFacade g_cache;

    private String cookieName;
    private String cookiePath;
    private String cookieDomain;
    private int cookieMaxAge;
    private boolean cookieSecure;
    private boolean discardNonSerializable;

    private final Logger logger = LoggerFactory.getLogger(J2CacheSessionFilter.class);

    private final String[] NULL_STRING_ARRAY = new String[0];
    private final String URL_SPLIT_PATTERN = "[, ;\r\n]";//逗号  空格 分号  换行

    private final PathMatcher pathMatcher = new AntPathMatcher();
    /**
     * 白名单
     */
    private String[] whiteListURLs = null;

    /**
     * 黑名单
     */
    private String[] blackListURLs = null;

    @Override
    public void init(FilterConfig config) {
        this.cookieName     = config.getInitParameter("cookie.name");
        this.cookieName = this.cookieName != null ? this.cookieName:"J2CACHE_SESSION_ID";
        this.cookieDomain   = config.getInitParameter("cookie.domain");
        this.cookiePath     = config.getInitParameter("cookie.path");
        this.cookieSecure = "true".equalsIgnoreCase(config.getInitParameter("cookie.secure"));

        String ctx = config.getServletContext().getContextPath();
        this.cookiePath = null != this.cookiePath && !"".equals(this.cookiePath) ? this.cookiePath:"".equals(ctx) ? "/":ctx;
        String maxAge     = config.getInitParameter("session.maxAge");
        this.cookieMaxAge   = Integer.parseInt(maxAge != null ? maxAge:"1800");
        this.discardNonSerializable = "true".equalsIgnoreCase(config.getInitParameter("session.discardNonSerializable"));

        Properties redisConf = new Properties();
        for(String name : Collections.list(config.getInitParameterNames())) {
            if(name.startsWith("redis.")) {
                redisConf.setProperty(name.substring(6), config.getInitParameter(name));
            }
        }

        String maxSessionSizeInMemory = config.getInitParameter("session.maxSizeInMemory");
        int maxSizeInMemory = Integer.parseInt(maxSessionSizeInMemory != null ? maxSessionSizeInMemory:"1000");

        this.g_cache = new CacheFacade(maxSizeInMemory, this.cookieMaxAge, redisConf, this.discardNonSerializable);

        String whiteListURLStr = config.getInitParameter("whiteListURL");
        whiteListURLStr = whiteListURLStr != null ? whiteListURLStr : "/**";
        whiteListURLs = strToArray(whiteListURLStr);
        String blackListURLStr = config.getInitParameter("blackListURL");
        blackListURLs = strToArray(blackListURLStr);
    }

    private String[] strToArray(String urlStr) {
        if (urlStr == null) {
            return NULL_STRING_ARRAY;
        }
        String[] urlArray = urlStr.split(URL_SPLIT_PATTERN);

        List<String> urlList = new ArrayList<String>();

        for (String url : urlArray) {
            url = url.trim();
            if (url.length() == 0) {
                continue;
            }

            urlList.add(url);
        }

        return urlList.toArray(NULL_STRING_ARRAY);
    }

    private boolean isWhiteURL(String currentURL) {
        for (String whiteURL : whiteListURLs) {
            if (pathMatcher.match(whiteURL, currentURL)) {
                logger.debug("url filter : white url list matches : [{}] match [{}] continue", currentURL, whiteURL);
                return true;
            }
        }
        logger.debug("url filter : white url list not matches : [{}] not match [{}]",
                currentURL, Arrays.toString(whiteListURLs));
        return false;
    }

    private boolean isBlackURL(String currentURL) {
        for (String blackURL : blackListURLs) {
            if (pathMatcher.match(blackURL, currentURL)) {
                logger.debug("url filter : black url list matches : [{}] match [{}] break", currentURL, blackURL);
                return true;
            }
        }
        logger.debug("url filter : black url list not matches : [{}] not match [{}]",
                currentURL, Arrays.toString(blackListURLs));
        return false;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
//        HttpServletResponse httpResponse = (HttpServletResponse) res;
        String currentURL = httpRequest.getServletPath();
        logger.debug("url filter : current url : [{}]", currentURL);
        if (isBlackURL(currentURL)) {//黑名单
            chain.doFilter(req, res);
            return;
        }
        if (!isWhiteURL(currentURL)) {//白名单
            chain.doFilter(req, res);
            return;
        }

        HttpServletRequest j2cacheRequest = new J2CacheRequestWrapper(req, res);
        try {
            chain.doFilter(j2cacheRequest, res);
        } finally {
            //更新 session 的有效时间
            J2CacheSession session = (J2CacheSession)j2cacheRequest.getSession(false);
            if(session != null && !session.isNew() && !session.isInvalid()){
                try {
                    g_cache.updateSessionAccessTime(session.getSessionObject());
                } catch (Exception e) {
    //                        logger.error(currentURL + ":" + e.getMessage(), e);
                    logger.error(currentURL + ":" + e.getMessage());
                }
            }
        }
    }

    @Override
    public void destroy() {
        g_cache.close();
    }


    /*************************************************
     * request 封装，用于重新处理 session 的实现
     *************************************************/
    public class J2CacheRequestWrapper extends HttpServletRequestWrapper {

        public static final String ATTR_AUTHORIZATION = "Authorization";
        public static final String ATTR_TOKEN = "token";

        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final ServletContext servletContext;
        private J2CacheSession session;

        public J2CacheRequestWrapper(ServletRequest req, ServletResponse res) {
            super((HttpServletRequest)req);
            this.request = (HttpServletRequest)req;
            this.response = (HttpServletResponse)res;
            this.servletContext = req.getServletContext();
        }

        @Override
        public HttpSession getSession(boolean create) {
            if (session != null && !session.isInvalid()) return session;
            Cookie ssnCookie = getCookie(cookieName);
            String session_id = null;
            if (ssnCookie != null) {
                session_id = ssnCookie.getValue();
                SessionObject ssnObject = g_cache.getSession(session_id);
                if(ssnObject != null) {
                    session = new J2CacheSession(servletContext, g_cache,ssnObject);
                    session.setNew(false);
                }
            }
            String clientIp = com.eryansky.common.utils.net.IpUtils.getIpAddr(request);
            if (null == session) {
                String token = extractToken(request);
                if (StringUtils.isNotBlank(token)) {
                    session_id = Encrypt.md5(token);
                    String finalSession_id = session_id;
                    String lockKey = "lock_session_token:"+session_id;
                    J2Cache.getChannel().lock(lockKey, 5, 10, new DefaultLockCallback<Boolean>(false, false) {
                        @Override
                        public Boolean handleObtainLock() {
                            SessionObject ssnObject = g_cache.getSession(finalSession_id);
                            //自定义session关联方案 兼容app与webview
                            boolean updateCookie = false;
                            if (ssnObject == null) {
                                ssnObject = g_cache.getSessionBySessionDataKey(finalSession_id);
                                updateCookie = null != ssnObject;
                            }
                            if (ssnObject != null) {
                                session = new J2CacheSession(servletContext, g_cache, ssnObject);
                                session.setNew(false);
                            } else if (create) {
                                session = new J2CacheSession(servletContext, finalSession_id, g_cache);
                                session.getSessionObject().setClientIP(clientIp);
                                g_cache.saveSession(session.getSessionObject());
                                updateCookie = true;
                            }
                            if(updateCookie){
                                Cookie cookie = WebUtils.getCookie(request,cookieName);
                                if(null == cookie){
//                                    setCookie(response,cookieName, finalSession_id);
                                    setCookie(response,cookieName, session.getId());
                                }
                            }
                            return true;
                        }

                        @Override
                        public Boolean handleNotObtainLock() {
                            logger.warn("handleNotObtainLock -> handleObtainLock {}",lockKey);
                            return this.handleObtainLock();
                        }
                    });

                }
            }

            if(session == null && create) {
                if(null == session_id){
                    session_id = UUID.randomUUID().toString().replaceAll("-", "");
                }
                session = new J2CacheSession(servletContext, session_id, g_cache);
                session.getSessionObject().setClientIP(clientIp);
                g_cache.saveSession(session.getSessionObject());
                setCookie(response,cookieName, session_id);
            }
            return session;
        }

        @Override
        public HttpSession getSession() {
            return this.getSession(true);
        }

        /**
         * Get cookie object by cookie name.
         */
        private Cookie getCookie(String name) {
            Cookie[] cookies = ((HttpServletRequest) getRequest()).getCookies();
            if (cookies != null)
                for (Cookie cookie : cookies)
                    if (cookie.getName().equalsIgnoreCase(name))
                        return cookie;
            return null;
        }

        // 独立Token提取方法
        private String extractToken(HttpServletRequest request) {
            // 优先从Header获取（大小写兼容）
            String authorization = request.getHeader(ATTR_AUTHORIZATION);
            if (StringUtils.isBlank(authorization)) {
                authorization = request.getHeader(ATTR_AUTHORIZATION.toLowerCase());
            }
            // Header无则从参数获取
            if (StringUtils.isBlank(authorization)) {
                authorization = request.getParameter(ATTR_TOKEN);
            }
            if (StringUtils.isBlank(authorization)) {
                authorization = request.getParameter(ATTR_AUTHORIZATION);
            }
            if (StringUtils.isBlank(authorization)) {
                return null;
            }
            // 处理Bearer前缀
            if (authorization.startsWith("Bearer ")) {
                return authorization.substring(7).trim();
            } else if (authorization.startsWith("Bearer")) {
                return authorization.substring(6).trim();
            }
            return authorization.trim();
        }
    }


    /**
     * @param name
     * @param value
     */
    private void setCookie(HttpServletResponse response,String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(-1);
        cookie.setPath(cookiePath);
        if (cookieDomain != null && cookieDomain.trim().length() > 0) {
            cookie.setDomain(cookieDomain);
        }
        try {
            cookie.setHttpOnly(true);
            //仅HTTPS环境设置微true
            cookie.setSecure(cookieSecure);
        } catch (Exception e) {
        }
        response.addCookie(cookie);
    }


    /**
     * 设置会话Cookie
     * @param response
     * @param value
     */
    public void setSessionCookie(HttpServletResponse response,String value) {
        setCookie(response,cookieName,value);
    }

    public CacheFacade getCache() {
        return g_cache;
    }
}