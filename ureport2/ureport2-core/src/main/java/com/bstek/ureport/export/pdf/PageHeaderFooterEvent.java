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
package com.bstek.ureport.export.pdf;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.bstek.ureport.build.paging.HeaderFooter;
import com.bstek.ureport.build.paging.Page;
import com.bstek.ureport.definition.Orientation;
import com.bstek.ureport.definition.Paper;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.export.pdf.font.FontBuilder;
import com.bstek.ureport.model.Report;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * pdf 页面事件
 * @author Jacky.gao
 * @since 2014年4月22日
 */
public class PageHeaderFooterEvent extends PdfPageEventHelper {
	private Report report;
	public PageHeaderFooterEvent(Report report) {
		this.report=report;
	}

	/**
	 * onOpenDocument()  — 当打开一个文档时触发，可以用于初始化文档的全局变量。
	 *         onStartPage()  — 当一个页面初始化时触发，可用于初始化页面的设置参数，但是注意这个函数触发时，该页面并没有创建好，不用利用这个函数添加内容，最好利用onEndPage()处理页面的初始化。
	 *         onEndPage()  — 在创建一个新页面完成但写入内容之前触发，是添加页眉、页脚、水印等最佳时机。
	 *        onCloseDocument() — 在文档关闭之前触发，可以用于释放一些资源。
	 *
	 *        onCloseDocument() — 在文档关闭之前触发，可以用于释放一些资源。
	 */
	/**
	 * 在创建一个新页面完成但写入内容之前触发，
	 * 是添加页眉、页脚、水印等最佳时机
	 * @param writer
	 * @param document
	 */
	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		List<Page> pages=report.getPages();
		// 获取当前页吗
		int pageNumber=writer.getPageNumber();
		if(pageNumber>pages.size()){
			return;
		}
		// 获取当前页表报的数据
		Page page=pages.get(pageNumber-1);
		// 页头
		HeaderFooter header=page.getHeader();
		// 页尾
		HeaderFooter footer=page.getFooter();
		if(header!=null){
			buildTable(writer,header,true,report);			
		}
		if(footer!=null){
			buildTable(writer,footer,false,report);						
		}
	}

	private void buildTable(PdfWriter writer,HeaderFooter hf,boolean header,Report report) {
		// 获取整个页面信息
		Paper paper=report.getPaper();
		int width=paper.getWidth();
		if(paper.getOrientation().equals(Orientation.landscape)){
			width=paper.getHeight();
		}
		int leftMargin=paper.getLeftMargin();
		int rightMargin=paper.getRightMargin();
		// ----
		//  计算页眉页脚的宽度
		int tableWidth=width-leftMargin-rightMargin;
		int height=paper.getHeight();
		if(paper.getOrientation().equals(Orientation.landscape)){
			height=paper.getWidth();
		}
		// 边距
		int margin=hf.getMargin();
		// 高
		int hfHeight=hf.getHeight();
		String left=hf.getLeft();
		String center=hf.getCenter();
		String right=hf.getRight();
		try {
			// 创建pdf页眉页脚表格
			PdfPTable table=null;
			if(StringUtils.isNotEmpty(left)){
				if(StringUtils.isNotEmpty(center) && StringUtils.isNotEmpty(right)){
					// 页眉页脚、 左 中 右都有 3列表格
					table = new PdfPTable(3);
					// 表格列宽 相对比例
					table.setWidths(new int[]{1, 1, 1});
					// 添加单元格
					table.addCell(buildPdfPCell(hf,left,1));
					table.addCell(buildPdfPCell(hf,center,2));
					table.addCell(buildPdfPCell(hf,right,3));
				}else if(StringUtils.isNotEmpty(center)){
					table = new PdfPTable(3);
					table.setWidths(new int[]{1, 1, 1});
					table.addCell(buildPdfPCell(hf,left,1));
					table.addCell(buildPdfPCell(hf,center,2));
					table.addCell(buildPdfPCell(hf,"",3));
				}else if(StringUtils.isNotEmpty(right)){
					table = new PdfPTable(2);
					table.setWidths(new int[]{1,1});
					table.addCell(buildPdfPCell(hf,left,1));
//					table.addCell(buildPdfPCell(hf,"",2));
					table.addCell(buildPdfPCell(hf,right,3));
				}else{
					table = new PdfPTable(1);
					table.setWidths(new int[]{1});
					table.addCell(buildPdfPCell(hf,left,1));
				}
			}else if(StringUtils.isNotEmpty(center)){
				if(StringUtils.isNotEmpty(right)){
					table = new PdfPTable(3);
					table.setWidths(new int[]{1, 1, 1});
					table.addCell(buildPdfPCell(hf,"",1));
					table.addCell(buildPdfPCell(hf,center,2));
					table.addCell(buildPdfPCell(hf,right,3));
				}else{
					table = new PdfPTable(1);
					table.setWidths(new int[]{1});
					table.addCell(buildPdfPCell(hf,center,2));
				}
			}else if(StringUtils.isNotEmpty(right)){
				table = new PdfPTable(1);
				table.setWidths(new int[]{1});
				table.addCell(buildPdfPCell(hf,right,3));
			}
			if(table==null){
				return;
			}
			// 设置单元格默认高度
			table.getDefaultCell().setFixedHeight(hfHeight);
			// 设备表格宽度
			table.setTotalWidth(tableWidth);

		    if(header){
		    	// 锁定宽度
				table.setLockedWidth(true);
		    	int y=height-margin;
		    	table.writeSelectedRows(0, -1, leftMargin,y, writer.getDirectContent());
		    }else{
				table.setLockedWidth(false);
		    	table.writeSelectedRows(0, -1, leftMargin,margin+hfHeight, writer.getDirectContent());            	 
		    }
		}catch(DocumentException de) {
		   throw new ReportComputeException(de);
		}
	}

	/**
	 * 生成pdf表格单元格
	 * @param phf
	 * @param text
	 * @param type
	 * @return
	 */
	private PdfPCell buildPdfPCell(HeaderFooter phf,String text,int type){
		PdfPCell cell=new PdfPCell();
		// 内边距
		cell.setPadding(0);
		// 设置单元格边框 无边框
		cell.setBorder(Rectangle.NO_BORDER);
		// 设置单元格字体
		Font font=FontBuilder.getFont(phf.getFontFamily(), phf.getFontSize(), phf.isBold(), phf.isItalic(),phf.isUnderline());
		String fontColor=phf.getForecolor();
		// 设置字体颜色
		if(StringUtils.isNotEmpty(fontColor)){
			String[] color=fontColor.split(",");
			font.setColor(Integer.valueOf(color[0]), Integer.valueOf(color[1]), Integer.valueOf(color[2]));			
		}
		Paragraph graph=new Paragraph(text,font);
		cell.setPhrase(graph);
		// 设置单元格水平对齐方式
		switch(type){
		case 1:
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			break;
		case 2:
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			break;
		case 3:
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			break;
		}
		// 设置单元格垂直对齐方式
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		return cell;
	}
}
