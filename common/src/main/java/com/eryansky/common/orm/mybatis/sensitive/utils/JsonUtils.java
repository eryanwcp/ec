package com.eryansky.common.orm.mybatis.sensitive.utils;


import com.eryansky.common.utils.mapper.JsonMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * json工具类
 *
 * @author Eryan
 * @version 2019-12-13
 */
public class JsonUtils {
    /**
     * 将json字符串转化为StringObject类型的map
     *
     * @param jsonStr json字符串
     * @return map
     */
    public static Map<String, Object> parseToObjectMap(String jsonStr) {
        try {
            return JsonMapper.getInstance().readValue(jsonStr, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 将map转化为json字符串
     *
     * @param params 参数集合
     * @return json
     */
    public static String parseMaptoJSONString(Map<String, Object> params) {
        return JsonMapper.toJsonString(params);
    }

}
