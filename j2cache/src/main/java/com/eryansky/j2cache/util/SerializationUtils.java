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
package com.eryansky.j2cache.util;

import com.eryansky.j2cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * 对象序列化工具包
 *
 * @author Winter Lau(javayou@gmail.com)
 */
public class SerializationUtils {

    private final static Logger log = LoggerFactory.getLogger(SerializationUtils.class);
    private static Serializer g_serializer;

    /**
     * 初始化序列化器
     * @param ser  serialization method
     * @param props serializer properties
     */
    public static void init(String ser, Properties props) {
        if (ser == null || "".equals(ser.trim()))
            g_serializer = new JavaSerializer();
        else {
            try {
                if ("java".equals(ser)) {
                    g_serializer = new JavaSerializer();
                } else if ("fst".equals(ser)) {
                    g_serializer = Class.forName("com.eryansky.j2cache.util.FSTSerializer").asSubclass(Serializer.class).getDeclaredConstructor().newInstance();
                } else if("fst-snappy".equals(ser)){
                    g_serializer = Class.forName("com.eryansky.j2cache.util.FstSnappySerializer").asSubclass(Serializer.class).getDeclaredConstructor().newInstance();
                } else if ("fst-json".equals(ser)) {
                    g_serializer = Class.forName("com.eryansky.j2cache.util.FstJSONSerializer").asSubclass(Serializer.class).getConstructor(Properties.class).newInstance(props);
                } else if("fory".equals(ser) || "fury".equals(ser)){
                    g_serializer = Class.forName("com.eryansky.j2cache.util.ForySerializer").asSubclass(Serializer.class).getDeclaredConstructor().newInstance();
                } else if("jackson".equals(ser)){
                    g_serializer = Class.forName("com.eryansky.j2cache.util.JacksonSerializer").asSubclass(Serializer.class).getDeclaredConstructor().newInstance();
                } else {
                    g_serializer = (Serializer) Class.forName(ser).getDeclaredConstructor().newInstance();
                }
            } catch (Exception e) {
                throw new CacheException("Cannot initialize Serializer named [" + ser + ']', e);
            }
        }
        log.info("Using Serializer -> [" + g_serializer.name() + ":" + g_serializer.getClass().getName() + ']');
    }

    /**
     * 针对不同类型做单独处理
     * @param obj 待序列化的对象
     * @return 返回序列化后的字节数组
     * @throws IOException io exception
     */
    public static byte[] serialize(Object obj) throws IOException {
        if (obj == null)
            return null;
        return g_serializer.serialize(obj);
    }

    public static byte[] serializeWithoutException(Object obj) {
        try {
            return serialize(obj);
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

    /**
     * 反序列化
     * @param bytes 待反序列化的字节数组
     * @return 序列化后的对象
     * @throws IOException io exception
     */
    public static Object deserialize(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0)
            return null;
        return g_serializer.deserialize(bytes);
    }
}
