/*******************************************************************************
 * Copyright 2017 Bstek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.definition;

import java.io.Serializable;

/**
 * 报表页面设置
 * 添加水印支持
 * @author Jacky.gao ZhouZhou
 * @since 2014年4月29日
 */
public class Paper implements Serializable{
	private static final long serialVersionUID = -6153150083492704136L;
	/**
	 * 页面左边距
	 */
	private int leftMargin=90;
	/**
	 * 页面右边距
	 */
	private int rightMargin=90;
	/**
	 * 页面上边距
	 */
	private int topMargin=72;
	/**
	 * 页面下边距
	 */
	private int bottomMargin=72;
	/**
	 * 页面类型、 A4 A5 等
	 */
	private PaperType paperType;
	/**
	 * 分页方式 --  自动、固定行 主要针对打印、pdf等
	 */
	private PagingMode pagingMode;
	/**
	 * 分页方式 固定行 固定的行数
	 */
	private int fixRows;
	/**
	 * 页面宽
	 */
	private int width;
	/**
	 * 页面高
	 */
	private int height;
	/**
	 * 页面方向 --  横向、纵向
	 */
	private Orientation orientation;
	/**
	 * html 页面表报对齐方式
	 */
	private HtmlReportAlign htmlReportAlign=HtmlReportAlign.left;
	/**
	 * html 页面表报刷新频率 秒
	 */
	private int htmlIntervalRefreshValue=0;
	/**
	 * 背景图
	 */
	private String bgImage;
	/**
	 * 水印
	 */
	private String watermark;
	/**
	 * 分栏 配置开关
	 */
	private boolean columnEnabled;
	/**
	 * 分栏数量
	 */
	private int columnCount=2;
	/**
	 * 🇫分栏边距
	 */
	private int columnMargin=5;

	public int getLeftMargin() {
		return leftMargin;
	}

	public void setLeftMargin(int leftMargin) {
		this.leftMargin = leftMargin;
	}

	public int getRightMargin() {
		return rightMargin;
	}

	public void setRightMargin(int rightMargin) {
		this.rightMargin = rightMargin;
	}

	public int getTopMargin() {
		return topMargin;
	}

	public void setTopMargin(int topMargin) {
		this.topMargin = topMargin;
	}

	public int getBottomMargin() {
		return bottomMargin;
	}

	public void setBottomMargin(int bottomMargin) {
		this.bottomMargin = bottomMargin;
	}
	
	public PaperType getPaperType() {
		return paperType;
	}
	public void setPaperType(PaperType paperType) {
		this.paperType = paperType;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	
	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	public PagingMode getPagingMode() {
		return pagingMode;
	}
	public void setPagingMode(PagingMode pagingMode) {
		this.pagingMode = pagingMode;
	}
	public int getFixRows() {
		return fixRows;
	}
	public void setFixRows(int fixRows) {
		this.fixRows = fixRows;
	}

	public boolean isColumnEnabled() {
		return columnEnabled;
	}

	public void setColumnEnabled(boolean columnEnabled) {
		this.columnEnabled = columnEnabled;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public int getColumnMargin() {
		return columnMargin;
	}

	public void setColumnMargin(int columnMargin) {
		this.columnMargin = columnMargin;
	}

	public HtmlReportAlign getHtmlReportAlign() {
		return htmlReportAlign;
	}

	public void setHtmlReportAlign(HtmlReportAlign htmlReportAlign) {
		this.htmlReportAlign = htmlReportAlign;
	}

	public String getBgImage() {
		return bgImage;
	}

	public void setBgImage(String bgImage) {
		this.bgImage = bgImage;
	}

	public int getHtmlIntervalRefreshValue() {
		return htmlIntervalRefreshValue;
	}

	public void setHtmlIntervalRefreshValue(int htmlIntervalRefreshValue) {
		this.htmlIntervalRefreshValue = htmlIntervalRefreshValue;
	}

	public String getWatermark() {
		return watermark;
	}

	public void setWatermark(String watermark) {
		this.watermark = watermark;
	}
}
