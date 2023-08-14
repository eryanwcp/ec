/**
 * Copyright (c) 2012-2014 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.modules.sys.mapper.Post;
import com.eryansky.modules.sys.service.PostService;

/**
 * @author Eryan
 * @date 2014-11-25
 */
public class PostUtils {

    private PostUtils(){}

    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    public static final class Static {
        private static final PostService postService = SpringContextHolder.getBean(PostService.class);
        private Static(){}
    }

    /**
     * 根据ID查找
     *
     * @param postId 岗位ID
     * @return
     */
    public static Post get(String postId) {
        if (StringUtils.isBlank(postId)) {
            return null;
        }
        return Static.postService.get(postId);
    }

    /**
     * 根据编码查找
     *
     * @param postCode 岗位编码
     * @return
     */
    public static Post getByCode(String postCode) {
        if (StringUtils.isBlank(postCode)) {
            return null;
        }
        return Static.postService.getByCode(postCode);
    }

    /**
     * 根据ID查找名称
     *
     * @param postId
     * @return
     */
    public static String getPostName(String postId) {
        Post post = get(postId);
        if (post != null) {
            return post.getName();
        }
        return null;
    }

}
