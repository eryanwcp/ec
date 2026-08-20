package com.eryansky.core.security.xss;

import com.google.common.collect.Lists;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class XssWhiteListMatcher {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // 改用 CopyOnWriteArrayList 确保并发读写的线程安全
    private static final List<String> WHITE_LIST = new CopyOnWriteArrayList<>();

    /**
     * 判断当前请求 URI 是否在白名单中
     */
    public static boolean isWhitelisted() {
        String currentUri = UrlContextHolder.getUri();
        if (!StringUtils.hasText(currentUri)) {
            return false;
        }
        return WHITE_LIST.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, currentUri));
    }

    /**
     * 外部添加单个或多个白名单 URL（支持可变参数）
     * 示例：XssWhiteListMatcher.addWhiteList("/api/test");
     *      XssWhiteListMatcher.addWhiteList("/api/a", "/api/b/**");
     *
     * @param patterns 一个或多个 URL 匹配模式
     */
    public static void addWhiteList(String... patterns) {
        if (patterns != null && patterns.length > 0) {
            addWhiteList(Arrays.asList(patterns));
        }
    }

    /**
     * 外部添加白名单 URL 集合
     * 示例：XssWhiteListMatcher.addWhiteList(List.of("/api/v1/**", "/api/v2/**"));
     *
     * @param patterns URL 匹配模式集合
     */
    public static void addWhiteList(Collection<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return;
        }
        for (String pattern : patterns) {
            // 过滤空字符串且避免重复添加
            if (StringUtils.hasText(pattern) && !WHITE_LIST.contains(pattern)) {
                WHITE_LIST.add(pattern);
            }
        }
    }

    /**
     * 获取当前的白名单列表（只读）
     */
    public static List<String> getWhiteList() {
        return Lists.newArrayList(WHITE_LIST);
    }
}