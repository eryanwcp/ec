package com.eryansky.fastweixin.company.api.config;

import com.eryansky.fastweixin.cluster.AccessTokenCache;
import com.eryansky.fastweixin.cluster.IAccessTokenCacheService;
import com.eryansky.fastweixin.api.config.ChangeType;
import com.eryansky.fastweixin.api.config.ConfigChangeNotice;
import com.eryansky.fastweixin.api.response.GetJsApiTicketResponse;
import com.eryansky.fastweixin.api.response.GetTokenResponse;
import com.eryansky.fastweixin.exception.WeixinException;
import com.eryansky.fastweixin.util.JSONUtil;
import com.eryansky.fastweixin.util.NetWorkCenter;
import com.eryansky.fastweixin.util.StrUtil;
import org.apache.http.HttpStatus;

/**
 * 企业微信号配置 支持集群
 * @author Eryan
 * @date 2018-10-31
 */
public final class ClusterQYAPIConfig extends QYAPIConfig {

    private final IAccessTokenCacheService accessTokenCacheService;

    private String url;

    /**
     * 构造方法一，实现同时获取AccessToken。不启用jsApi
     *
     * @param corpid     corpid
     * @param corpSecret corpSecret
     * @param accessTokenCacheService 集群缓存实现接口
     */
    public ClusterQYAPIConfig(String corpid, String corpSecret,IAccessTokenCacheService accessTokenCacheService) {
        this(corpid, corpSecret, false,null,accessTokenCacheService);
    }

    /**
     * 构造方法二，实现同时获取AccessToken，启用jsApi
     *
     * @param corpid      corpid
     * @param corpsecret  corpsecret
     * @param enableJsApi enableJsApi
     * @param accessTokenCacheService 集群缓存实现接口
     */
    public ClusterQYAPIConfig(String corpid, String corpsecret, boolean enableJsApi,String url,IAccessTokenCacheService accessTokenCacheService) {
        super(corpid,corpsecret,enableJsApi,url);
        this.url = url;
        if(accessTokenCacheService == null){
            throw new WeixinException(corpid+",参数[accessTokenCacheService]不能为null");
        }
        this.accessTokenCacheService = accessTokenCacheService;
        //初始化
        AccessTokenCache accessTokenCache = accessTokenCacheService.getAccessTokenCache();
        accessTokenCache = accessTokenCache == null ? new AccessTokenCache():accessTokenCache;
        if(!accessTokenCache.isFromExtend()){
            long now = System.currentTimeMillis();
            initToken(now,accessTokenCache);
            if (enableJsApi) initJSToken(now,accessTokenCache);
        }

    }

    public AccessTokenCache getLocalAccessTokenCache() {
        AccessTokenCache accessTokenCache = accessTokenCacheService.getAccessTokenCache();
        accessTokenCache = accessTokenCache == null ? new AccessTokenCache():accessTokenCache;
        long now = System.currentTimeMillis();
        long time = now - accessTokenCache.getWeixinTokenStartTime();
        try {
            /*
             * 判断优先顺序：
             * 1.官方给出的超时时间是7200秒，这里用7100秒来做，防止出现已经过期的情况
             * 2.刷新标识判断，如果已经在刷新了，则也直接跳过，避免多次重复刷新，如果没有在刷新，则开始刷新
             */
            if (time > CACHE_TIME && this.tokenRefreshing.compareAndSet(false, true)) {
                LOG.debug("准备刷新token.............");
                initToken(now,accessTokenCache);
            }
        } catch (Exception e) {
            LOG.warn("刷新Token出错.", e);
            //刷新工作出现有异常，将标识设置回false
            this.tokenRefreshing.set(false);
        }
        if (enableJsApi) {
            long time2 = now - accessTokenCache.getJsTokenStartTime();
            try {
                //官方给出的超时时间是7200秒，这里用7100秒来做，防止出现已经过期的情况
                if (time2 > CACHE_TIME && this.jsRefreshing.compareAndSet(false, true)) {
                    initJSToken(now,accessTokenCache);
                }
            } catch (Exception e) {
                LOG.warn("刷新jsTicket出错.", e);
                //刷新工作出现有异常，将标识设置回false
                this.jsRefreshing.set(false);
            }
        }
        return accessTokenCache;
    }


    public String getAccessToken() {
        AccessTokenCache accessTokenCache = accessTokenCacheService.getAccessTokenCache();
        accessTokenCache = accessTokenCache == null ? new AccessTokenCache():accessTokenCache;
        if(accessTokenCache.isFromExtend()){
            return accessTokenCache.getAccessToken();
        }
        long now = System.currentTimeMillis();
        long time = now - accessTokenCache.getWeixinTokenStartTime();
        try {
            if (time > CACHE_TIME && tokenRefreshing.compareAndSet(false, true)) {
                LOG.debug("准备刷新AccessToken......... {}",corpid);
                initToken(now,accessTokenCache);
            }
        } catch (Exception e) {
            LOG.error("刷新AccessToken异常:"+corpid, e);
            tokenRefreshing.set(false);
        }
        return accessTokenCache.getAccessToken();
    }

