/**
 * Copyright (c) 2015-2017, Winter Lau (javayou@gmail.com).
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
package com.eryansky.j2cache.cluster;

import com.eryansky.j2cache.CacheException;
import com.eryansky.j2cache.CacheProviderHolder;
import com.eryansky.j2cache.lettuce.LettuceCacheProvider;

import java.util.Properties;

/**
 * 集群策略工厂
 * @author Winter Lau(javayou@gmail.com)
 */
public class ClusterPolicyFactory {

    private ClusterPolicyFactory(){}

    /**
     * 初始化集群消息通知机制
     * @param broadcast  j2cache.broadcast value
     * @param props  broadcast configuations
     * @return ClusterPolicy instance
     */
    public final static ClusterPolicy init(CacheProviderHolder holder, String broadcast, Properties props) {
        ClusterPolicy policy;
        if ("lettuce".equalsIgnoreCase(broadcast))
            policy = ClusterPolicyFactory.lettuce(props, holder);
        else if ("none".equalsIgnoreCase(broadcast))
            policy = new NoneClusterPolicy();
        else
            policy = ClusterPolicyFactory.custom(broadcast, props, holder);
        return policy;
    }

    /**
     * 使用 Redis 订阅和发布机制，该方法只能调用一次
     * @param props 框架配置
     * @return 返回 Redis 集群策略的实例
     */
    private final static ClusterPolicy lettuce(Properties props, CacheProviderHolder holder) {
        LettuceCacheProvider policy = new LettuceCacheProvider();
        policy.connect(props, holder);
        return policy;
    }

    /**
     * 加载自定义的集群通知策略
     * @param classname
     * @param props
     * @return
     */
    private final static ClusterPolicy custom(String classname, Properties props, CacheProviderHolder holder) {
        try {
            ClusterPolicy policy = (ClusterPolicy)Class.forName(classname).getDeclaredConstructor().newInstance();
            policy.connect(props, holder);
            return policy;
        } catch (Exception e) {
            throw new CacheException("Failed in load custom cluster policy. class = " + classname, e);
        }
    }

}
