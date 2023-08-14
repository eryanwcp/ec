/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.utils;

import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;
import com.eryansky.modules.disk._enum.FolderType;
import com.eryansky.modules.notice._enum.IsTop;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * combobox、combotree功能类型 全部、请选择  枚举类型.
 * <br>全部("all") 请选择("select")
 * @author Eryan
 * @date 2013-01-28 上午10:48:23
 * 
 */
public enum SelectType implements IGenericEnum<SelectType> {
	/** 全部("all") */
	all("all", "全部"),
	/** 请选择("select") */
	select("select", "请选择...");

	/**
	 * 值 String型
	 */
	private final String value;
	/**
	 * 描述 String型
	 */
	private final String description;

	SelectType(String value, String description) {
		this.value = value;
		this.description = description;
	}

	/**
	 * 获取值
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
     * 获取描述信息
     * @return description
     */
	public String getDescription() {
		return description;
	}

	public static SelectType getByValue(String value) {
		return GenericEnumUtils.getByValue(SelectType.class,value);
	}
	
	public static SelectType getDescription(String description) {
		return GenericEnumUtils.getByDescription(SelectType.class,description);
	}

    /**
     * 快速构造一个树形节点
     * @param selectType
     * @return
     */
    public static TreeNode treeNode(String selectType){
        TreeNode selectTreeNode = null;
        //为combobox添加  "---全部---"、"---请选择---"
        if(StringUtils.isNotBlank(selectType)){
            SelectType s = SelectType.getByValue(selectType);
            String title = selectType;
            if(s != null){
                title = s.getDescription();
            }
            selectTreeNode = new TreeNode("", title);
        }
        return selectTreeNode;
    }

    /**
     * 快速构造一个选项节点
     * @param selectType
     * @return
     */
    public static Combobox combobox(String selectType){
        Combobox selectCombobox = null;
        //为combobox添加  "---全部---"、"---请选择---"
        if(StringUtils.isNotBlank(selectType)){
            SelectType s = SelectType.getByValue(selectType);
            String title = selectType;
            if(s != null){
                title = s.getDescription();
            }
            selectCombobox = new Combobox("", title);
        }
        return selectCombobox;
    }

}