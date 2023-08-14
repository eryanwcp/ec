/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.web.springmvc;

import com.eryansky.common.utils.BeanValidators;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;


/**
 * 控制器支持类
 */
public abstract class SimpleController{

    /**
     * 日志对象
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 验证Bean实例对象
     */
    @Autowired(required = false)
    protected Validator validator;


    protected SimpleController() {
    }

    public HttpHeaders getTextPlainHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_PLAIN);
        return responseHeaders;
    }


    /**
     * 用户跳转JSP页面
     * <p/>
     * 此方法不考虑权限控制
     *
     * @param folder 路径
     * @param pageName 页面名称(不加后缀)
     * @return 指定JSP页面
     */
    @GetMapping("/{folder}/{pageName}")
    public String redirectJsp(@PathVariable String folder, @PathVariable String pageName) {
        return "/" + folder + "/" + pageName;
    }

    /**
     * 用户跳转JSP页面
     * <p/>
     * 此方法不考虑权限控制
     *
     * @param prefix   前缀
     * @param toPage   跳转页面名称（包含目录以及后缀）
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping("redirect")
    public void redirectJsp(String prefix, String toPage, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (StringUtils.isEmpty(prefix)) {
            prefix = "/WEB-INF/views/";
        }
        if (StringUtils.isBlank(toPage)) {
            logger.warn("重定向页面为空!");
            response.sendError(404);
        } else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("重定向到页面:{}" , prefix + toPage);
            }
            request.getRequestDispatcher(prefix + toPage).forward(request, response);
        }
    }


    /**
     * 服务端参数有效性验证
     *
     * @param object 验证的实体对象
     * @param groups 验证组
     * @return 验证成功：返回true；严重失败：将错误信息添加到 message 中
     */
    protected boolean beanValidator(Model model, Object object, Class<?>... groups) {
        try {
            BeanValidators.validateWithException(validator, object, groups);
        } catch (ConstraintViolationException ex) {
            List<String> list = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
            list.add(0, "数据验证失败：");
            addMessage(model, list.toArray(new String[]{}));
            return false;
        }
        return true;
    }



    /**
     * 服务端参数有效性验证
     *
     * @param object 验证的实体对象
     * @param groups 验证组
     * @return 验证成功：返回true；严重失败：将错误信息添加到 flash message 中
     */
    protected boolean beanValidator(RedirectAttributes redirectAttributes, Object object, Class<?>... groups) {
        try {
            BeanValidators.validateWithException(validator, object, groups);
        } catch (ConstraintViolationException ex) {
            List<String> list = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
            list.add(0, "数据验证失败：");
            addMessage(redirectAttributes, list.toArray(new String[]{}));
            return false;
        }
        return true;
    }

    /**
     * 服务端参数有效性验证
     * @param object 验证的实体对象
     * @param groups 验证组，不传入此参数时，同@Valid注解验证
     * @return 验证成功：继续执行；验证失败：抛出异常跳转400页面。
     */
    protected void beanValidator(Object object, Class<?>... groups) {
        BeanValidators.validateWithException(validator, object, groups);
    }

    /**
     * 添加Model消息
     *
     * @param messages 消息
     */
    protected void addMessage(Model model, String... messages) {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message).append(messages.length > 1 ? "<br/>" : "");
        }
        model.addAttribute("message", sb.toString());
    }

    /**
     * 添加Flash消息
     *
     * @param messages 消息
     */
    protected void addMessage(RedirectAttributes redirectAttributes, String... messages) {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message).append(messages.length > 1 ? "<br/>" : "");
        }
        redirectAttributes.addFlashAttribute("message", sb.toString());
    }

    /**
     * 初始化数据绑定
     * 1. 设置被排除的属性 不自动绑定
     * 2. 将所有传递进来的String进行HTML编码，防止XSS攻击
     * 3. 将字段中Date类型转换为String类型
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        defaultWebDataBinder(binder);
        setAllowedFields(binder);
        xss(binder);
    }

    /**
     * 默认数据绑定
     * @param binder
     */
    protected void defaultWebDataBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });

    }

    /**
     * String类型转换，将所有传递进来的String进行HTML编码，防止XSS攻击
     *
     * @param binder
     */
    protected void xss(WebDataBinder binder) {
        // String类型转换，将所有传递进来的String进行HTML编码，防止XSS攻击
        binder.registerCustomEditor(String.class, new StringEscapeEditor(true, false));

    }

    /**
     * 漏洞修读 CNVD-2022-23942
     * @param dataBinder
     */
    public void setAllowedFields(WebDataBinder dataBinder) {
        String[] denylist = new String[]{"class.*", "Class.*", "*.class.*", "*.Class.*"};
        dataBinder.setDisallowedFields(denylist);
    }

    /**
     * 客户端返回JSON字符串
     * @param response
     * @param object
     * @return
     */
    protected String renderString(HttpServletResponse response, Object object) {
        return renderString(response, JsonMapper.toJsonString(object), WebUtils.JSON_TYPE);
    }

    /**
     * 客户端返回JSON字符串 只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串，建议在外部接口中使用
     * @param response
     * @param object
     * @return
     */
    protected String renderNonEmptyString(HttpServletResponse response, Object object) {
        return renderString(response, JsonMapper.toNonEmptyJsonString(object), WebUtils.JSON_TYPE);
    }


    /**
     * 客户端返回字符串
     * @param response
     * @param string
     * @return
     */
    protected String renderString(HttpServletResponse response, String string, String type) {
        try (PrintWriter writer = response.getWriter()){
            response.reset();
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setContentType(type);
            response.setCharacterEncoding(WebUtils.DEFAULT_ENCODING);
            writer.print(string);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

}
