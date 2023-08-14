package com.eryansky.core.security;

import com.alibaba.druid.util.DruidPasswordCallback;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Encryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 解密数据库密码回调
 *
 * @author Eryan
 * @since 2021-11-30
 */
public class CustomDruidPasswordCallback extends DruidPasswordCallback {

    private static final Logger logger = LoggerFactory.getLogger(CustomDruidPasswordCallback.class);

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String cdecrypt = (String) properties.get("config.decrypt");
        boolean decrypt = Boolean.parseBoolean(cdecrypt);
        //获取配置文件中的已经加密的密码
        String ePassword = (String) properties.get("password");
        String cKey = (String) properties.get("key");
        if (!decrypt || !StringUtils.isNotEmpty(ePassword)) {
            return;
        }
        try {
            //这里的代码是将密码进行解密，并设置
            String password = StringUtils.isNotBlank(cKey) ?  Encryption.decrypt(ePassword,cKey ): Encryption.decrypt(ePassword);
            setPassword(password.toCharArray());
            logger.debug("数据库密码加密参数转换成功.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        String password = "password";
        String key = "";
        System.out.println(Encryption.encrypt(password));
    }
}