package com.eryansky.core.rpc.consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerHolder {

    /**
     * 保存RpcProvider提供者的信息
     */
    public static final Map<String, ConsumerInfo> RPC_CONSUMER_MAP = new ConcurrentHashMap<>();

    public static class ConsumerInfo implements Serializable {
        /**
         * rpc服务提供者应用名称
         */
        private String name;

        /**
         * rpc发布服务前缀，默认是 /beanName
         */
        private String urlPrefix;
        /**
         * rpc服务提供者应用名称
         */
        private String serverUrl;
        private String apiKey;
        private String encrypt;
        /**
         * rpc发布服务url核心部分，默认是接口方法名称
         */
        private List<ConsumerHolder.RPCMethod> urlCoreMethod;
        /**
         * rpc服务注册到spring容器中的bean
         */
        private Object rpcProxy;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrlPrefix() {
            return urlPrefix;
        }

        public void setUrlPrefix(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getEncrypt() {
            return encrypt;
        }

        public void setEncrypt(String encrypt) {
            this.encrypt = encrypt;
        }

        public List<ConsumerHolder.RPCMethod> getUrlCoreMethod() {
            return urlCoreMethod;
        }

        public void setUrlCoreMethod(List<ConsumerHolder.RPCMethod> urlCoreMethod) {
            this.urlCoreMethod = urlCoreMethod;
        }

        @JsonIgnore
        public Object getRpcProxy() {
            return rpcProxy;
        }

        public void setRpcProxy(Object rpcProxy) {
            this.rpcProxy = rpcProxy;
        }


    }

    public static class RPCMethod implements Serializable {
        /**
         * 方法对象
         */
        private Method method;

        /**
         * 方法别名
         */
        private String alias;
        /**
         * 加密方式
         */
        private String encrypt;

        @JsonIgnore
        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getEncrypt() {
            return encrypt;
        }

        public void setEncrypt(String encrypt) {
            this.encrypt = encrypt;
        }

    }


}
