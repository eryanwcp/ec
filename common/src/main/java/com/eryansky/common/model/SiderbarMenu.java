/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.model;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 树形节点Menu模型.
 * 
 * @author Eryan
 * @date : 2019-4-1
 */
@SuppressWarnings("serial")
public class SiderbarMenu implements Serializable {

	/**
	 * 节点id
	 */
	private String id;
	/**
	 * 树节点名称
	 */
	private String text;
	/**
	 * 前面的小图标样式
	 */
	private String icon;
	/**
	 * 父级ID
	 */
	private String pId;
	/**
	 * URL
	 */
	private String url;
	/**
	 * 是否顶部
	 */
	private Boolean isHeader;
	/**
	 * iframe-tab
	 */
	private String targetType;
	/**
	 * abosulte
	 */
	private String urlType;
	/**
	 *
	 */
	private String tip;
	/**
	 *
	 */
	private String tipStyle;
	/**
	 * 自定义属性
	 */
	private Map<String, Object> attributes = Maps.newHashMap();
	/**
	 * 子节点
	 */
	private List<SiderbarMenu> children = new ArrayList<SiderbarMenu>(0);

	public SiderbarMenu() {
	}

	public SiderbarMenu(String id, String text) {
		this.id = id;
		this.text = text;
	}

	public SiderbarMenu(String id, String text, String icon) {
		this.id = id;
		this.text = text;
		this.icon = icon;
	}

	/**
	 * 添加子节点.
	 * 
	 * @param childMenu
	 *            子节点
	 */
	public SiderbarMenu addChild(SiderbarMenu childMenu) {
		this.children.add(childMenu);
        return this;
	}

	/**
	 * 添加自定义属性
	 * @param key key
	 * @param object 值
	 * @return
	 */
	public SiderbarMenu addAttribute(String key, Object object) {
		this.attributes.put(key,object);
		return this;
	}

	/**
	 * 返回自定义属性
	 * @param key
	 * @param <T>
	 * @return
	 */
	public <T> T getAttribute(String key) {
		return (T) attributes.get(key);
	}

	/**
	 * 节点id
	 */
	public String getId() {
		return id;
	}

	public SiderbarMenu setId(String id) {
		this.id = id;
        return this;
	}

	/**
	 * 树节点名称
	 */
	public String getText() {
		return text;
	}

	public SiderbarMenu setText(String text) {
		this.text = text;
        return this;
	}

    public String getUrl() {
        return url;
    }

    public SiderbarMenu setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
	 * 自定义属性
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public SiderbarMenu setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
        return this;
	}

	/**
	 * 子节点
	 */
	public List<SiderbarMenu> getChildren() {
		return children;
	}

	public SiderbarMenu setChildren(List<SiderbarMenu> children) {
		this.children = children;
        return this;
	}


	/**
	 * 图标样式
	 */
	public String getIcon() {
		return icon;
	}

	public SiderbarMenu setIcon(String icon) {
		this.icon = icon;
        return this;
	}

	/**
	 * 父级ID
	 * @return
	 */
	public String getpId() {
		return pId;
	}

	/**
	 * 设置父级ID
	 * @param pId 父级ID
	 * @return
	 */
	public SiderbarMenu setpId(String pId) {
		this.pId = pId;
		return this;
	}

	public Boolean getHeader() {
		return isHeader;
	}

	public SiderbarMenu setHeader(Boolean header) {
		isHeader = header;
		return this;
	}

	public String getTargetType() {
		return targetType;
	}

	public SiderbarMenu setTargetType(String targetType) {
		this.targetType = targetType;
		return this;
	}

	public String getUrlType() {
		return urlType;
	}

	public SiderbarMenu setUrlType(String urlType) {
		this.urlType = urlType;
		return this;
	}

	public String getTip() {
		return tip;
	}

	public SiderbarMenu setTip(String tip) {
		this.tip = tip;
		return this;
	}

	public String getTipStyle() {
		return tipStyle;
	}

	public SiderbarMenu setTipStyle(String tipStyle) {
		this.tipStyle = tipStyle;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
