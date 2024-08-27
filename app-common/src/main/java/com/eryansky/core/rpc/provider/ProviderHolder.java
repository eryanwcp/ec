package com.eryansky.core.rpc.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderHolder {

    /**
     * 保存RpcProvider提供者的信息
     */
    public static final Map<String, ProviderInfo> RPC_PROVIDER_MAP = new ConcurrentHashMap<>();

    public static class ProviderInfo implements Serializable {

        /**
         * rpc服务提供者应用名称
         */
        private String name;

        /**
         * rpc发布服务前缀，默认是 /beanName
         */
        private String urlPrefix;

        /**
         * rpc发布服务url核心部分，默认是接口方法名称
         */
        private List<RPCMethod> urlCoreMethod;

        /**
         * rpc服务注册到spring容器中的beanName
         */
        private String rpcBeanName;

        /**
         * rpc服务注册到spring容器中的bean
         */
        private Object rpcBean;

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

        public List<RPCMethod> getUrlCoreMethod() {
            return urlCoreMethod;
        }

        public void setUrlCoreMethod(List<RPCMethod> urlCoreMethod) {
            this.urlCoreMethod = urlCoreMethod;
        }

        public String getRpcBeanName() {
            return rpcBeanName;
        }

        public void setRpcBeanName(String rpcBeanName) {
            this.rpcBeanName = rpcBeanName;
        }

        @JsonIgnore
        public Object getRpcBean() {
            return rpcBean;
        }

        public void setRpcBean(Object rpcBean) {
            this.rpcBean = rpcBean;
        }
    }

    public static class RPCMethod implements Serializable{
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
