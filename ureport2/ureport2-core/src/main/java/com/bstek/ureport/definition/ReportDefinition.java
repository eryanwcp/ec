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

import com.bstek.ureport.build.Dataset;
import com.bstek.ureport.definition.datasource.DatasourceDefinition;
import com.bstek.ureport.definition.searchform.RenderContext;
import com.bstek.ureport.definition.searchform.SearchForm;
import com.bstek.ureport.export.html.SearchFormData;
import com.bstek.ureport.model.Cell;
import com.bstek.ureport.model.Column;
import com.bstek.ureport.model.Report;
import com.bstek.ureport.model.Row;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定义表报类
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class ReportDefinition implements Serializable{
	private static final long serialVersionUID = 5934291400824773809L;
	/**
	 * 报表配置文件名称
	 */
	private String reportFullName;
	/**
	 * 页面设置信息
	 */
	private Paper paper;
	/**
	 * xml文件相关配置参数
	 */
	private Config config;
	private CellDefinition rootCell;
	/**
	 * 页眉
	 */
	private HeaderFooterDefinition header;
	/**
	 * 页脚
	 */
	private HeaderFooterDefinition footer;
	/**
	 * 表单搜索
	 */
	private SearchForm searchForm;
	// 单元格信息
	private List<CellDefinition> cells;
	// 行信息
	private List<RowDefinition> rows;
	// 列信息
	private List<ColumnDefinition>columns;
	// 图片信息
	private List<ImgDefinition> imgs;
	// 数据源信息
	private List<DatasourceDefinition> datasources;
	private String searchFormXml;
	@JsonIgnore
	private String style;

	public Report newReport() {
		Report report = new Report();
		report.setReportFullName(reportFullName);
		report.setPaper(paper);
		report.setConfig(config);
		report.setHeader(header);
		report.setFooter(footer);
		report.setImgs(imgs);
		List<Row> reportRows = new ArrayList<Row>();
		List<Column> reportColumns = new ArrayList<Column>();
		report.setRows(reportRows);
		report.setColumns(reportColumns);
		//行号对应行信息
		Map<Integer,Row> rowMap=new HashMap<Integer,Row>();

		int headerRowsHeight=0,footerRowsHeight=0,titleRowsHeight=0,summaryRowsHeight=0;
		//转换行信息
		for (RowDefinition rowDef : rows) {

			Row newRow=rowDef.newRow(reportRows);
			//插入行信息
			report.insertRow(newRow, rowDef.getRowNumber());

			rowMap.put(rowDef.getRowNumber(), newRow);

			Band band=rowDef.getBand();
			if(band!=null){
				if(band.equals(Band.headerrepeat)){
					report.getHeaderRepeatRows().add(newRow);
					headerRowsHeight+=newRow.getRealHeight();
				}else if(band.equals(Band.footerrepeat)){
					report.getFooterRepeatRows().add(newRow);
					footerRowsHeight+=newRow.getRealHeight();
				}else if(band.equals(Band.title)){
					report.getTitleRows().add(newRow);
					titleRowsHeight+=newRow.getRealHeight();
				}else if(band.equals(Band.summary)){
					report.getSummaryRows().add(newRow);
					summaryRowsHeight+=newRow.getRealHeight();
				}
			}
		}

		report.setRepeatHeaderRowHeight(headerRowsHeight);
		report.setRepeatFooterRowHeight(footerRowsHeight);
		report.setTitleRowsHeight(titleRowsHeight);
		report.setSummaryRowsHeight(summaryRowsHeight);

		//列号对应列信息
		Map<Integer,Column> columnMap=new HashMap<Integer,Column>();

		for (ColumnDefinition columnDef : columns) {
			Column newColumn=columnDef.newColumn(reportColumns);
			report.insertColumn(newColumn, columnDef.getColumnNumber());
			columnMap.put(columnDef.getColumnNumber(), newColumn);
		}

		//xml单元格对应页面单元格
		Map<CellDefinition,Cell> cellMap=new HashMap<CellDefinition,Cell>();

		for (CellDefinition cellDef : cells) {

			Cell cell = cellDef.newCell();

			cellMap.put(cellDef, cell);
			//设置单元格所在行信息
			Row targetRow=rowMap.get(cellDef.getRowNumber());
			cell.setRow(targetRow);
			//反搞过来将行对象中添加单元格信息
			targetRow.getCells().add(cell);

			//设置单元格列信息
			Column targetColumn=columnMap.get(cellDef.getColumnNumber());
			cell.setColumn(targetColumn);
			//反搞过来将列对象中添加单元格信息
			targetColumn.getCells().add(cell);

			//左上角第一个单元格
			if(cellDef.getLeftParentCell()==null && cellDef.getTopParentCell()==null){
				report.setRootCell(cell);
			}

			//添加信息单元格信息
			report.addCell(cell);
		}

		//设置单元格左 上 单元格
		for (CellDefinition cellDef : cells) {

			Cell targetCell=cellMap.get(cellDef);
			CellDefinition leftParentCellDef=cellDef.getLeftParentCell();

			if(leftParentCellDef!=null){
				targetCell.setLeftParentCell(cellMap.get(leftParentCellDef));
				targetCell.setLeftParentCellName(targetCell.getLeftParentCell().getName());
			}else{
				targetCell.setLeftParentCell(null);
			}
			CellDefinition topParentCellDef=cellDef.getTopParentCell();
			if(topParentCellDef!=null){
				targetCell.setTopParentCell(cellMap.get(topParentCellDef));
				targetCell.setTopParentCellName(targetCell.getTopParentCell().getName());
			}else{
				targetCell.setTopParentCell(null);
			}

			CellDefinition downParentCell=cellDef.getDownParentCell();
			if(downParentCell!=null){
				targetCell.setDownParentCell(cellMap.get(downParentCell));
				targetCell.setDownParentCellName(targetCell.getDownParentCell().getName());
			}else{
				targetCell.setDownParentCell(null);
			}

			CellDefinition rightParentCell= cellDef.getRightParentCell();
			if(rightParentCell!=null){
				targetCell.setRightParentCell(cellMap.get(rightParentCell));
				targetCell.setRightParentCellName(targetCell.getRightParentCell().getName());
			}else{
				targetCell.setRightParentCell(null);
			}
		}

		//设置单元格 行子 、 列子 单元格
		for (CellDefinition cellDef : cells) {

			Cell targetCell = cellMap.get(cellDef);

			List<CellDefinition> rowChildrenCellDefinitions=cellDef.getRowChildrenCells();
			for(CellDefinition childCellDef:rowChildrenCellDefinitions){
				Cell childCell=cellMap.get(childCellDef);
				targetCell.addRowChild(childCell);
			}

			List<CellDefinition> columnChildrenCellDefinitions=cellDef.getColumnChildrenCells();
			for(CellDefinition childCellDef:columnChildrenCellDefinitions){
				Cell childCell=cellMap.get(childCellDef);
				targetCell.addColumnChild(childCell);
			}

			List<CellDefinition> leftChildrenCellDefinitions=cellDef.getLeftParentCells();
			for(CellDefinition childCellDef:leftChildrenCellDefinitions){
				Cell childCell=cellMap.get(childCellDef);
				targetCell.addLeftColumnChild(childCell);
			}

		}

		return report;
	}
	
	public String getStyle() {
		if(style==null){
			style=buildStyle();
		}
		return style;
	}
	
	private String buildStyle(){
		StringBuffer sb=new StringBuffer();
		for(CellDefinition cell:cells){
			CellStyle cellStyle=cell.getCellStyle();
			sb.append("._"+cell.getName()+"{");
			int colWidth=getColumnWidth(cell.getColumnNumber(),cell.getColSpan());
			sb.append("width:"+colWidth+"pt;");
			Alignment align=cellStyle.getAlign();
			if(align!=null){
				sb.append("text-align:"+align.name()+";");				
			}
			Alignment valign=cellStyle.getValign();
			if(valign!=null){
				sb.append("vertical-align:"+valign.name()+";");				
			}
			float lineHeight=cellStyle.getLineHeight();
			if(lineHeight>0){
				sb.append("line-height:"+lineHeight+";");
			}
			String bgcolor=cellStyle.getBgcolor();
			if(StringUtils.isNotBlank(bgcolor)){
				sb.append("background-color:rgb("+bgcolor+");");				
			}
			String fontFamilty=cellStyle.getFontFamily();
			if(StringUtils.isNotBlank(fontFamilty)){
				sb.append("font-family:"+fontFamilty+";");				
			}
			int fontSize=cellStyle.getFontSize();
			sb.append("font-size:"+fontSize+"pt;");
			String foreColor=cellStyle.getForecolor();
			if(StringUtils.isNotBlank(foreColor)){
				sb.append("color:rgb("+foreColor+");");				
			}
			Boolean bold=cellStyle.getBold(),wordWrap = cellStyle.getWordWrap(),
					italic=cellStyle.getItalic(),underline=cellStyle.getUnderline(),
					delline = cellStyle.getDelline();
			if(bold!=null && bold){
				sb.append("font-weight:bold;");								
			}
			if(italic!=null && italic){
				sb.append("font-style:italic;");												
			}
			if(wordWrap !=null && wordWrap){
				sb.append("word-break:break-all;");
			}
			if(underline!=null && underline
					&& (delline == null || !delline)){
				sb.append("text-decoration:underline;");												
			}
			if(delline!=null && delline
					&& (underline == null || !underline)){
				sb.append("text-decoration:line-through;");
			}
			if(delline!=null && delline!=null
					&& delline && underline){
				sb.append("text-decoration:underline line-through;");
			}
			Border border=cellStyle.getLeftBorder();
			if(border!=null){
				sb.append("border-left:"+border.getStyle().name()+" "+border.getWidth()+"px rgb("+border.getColor()+");");				
			}
			border=cellStyle.getRightBorder();
			if(border!=null){
				sb.append("border-right:"+border.getStyle().name()+" "+border.getWidth()+"px rgb("+border.getColor()+");");				
			}
			border=cellStyle.getTopBorder();
			if(border!=null){
				sb.append("border-top:"+border.getStyle().name()+" "+border.getWidth()+"px rgb("+border.getColor()+");");				
			}
			border=cellStyle.getBottomBorder();
			if(border!=null){
				sb.append("border-bottom:"+border.getStyle().name()+" "+border.getWidth()+"px rgb("+border.getColor()+");");				
			}
			sb.append("}");
		}
		return sb.toString();
	}
	
	public SearchFormData buildSearchFormData(Map<String,Dataset> datasetMap,Map<String, Object> parameters){
		if(searchForm==null){
			return null;
		}
		RenderContext context=new RenderContext(datasetMap,parameters);
		SearchFormData data=new SearchFormData();
		data.setFormPosition(searchForm.getFormPosition());
		data.setHtml(searchForm.toHtml(context));
		data.setJs(searchForm.toJs(context));
		data.setSearchFormXml(searchFormXml);
		data.setSearchFormDataStyle(searchForm.toStyle(context));
		return data;
	}
	
	private int getColumnWidth(int columnNumber,int colSpan){
		int width=0;
		if(colSpan>0)colSpan--;
		int start=columnNumber,end=start+colSpan;
		for(int i=start;i<=end;i++){
			for(ColumnDefinition col:columns){
				if(col.getColumnNumber()==i){
					width+=col.getWidth();
				}
			}			
		}
		return width;
	}

	public String getReportFullName() {
		return reportFullName;
	}

	public void setReportFullName(String reportFullName) {
		this.reportFullName = reportFullName;
	}

	public Paper getPaper() {
		return paper;
	}

	public void setPaper(Paper paper) {
		this.paper = paper;
	}

	public CellDefinition getRootCell() {
		return rootCell;
	}

	public void setRootCell(CellDefinition rootCell) {
		this.rootCell = rootCell;
	}

	public HeaderFooterDefinition getHeader() {
		return header;
	}

	public void setHeader(HeaderFooterDefinition header) {
		this.header = header;
	}

	public HeaderFooterDefinition getFooter() {
		return footer;
	}

	public void setFooter(HeaderFooterDefinition footer) {
		this.footer = footer;
	}

	public SearchForm getSearchForm() {
		return searchForm;
	}

	public void setSearchForm(SearchForm searchForm) {
		this.searchForm = searchForm;
	}

	public List<RowDefinition> getRows() {
		return rows;
	}

	public void setRows(List<RowDefinition> rows) {
		this.rows = rows;
	}

	public List<ColumnDefinition> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnDefinition> columns) {
		this.columns = columns;
	}

	public List<CellDefinition> getCells() {
		return cells;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public void setCells(List<CellDefinition> cells) {
		this.cells = cells;
	}

	public List<ImgDefinition> getImgs() {
		return imgs;
	}

	public void setImgs(List<ImgDefinition> imgs) {
		this.imgs = imgs;
	}

	public void setDatasources(List<DatasourceDefinition> datasources) {
		this.datasources = datasources;
	}
	public List<DatasourceDefinition> getDatasources() {
		return datasources;
	}
	public String getSearchFormXml() {
		return searchFormXml;
	}
	public void setSearchFormXml(String searchFormXml) {
		this.searchFormXml = searchFormXml;
	}
}