    public String getJsApiTicket() {
        AccessTokenCache accessTokenCache = accessTokenCacheService.getAccessTokenCache();
        accessTokenCache = accessTokenCache == null ? new AccessTokenCache():accessTokenCache;
        if(accessTokenCache.isFromExtend()){
            return enableJsApi ? accessTokenCache.getJsApiTicket():null;
        }
        if (enableJsApi) {
            long now = System.currentTimeMillis();
            long time = now - accessTokenCache.getJsTokenStartTime();
            try {
                if (time > CACHE_TIME && jsRefreshing.compareAndSet(false, true)) {
                    LOG.debug("准备刷新JsApiTicket.......... {}",corpid);
                    initJSToken(now,accessTokenCache);
                }
            } catch (Exception e) {
                LOG.error("刷新JsApiTicket异常:"+corpid, e);
                jsRefreshing.set(false);
            }
        } else {
            return null;
        }
        return accessTokenCache.getJsApiTicket();
    }


    public ClusterQYAPIConfig setEnableJsApi(boolean enableJsApi) {
        this.enableJsApi = enableJsApi;
        if (!enableJsApi){
            AccessTokenCache accessTokenCache = accessTokenCacheService.getAccessTokenCache();
            accessTokenCache = accessTokenCache == null ? new AccessTokenCache():accessTokenCache;
            if(!accessTokenCache.isFromExtend()){
                accessTokenCache.setJsApiTicket(null);
                accessTokenCacheService.putAccessTokenCache(accessTokenCache);
            }else {
                LOG.error("不支持的操作。");
            }

        }
        return this;
    }

    private ClusterQYAPIConfig initToken(final long refreshTime, final AccessTokenCache accessTokenCache) {
        LOG.debug("开始初始化access_token.......... {}",corpid);
        // 记住原本的事件，用于出错回滚
        final long oldTime = accessTokenCache.getWeixinTokenStartTime();
        accessTokenCache.setWeixinTokenStartTime(refreshTime);
        String url = (null != this.url ? this.url:"https://qyapi.weixin.qq.com/") + "cgi-bin/gettoken?corpid=" + corpid + "&corpsecret=" + corpsecret;
        NetWorkCenter.get(url, null, new NetWorkCenter.ResponseCallback() {
            @Override
            public void onResponse(int resultCode, String resultJson) {
                if (HttpStatus.SC_OK == resultCode) {
                    GetTokenResponse response = JSONUtil.toBean(resultJson, GetTokenResponse.class);
                    LOG.debug("获取access_token:{} {}", corpid,response.getAccessToken());
                    if (null == response.getAccessToken()) {
                        // 刷新时间回滚
                        accessTokenCache.setWeixinTokenStartTime(oldTime);
                        accessTokenCacheService.putAccessTokenCache(accessTokenCache);
                        throw new WeixinException("微信企业号token获取出错，错误信息:" + corpid+"," +  response.getErrcode() + "," + response.getErrmsg());
                    }
                    String accessToken = response.getAccessToken();
                    accessTokenCache.setAccessToken(accessToken);
                    accessTokenCacheService.putAccessTokenCache(accessTokenCache);
                    // 设置通知点
                    setChanged();
                    notifyObservers(new ConfigChangeNotice(getCorpid(), ChangeType.ACCESS_TOKEN, accessToken));
                }
            }
        });
        tokenRefreshing.set(false);
        return this;
    }

    private ClusterQYAPIConfig initJSToken(final long refreshTime, final AccessTokenCache accessTokenCache) {
        LOG.debug("初始化 jsapi_ticket......... {}",corpid);
        // 记住原本的事件，用于出错回滚
        final long oldTime = accessTokenCache.getWeixinTokenStartTime();
        accessTokenCache.setJsTokenStartTime(refreshTime);
        String url = (null != this.url ? this.url:"https://qyapi.weixin.qq.com/") + "cgi-bin/get_jsapi_ticket?access_token=" + getAccessToken();
        NetWorkCenter.get(url, null, new NetWorkCenter.ResponseCallback() {

            @Override
            public void onResponse(int resultCode, String resultJson) {
                if (HttpStatus.SC_OK == resultCode) {
                    GetJsApiTicketResponse response = JSONUtil.toBean(resultJson, GetJsApiTicketResponse.class);
                    LOG.debug("获取jsapi_ticket:{} {}", corpid,response.getTicket());
                    if (StrUtil.isBlank(response.getTicket())) {
                        //刷新时间回滚
                        accessTokenCache.setJsTokenStartTime(oldTime);
                        accessTokenCacheService.putAccessTokenCache(accessTokenCache);
                        throw new WeixinException("微信企业号jsToken获取出错，错误信息:" + corpid+","+ response.getErrcode() + "," + response.getErrmsg());
                    }
                    String jsApiTicket = response.getTicket();
                    accessTokenCache.setJsApiTicket(jsApiTicket);
                    accessTokenCacheService.putAccessTokenCache(accessTokenCache);
                    //设置通知点
                    setChanged();
                    notifyObservers(new ConfigChangeNotice(getCorpid(), ChangeType.JS_TOKEN, jsApiTicket));
                }
            }
        });
        jsRefreshing.set(false);
        return this;
    }
}
