package com.eryansky.fastweixin.handle;

import com.eryansky.fastweixin.api.config.ConfigChangeNotice;
import com.eryansky.fastweixin.util.BeanUtil;

import java.util.Observable;

/**
 * 配置变化监听器抽象类
 *
 * @author Eryan
 * @date 2016-03-15
 */
public abstract class AbstractApiConfigChangeHandle implements ApiConfigChangeHandle {

    @Override
    public void update(Observable o, Object arg) {
        if (BeanUtil.nonNull(arg) && arg instanceof ConfigChangeNotice) {
            configChange((ConfigChangeNotice) arg);
        }
    }

    /**
     * 子类实现，当配置变化时会触发该方法
     *
     * @param notice 通知对象
     */
    public abstract void configChange(ConfigChangeNotice notice);
}
