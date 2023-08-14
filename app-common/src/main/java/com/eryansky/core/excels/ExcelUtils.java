/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.excels;

import com.eryansky.common.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import delight.rhinosandox.RhinoSandbox;
import delight.rhinosandox.RhinoSandboxes;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

/**
 * @author Eryan
 * @date : 2014-07-31 20:36
 */
public class ExcelUtils {

    private static Logger mLogger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * JavaBean转Map
     * @param obj
     * @return
     */
    public static Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> params = Maps.newHashMap();
        try {
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj);
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (!StringUtils.equals(name, "class")) {
                    params.put(name, propertyUtilsBean.getNestedProperty(obj, name));
                }
            }
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
        }
        return params;
    }

    /**
     * 创建普通表头
     * @param list 表头名称列表
     * @return
     */
    public static TableHeaderMetaData createTableHeader(List<String> list) {
        TableHeaderMetaData headMeta = new TableHeaderMetaData();
        for (String title : list) {
            TableColumn tc = new TableColumn();
            tc.setDisplay(title);
            headMeta.addColumn(tc);
        }
        return headMeta;
    }

    /**
     * 创建普通表头
     * @param titls 表头名称数组
     * @return
     */
    public static TableHeaderMetaData createTableHeader(String[] titls) {
        TableHeaderMetaData headMeta = new TableHeaderMetaData();
        for (String title : titls) {
            TableColumn tc = new TableColumn();
            tc.setDisplay(title);
            tc.setGrouped(true);
            headMeta.addColumn(tc);
        }
        return headMeta;
    }

    /**
     * 创建普通表头
     * @param titls 表头名称数组
     * @param spanCount 需要行合并的列数。
     * 		由第一列数据开始到指定列依次从左到右进行合并操作。
     * 		如该值大于表头名称数组，则为全列合并
     * @return
     */
    public static TableHeaderMetaData createTableHeader(String[] titls, int spanCount) {
        if (spanCount > titls.length)
            spanCount = titls.length;
        TableHeaderMetaData headMeta = new TableHeaderMetaData();
        for (int i = 0; i < titls.length; i++) {
            TableColumn tc = new TableColumn();
            tc.setDisplay(titls[i]);
            if (i < spanCount)
                tc.setGrouped(true);
            headMeta.addColumn(tc);
        }
        return headMeta;
    }

    /**
     * 创建合并表头
     * @param parents 父表头数组
     * @param children 子表头数组
     * @return
     */
    public static TableHeaderMetaData createTableHeader(String[] parents, String[][] children) {
        TableHeaderMetaData headMeta = new TableHeaderMetaData();
        TableColumn parentColumn = null;
        TableColumn sonColumn = null;
        for (int i = 0; i < parents.length; i++) {
            parentColumn = new TableColumn();
            parentColumn.setDisplay(parents[i]);
            if (children != null && children[i] != null) {
                for (int j = 0; j < children[i].length; j++) {
                    sonColumn = new TableColumn();
                    sonColumn.setDisplay(children[i][j]);
                    parentColumn.addChild(sonColumn);
                }
            }
            headMeta.addColumn(parentColumn);
        }
        return headMeta;
    }

    /**
     * 拼装数据
     *
     * @param list 数据集
     * @param headMeta 表头
     * @param fields 对象或Map属性数组（注意：顺序要与表头标题顺序对应，如数据集为List<Object[]>，则该参数可以为null）
     * @return TableData
     */
    @SuppressWarnings("unchecked")
    public static TableData createTableData(List list, TableHeaderMetaData headMeta, String[] fields) {

        TableData td = new TableData(headMeta);
        TableDataRow row = null;
        if (list != null && list.size() > 0) {
            if (list.get(0).getClass().isArray()) {//数组类型
                for (Object obj : list) {
                    row = new TableDataRow(td);
                    for (Object o : (Object[]) obj) {
                        row.addCell(o);
                    }
                    td.addRow(row);
                }
            } else {//JavaBean或Map类型
                for (Object obj : list) {
                    row = new TableDataRow(td);
                    Map<String, Object> map = (obj instanceof Map) ? (Map<String, Object>) obj : beanToMap(obj);
                    for (String key : fields) {
                        row.addCell(map.get(key));
                    }
                    td.addRow(row);
                }
            }
        }
        return td;
    }

    /**
     * 创建压缩输出流
     * @param response
     * @param zipName 压缩包名次
     * @return
     */
    public static ZipOutputStream createZipStream(HttpServletResponse response, String zipName) {
        response.reset();
        response.setContentType("application/vnd.ms-excel"); // 不同类型的文件对应不同的MIME类型
        try {
            response.setHeader("Content-Disposition", "attachment;filename="
                    .concat(String.valueOf(URLEncoder.encode(zipName + ".zip", "UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            mLogger.error(e.getMessage(), e);
        }
        OutputStream os = null;
        try {
            os = response.getOutputStream();
        } catch (IOException e) {
//			e.printStackTrace();
            mLogger.error(e.getMessage(), e);
        }
        return new ZipOutputStream(os);
    }

    public static void copySheetStyle(HSSFWorkbook destwb, HSSFSheet dest,
                                      HSSFWorkbook srcwb, HSSFSheet src) {
        if (src == null || dest == null)
            return;

        dest.setAlternativeExpression(src.getAlternateExpression());
        dest.setAlternativeFormula(src.getAlternateFormula());
        dest.setAutobreaks(src.getAutobreaks());
        dest.setDialog(src.getDialog());
        if (src.getColumnBreaks() != null) {
            for (int col : src.getColumnBreaks()) {
                dest.setColumnBreak((short) col);
            }
        }
        dest.setDefaultColumnWidth(src.getDefaultColumnWidth());
        dest.setDefaultRowHeight(src.getDefaultRowHeight());
        dest.setDefaultRowHeightInPoints(src.getDefaultRowHeightInPoints());
        dest.setDisplayGuts(src.getDisplayGuts());
        dest.setFitToPage(src.getFitToPage());
        dest.setHorizontallyCenter(src.getHorizontallyCenter());
        dest.setDisplayFormulas(src.isDisplayFormulas());
        dest.setDisplayGridlines(src.isDisplayGridlines());
        dest.setDisplayRowColHeadings(src.isDisplayRowColHeadings());
        dest.setGridsPrinted(src.isGridsPrinted());
        dest.setPrintGridlines(src.isPrintGridlines());

        for (int i = 0; i < src.getNumMergedRegions(); i++) {
            dest.addMergedRegion(src.getMergedRegion(i));
        }

        if (src.getRowBreaks() != null) {
            for (int row : src.getRowBreaks()) {
                dest.setRowBreak(row);
            }
        }
        dest.setRowSumsBelow(src.getRowSumsBelow());
        dest.setRowSumsRight(src.getRowSumsRight());

        short maxcol = 0;
        for (int i = 0; i <= src.getLastRowNum(); i++) {
            HSSFRow row = src.getRow(i);
            if (row != null) {
                if (maxcol < row.getLastCellNum())
                    maxcol = row.getLastCellNum();
            }
        }
        for (short col = 0; col <= maxcol; col++) {
            if (src.getColumnWidth(col) != src.getDefaultColumnWidth())
                dest.setColumnWidth(col, src.getColumnWidth(col));
            dest.setColumnHidden(col, src.isColumnHidden(col));
        }
    }

    public static String dumpCellStyle(HSSFCellStyle style) {
        StringBuffer sb = new StringBuffer();
        sb.append(style.getHidden()).append(",");
        sb.append(style.getLocked()).append(",");
        sb.append(style.getWrapText()).append(",");
        sb.append(style.getAlignment()).append(",");
        sb.append(style.getBorderBottom()).append(",");
        sb.append(style.getBorderLeft()).append(",");
        sb.append(style.getBorderRight()).append(",");
        sb.append(style.getBorderTop()).append(",");
        sb.append(style.getBottomBorderColor()).append(",");
        sb.append(style.getDataFormat()).append(",");
        sb.append(style.getFillBackgroundColor()).append(",");
        sb.append(style.getFillForegroundColor()).append(",");
        sb.append(style.getFillPattern()).append(",");
        sb.append(style.getIndention()).append(",");
        sb.append(style.getLeftBorderColor()).append(",");
        sb.append(style.getRightBorderColor()).append(",");
        sb.append(style.getRotation()).append(",");
        sb.append(style.getTopBorderColor()).append(",");
        sb.append(style.getVerticalAlignment());

        return sb.toString();
    }

    public static String dumpFont(HSSFFont font) {
        StringBuffer sb = new StringBuffer();
        sb.append(font.getItalic()).append(",").append(font.getStrikeout())
                .append(",").append(font.getBold()).append(",").append(
                        font.getCharSet()).append(",").append(font.getColor())
                .append(",").append(font.getFontHeight()).append(",").append(
                        font.getFontName()).append(",").append(
                        font.getTypeOffset()).append(",").append(
                        font.getUnderline());
        return sb.toString();
    }

    public static void copyCellStyle(HSSFWorkbook destwb, HSSFCell dest,
                                     HSSFWorkbook srcwb, HSSFCell src) {
        if (src == null || dest == null)
            return;

        HSSFCellStyle nstyle = findStyle(src.getCellStyle(), srcwb, destwb);
        if (nstyle == null) {
            nstyle = destwb.createCellStyle();
            copyCellStyle(destwb, nstyle, srcwb, src.getCellStyle());
        }
        dest.setCellStyle(nstyle);
    }

    private static boolean isSameColor(short a, short b, HSSFPalette apalette,
                                       HSSFPalette bpalette) {
        if (a == b)
            return true;
        HSSFColor acolor = apalette.getColor(a);
        HSSFColor bcolor = bpalette.getColor(b);
        if (acolor == null)
            return true;
        if (bcolor == null)
            return false;
        return acolor.getHexString().equals(bcolor.getHexString());
    }

    private static short findColor(short index, HSSFWorkbook srcwb,
                                   HSSFWorkbook destwb) {
        Integer id = Integer.valueOf(index);
        if (HSSFColor.getIndexHash().containsKey(id))
            return index;
        if (index == HSSFColor.HSSFColorPredefined.AUTOMATIC.getIndex())
            return index;
        HSSFColor color = srcwb.getCustomPalette().getColor(index);
        if (color == null) {
            return index;
        }

        HSSFColor ncolor = destwb.getCustomPalette().findColor(
                (byte) color.getTriplet()[0], (byte) color.getTriplet()[1],
                (byte) color.getTriplet()[2]);
        if (ncolor != null)
            return ncolor.getIndex();
        destwb.getCustomPalette().setColorAtIndex(index,
                (byte) color.getTriplet()[0], (byte) color.getTriplet()[1],
                (byte) color.getTriplet()[2]);
        return index;
    }

    public static HSSFCellStyle findStyle(HSSFCellStyle style,
                                          HSSFWorkbook srcwb, HSSFWorkbook destwb) {
        HSSFPalette srcpalette = srcwb.getCustomPalette();
        HSSFPalette destpalette = destwb.getCustomPalette();

        for (short i = 0; i < destwb.getNumCellStyles(); i++) {
            HSSFCellStyle old = destwb.getCellStyleAt(i);
            if (old == null)
                continue;

            if (style.getAlignment() == old.getAlignment()
                    && style.getBorderBottom() == old.getBorderBottom()
                    && style.getBorderLeft() == old.getBorderLeft()
                    && style.getBorderRight() == old.getBorderRight()
                    && style.getBorderTop() == old.getBorderTop()
                    && isSameColor(style.getBottomBorderColor(), old
                    .getBottomBorderColor(), srcpalette, destpalette)
                    && style.getDataFormat() == old.getDataFormat()
                    && isSameColor(style.getFillBackgroundColor(), old
                    .getFillBackgroundColor(), srcpalette, destpalette)
                    && isSameColor(style.getFillForegroundColor(), old
                    .getFillForegroundColor(), srcpalette, destpalette)
                    && style.getFillPattern() == old.getFillPattern()
                    && style.getHidden() == old.getHidden()
                    && style.getIndention() == old.getIndention()
                    && isSameColor(style.getLeftBorderColor(), old
                    .getLeftBorderColor(), srcpalette, destpalette)
                    && style.getLocked() == old.getLocked()
                    && isSameColor(style.getRightBorderColor(), old
                    .getRightBorderColor(), srcpalette, destpalette)
                    && style.getRotation() == old.getRotation()
                    && isSameColor(style.getTopBorderColor(), old
                    .getTopBorderColor(), srcpalette, destpalette)
                    && style.getVerticalAlignment() == old
                    .getVerticalAlignment()
                    && style.getWrapText() == old.getWrapText()) {

                HSSFFont oldfont = destwb.getFontAt(old.getFontIndex());
                HSSFFont font = srcwb.getFontAt(style.getFontIndex());
                if (oldfont.getBold() == font.getBold()
                        && oldfont.getItalic() == font.getItalic()
                        && oldfont.getStrikeout() == font.getStrikeout()
                        && oldfont.getCharSet() == font.getCharSet()
                        && isSameColor(oldfont.getColor(), font.getColor(),
                        srcpalette, destpalette)
                        && oldfont.getFontHeight() == font.getFontHeight()
                        && oldfont.getFontName().equals(font.getFontName())
                        && oldfont.getTypeOffset() == font.getTypeOffset()
                        && oldfont.getUnderline() == font.getUnderline()) {
                    return old;
                }
            }
        }
        return null;
    }

    public static void copyCellStyle(HSSFWorkbook destwb, HSSFCellStyle dest,
                                     HSSFWorkbook srcwb, HSSFCellStyle src) {
        if (src == null || dest == null)
            return;
        dest.setAlignment(src.getAlignment());
        dest.setBorderBottom(src.getBorderBottom());
        dest.setBorderLeft(src.getBorderLeft());
        dest.setBorderRight(src.getBorderRight());
        dest.setBorderTop(src.getBorderTop());
        dest.setBottomBorderColor(findColor(src.getBottomBorderColor(), srcwb,
                destwb));
        dest.setDataFormat(destwb.createDataFormat().getFormat(
                srcwb.createDataFormat().getFormat(src.getDataFormat())));
        dest.setFillPattern(src.getFillPattern());
        dest.setFillForegroundColor(findColor(src.getFillForegroundColor(),
                srcwb, destwb));
        dest.setFillBackgroundColor(findColor(src.getFillBackgroundColor(),
                srcwb, destwb));
        dest.setHidden(src.getHidden());
        dest.setIndention(src.getIndention());
        dest.setLeftBorderColor(findColor(src.getLeftBorderColor(), srcwb,
                destwb));
        dest.setLocked(src.getLocked());
        dest.setRightBorderColor(findColor(src.getRightBorderColor(), srcwb,
                destwb));
        dest.setRotation(src.getRotation());
        dest
                .setTopBorderColor(findColor(src.getTopBorderColor(), srcwb,
                        destwb));
        dest.setVerticalAlignment(src.getVerticalAlignment());
        dest.setWrapText(src.getWrapText());

        HSSFFont f = srcwb.getFontAt(src.getFontIndex());
        HSSFFont nf = findFont(f, srcwb, destwb);
        if (nf == null) {
            nf = destwb.createFont();
            nf.setBold(f.getBold());//TODO
            nf.setCharSet(f.getCharSet());
            nf.setColor(findColor(f.getColor(), srcwb, destwb));
            nf.setFontHeight(f.getFontHeight());
            nf.setFontHeightInPoints(f.getFontHeightInPoints());
            nf.setFontName(f.getFontName());
            nf.setItalic(f.getItalic());
            nf.setStrikeout(f.getStrikeout());
            nf.setTypeOffset(f.getTypeOffset());
            nf.setUnderline(f.getUnderline());
        }
        dest.setFont(nf);
    }

    private static HSSFFont findFont(HSSFFont font, HSSFWorkbook src,
                                     HSSFWorkbook dest) {
        for (short i = 0; i < dest.getNumberOfFonts(); i++) {
            HSSFFont oldfont = dest.getFontAt(i);
            if (font.getBold() == oldfont.getBold()
                    && font.getItalic() == oldfont.getItalic()
                    && font.getStrikeout() == oldfont.getStrikeout()
                    && font.getCharSet() == oldfont.getCharSet()
                    && font.getColor() == oldfont.getColor()
                    && font.getFontHeight() == oldfont.getFontHeight()
                    && font.getFontName().equals(oldfont.getFontName())
                    && font.getTypeOffset() == oldfont.getTypeOffset()
                    && font.getUnderline() == oldfont.getUnderline()) {
                return oldfont;
            }
        }
        return null;
    }

    public static void copySheet(HSSFWorkbook destwb, HSSFSheet dest,
                                 HSSFWorkbook srcwb, HSSFSheet src) {
        if (src == null || dest == null)
            return;

        copySheetStyle(destwb, dest, srcwb, src);

        for (int i = 0; i <= src.getLastRowNum(); i++) {
            HSSFRow row = src.getRow(i);
            copyRow(destwb, dest.createRow(i), srcwb, row);
        }
    }

    public static void copyRow(HSSFWorkbook destwb, HSSFRow dest,
                               HSSFWorkbook srcwb, HSSFRow src) {
        if (src == null || dest == null)
            return;
        for (short i = 0; i <= src.getLastCellNum(); i++) {
            if (src.getCell(i) != null) {
                HSSFCell cell = dest.createCell(i);
                copyCell(destwb, cell, srcwb, src.getCell(i));
            }
        }

    }

    public static void copyCell(HSSFWorkbook destwb, HSSFCell dest,
                                HSSFWorkbook srcwb, HSSFCell src) {
        if (src == null) {
            dest.setCellType(CellType.BLANK);
            return;
        }

        if (src.getCellComment() != null)
            dest.setCellComment(src.getCellComment());
        if (src.getCellStyle() != null) {
            HSSFCellStyle nstyle = findStyle(src.getCellStyle(), srcwb, destwb);
            if (nstyle == null) {
                nstyle = destwb.createCellStyle();
                copyCellStyle(destwb, nstyle, srcwb, src.getCellStyle());
            }
            dest.setCellStyle(nstyle);
        }
        dest.setCellType(src.getCellType());

        switch (src.getCellType()) {
            case BLANK:

                break;
            case BOOLEAN:
                dest.setCellValue(src.getBooleanCellValue());
                break;
            case FORMULA:
                dest.setCellFormula(src.getCellFormula());
                break;
            case ERROR:
                dest.setCellErrorValue(src.getErrorCellValue());
                break;
            case NUMERIC:
                dest.setCellValue(src.getNumericCellValue());
                break;
            default:
                dest.setCellValue(new HSSFRichTextString(src
                        .getRichStringCellValue().getString()));
                break;
        }
    }


    /**
     * 验证字符串
     * @param regex
     * @param value
     * @return
     */
    public static boolean regex(String regex, String value) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    /**
     * @description 验证日期
     * @param regex
     * @param date
     * @return
     */
    public static boolean regex(String regex, Date date) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher((CharSequence) date);
        return matcher.matches();
    }

    /**
     * @description 根据索引获得excel对应的列
     * @param index
     * @return
     */
    public static String numToAbc(int index) {
        String[] abcString = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
                "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK",
                "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AW",
                "AX", "AY", "AZ"};
        String result = null;
        if (index > 52) {
            result = "error";
        } else {
            result = abcString[index];
        }
        return result;
    }

    /**
     * 将excel里的值转换成字符串
     *
     * @param cell
     * @return
     */
    public static String convertCellToStr(Cell cell) {
        String cellStr = null;
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    cellStr = cell.getStringCellValue().toString();
                    break;
                case BOOLEAN:
                    // 得到Boolean对象的方法
                    cellStr = String.valueOf(cell.getBooleanCellValue());
                    break;
                case NUMERIC:
                    // 先看是否是日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 读取日期格式 2013/2/28
                        cellStr = dateFormate(cell.getDateCellValue());
                    } else {
                        // 读取数字,如果为整数则输出整数，为小数则输出小数。
                        cellStr = getValue(String.valueOf(cell.getNumericCellValue()));
                    }
                    break;
                case FORMULA:
                    // 读取公式
                    cellStr = cell.getCellFormula();
                    break;
            }
        } else {
            return cellStr;
        }
        return cellStr;
    }


    public static Double convertCellToDouble(Cell cell) {
        Double cellDouble = null;
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    cellDouble = Double.valueOf(cell.getStringCellValue());
                    break;
                case NUMERIC:
                    // 先看是否是日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 读取日期格式 2013/2/28
                        cellDouble = Double.valueOf(dateFormate(cell.getDateCellValue()));
                    } else {
                        // 读取数字,如果为整数则输出整数，为小数则输出小数。
                        cellDouble = Double.valueOf(getValue(String.valueOf(cell.getNumericCellValue())));
                    }
                    break;
            }
        }
        return cellDouble;
    }

    /**
     * @description cell转换成date()
     * @param cell
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Date convertCellToDate(Cell cell) {
        Date cellDate = null;
        if (DateUtil.isCellDateFormatted(cell)) {
            // 读取日期格式
            cellDate = cell.getDateCellValue();
        } else {
            cellDate = new Date(String.valueOf(cell.getNumericCellValue()));
        }
        return cellDate;
    }

    /**
     * @description 将科学计数法还原，并去除多余的0,保留两位有效数字。
     * @param number
     * @return
     */
    public static String getValue(String number) {
        return BigDecimal.valueOf(Double.parseDouble(number))
                .setScale(2, BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros()
                .toPlainString();
    }

    /**
     * @description 先根据suffix判断是否为excel文件，再获取文件头，读取前3个字节判断文件类型。
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean isExcel(File file) throws FileNotFoundException {
        Boolean bool = false;
        // 不能通过后缀来判断，因为上传得到的是tmp文件。
        // String suffix =
        // file.getName().substring(file.getName().lastIndexOf(".") + 1);

        byte[] b = new byte[3];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(b, 0, b.length);
            if ("d0cf11".equalsIgnoreCase(bytesToHexString(b))
                    || "504b03".equalsIgnoreCase(bytesToHexString(b))) {
                bool = true;
            }
        } catch (IOException e) {
            mLogger.error(e.getMessage(), e);
        }

        return bool;
    }

    /**
     * @description 获取文件头，读取前几个字节。
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * @description 将date格式化
     * @param date
     * @return String
     */
    public static String dateFormate(Date date) {
        return new SimpleDateFormat("yyyy/MM/dd").format(date);
    }

    /**
     * @description 格式化decimal数据
     * @param num
     * @return
     */
    public static String numberFormate(String num) {
        NumberFormat nf = NumberFormat.getInstance();
        // 小数点最大两位
        nf.setMaximumFractionDigits(2);
        return nf.format(num);
    }

    /**
     * @description 四舍五入，保留两位有效数字。
     * @param num
     * @return
     */
    public static Double round(Double num) {
        int temp = (int) (num * 100 + 0.5);
        num = (double) temp / 100;
        return num;
    }

    /**
     * @description 计算字符串表达式的值 注意 010 会当成八进制的8 0x10会当成十六进制的16 不支持 \ ^ 支持 % ( ) + - * /
     * @param ex
     * @return
     */
    public static Double calculateExpression(String ex) {
        RhinoSandbox sandbox = RhinoSandboxes.create();
        try {
            Double d = (Double) sandbox.eval("source",ex);
            return d;
        } catch (Exception e) {
            mLogger.error(e.getMessage());
            return null;
        }
    }
    /**
     * @description 将科学计数法还原
     * @param account
     * @return
     *
     *         public static String getValue(String account) {
     *
     *         String regex = "^((\\d+.?\\d+)[Ee]{1}(\\d+))$";
     *
     *         Pattern pattern = Pattern.compile(regex);
     *
     *         java.util.regex.Matcher m = pattern.matcher(account); boolean b =
     *         m.matches(); if (b) { DecimalFormat df = new DecimalFormat("#"); account =
     *         df.format(Double.parseDouble(account)); } return account;
     *
     *         }
     */


    /**
     * excel导出
     * @param fileNamePath    导出的文件名称
     * @param sheetName        导出的sheet名称
     * @param list            数据集合
     * @param titles        第一行表头
     * @param fieldNames    字段名称数组
     * @return
     * @throws Exception
     */
    public static <T> File export(String fileNamePath, String sheetName, List<T> list,
                                  String[] titles, String[] fieldNames) throws Exception {
        File file = new File(fileNamePath);
        try (OutputStream os = new FileOutputStream(file);
             HSSFWorkbook wb = new HSSFWorkbook();) {

            HSSFSheet sheet = null;

            // 对每个表生成一个新的sheet,并以表名命名
            if (sheetName == null) {
                sheetName = "sheet1";
            }
            sheet = wb.createSheet(sheetName);

            // 设置表头的说明
            HSSFRow topRow = sheet.createRow(0);
            for (int i = 0; i < titles.length; i++) {
                setCellGBKValue(topRow.createCell(i), titles[i]);
            }

            String methodName = "";
            Method method = null;
            T t = null;
            Object ret = null;
            // 遍历生成数据行，通过反射获取字段的get方法
            for (int i = 0; i < list.size(); i++) {
                t = list.get(i);
                HSSFRow row = sheet.createRow(i + 1);
                Class<? extends Object> clazz = t.getClass();
                for (int j = 0; j < fieldNames.length; j++) {
                    methodName = "get" + capitalize(fieldNames[j]);
                    try {
                        method = clazz.getDeclaredMethod(methodName);
                    } catch (NoSuchMethodException e) {    //	不存在该方法，查看父类是否存在。此处只支持一级父类，若想支持更多，建议使用while循环
                        if (null != clazz.getSuperclass()) {
                            method = clazz.getSuperclass().getDeclaredMethod(methodName);
                        }
                    }
                    if (null == method) {
                        throw new Exception(clazz.getName() + " don't have menthod --> " + methodName);
                    }
                    ret = method.invoke(t);
                    setCellGBKValue(row.createCell(j), ret + "");
                }
            }


            wb.write(os);
        } catch (Exception e) {
            throw new Exception("write excel file error!", e);
        }
        return file;
    }

    private static void setCellGBKValue(HSSFCell cell, String value) {
        cell.setCellType(CellType.STRING);
        //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
        cell.setCellValue(value);
    }

    private static String capitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final char firstChar = str.charAt(0);
        final char newChar = Character.toTitleCase(firstChar);
        if (firstChar == newChar) {
            // already capitalized
            return str;
        }

        char[] newChars = new char[strLen];
        newChars[0] = newChar;
        str.getChars(1, strLen, newChars, 1);
        return String.valueOf(newChars);
    }

    //新增

    /**
     * 对外提供读取excel的方法， 当且仅当只有一个sheet， 默认从第一个sheet读取数据
     * @param file
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(File file) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        if (file.exists() && file.isFile()) {
            Workbook wb = getWorkbook(file);
            if (wb != null) {
                Sheet sheet = getSheet(wb, 0);
                list = getSheetData(wb, sheet);
            }
        }
        return list;
    }

    /**
     * 对外提供读取excel的方法， 根据sheet下标索引读取sheet数据
     * @param file
     * @param sheetIndex
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(File file, int sheetIndex) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        if (file.exists() && file.isFile()) {
            Workbook wb = getWorkbook(file);
            if (wb != null) {
                Sheet sheet = getSheet(wb, sheetIndex);
                list = getSheetData(wb, sheet);
            }
        }
        return list;
    }

    /**
     * 对外提供读取excel的方法， 根据sheet下标索引读取sheet对象， 并指定行列区间获取数据[startRowIndex, endRowIndex), [startColumnIndex, endColumnIndex)
     * @param file
     * @param sheetIndex
     * @param startRowIndex
     * @param endRowIndex
     * @param startColumnIndex
     * @param endColumnIndex
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(File file, int sheetIndex, int startRowIndex, int endRowIndex,
                                               int startColumnIndex, int endColumnIndex) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        Workbook wb = getWorkbook(file);
        if (wb != null) {
            Sheet sheet = getSheet(wb, sheetIndex);
            list = getSheetData(wb, sheet, startRowIndex, endRowIndex, startColumnIndex, endColumnIndex);
        }
        return list;
    }

    /**
     * 对外提供读取excel的方法， 根据sheet名称读取sheet数据
     * @param file
     * @param sheetName
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(File file, String sheetName) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        Workbook wb = getWorkbook(file);
        if (wb != null) {
            Sheet sheet = getSheet(wb, sheetName);
            list = getSheetData(wb, sheet);
        }
        return list;
    }

    /**
     * 对外提供读取excel的方法， 根据sheet名称读取sheet对象， 并指定行列区间获取数据[startRowIndex, endRowIndex), [startColumnIndex, endColumnIndex)
     * @param file
     * @param sheetName
     * @param startRowIndex
     * @param endRowIndex
     * @param startColumnIndex
     * @param endColumnIndex
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(File file, String sheetName, int startRowIndex, int endRowIndex,
                                               int startColumnIndex, int endColumnIndex) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        Workbook wb = getWorkbook(file);
        if (wb != null) {
            Sheet sheet = getSheet(wb, sheetName);
            list = getSheetData(wb, sheet, startRowIndex, endRowIndex, startColumnIndex, endColumnIndex);
        }
        return list;
    }

    /**
     * 读取excel的正文内容
     * @param file
     * @param sheetIndex sheet的下标索引值
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcelBody(File file, int sheetIndex) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        if (file.exists() && file.isFile()) {
            Workbook wb = getWorkbook(file);
            if (wb != null) {
                Sheet sheet = getSheet(wb, sheetIndex);
                list = getSheetBodyData(wb, sheet);
            }
        }
        return list;
    }

    /**
     * 读取excel的正文内容
     * @param file
     * @param sheetName sheet的名称
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcelBody(File file, String sheetName) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        if (file.exists() && file.isFile()) {
            Workbook wb = getWorkbook(file);
            if (wb != null) {
                Sheet sheet = getSheet(wb, sheetName);
                list = getSheetBodyData(wb, sheet);
            }
        }
        return list;
    }

    /**
     * 对外提供读取excel的方法， 当且仅当只有一个sheet， 默认从第一个sheet读取数据
     * @param filePath
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(String filePath) throws IOException {
        File file = new File(filePath);
        return readExcel(file);
    }

    /**
     * 对外提供读取excel的方法， 根据sheet下标索引读取sheet数据
     * @param filePath
     * @param sheetIndex
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(String filePath, int sheetIndex) throws IOException {
        File file = new File(filePath);
        return readExcel(file, sheetIndex);
    }

    /**
     * 对外提供读取excel的方法， 根据sheet下标索引读取sheet对象， 并指定行列区间获取数据[startRowIndex, endRowIndex), [startColumnIndex, endColumnIndex)
     * @param filePath
     * @param sheetIndex
     * @param startRowIndex
     * @param endRowIndex
     * @param startColumnIndex
     * @param endColumnIndex
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(String filePath, int sheetIndex, int startRowIndex, int endRowIndex,
                                               int startColumnIndex, int endColumnIndex) throws IOException {
        File file = new File(filePath);
        return readExcel(file, sheetIndex, startRowIndex, endRowIndex, startColumnIndex, endColumnIndex);
    }

    /**
     * 对外提供读取excel的方法， 根据sheet名称读取sheet数据
     * @param filePath
     * @param sheetName
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(String filePath, String sheetName) throws IOException {
        File file = new File(filePath);
        return readExcel(file, sheetName);
    }

    /**
     * 对外提供读取excel的方法， 根据sheet名称读取sheet对象， 并指定行列区间获取数据[startRowIndex, endRowIndex), [startColumnIndex, endColumnIndex)
     * @param filePath
     * @param sheetName
     * @param startRowIndex
     * @param endRowIndex
     * @param startColumnIndex
     * @param endColumnIndex
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcel(String filePath, String sheetName, int startRowIndex, int endRowIndex,
                                               int startColumnIndex, int endColumnIndex) throws IOException {
        File file = new File(filePath);
        return readExcel(file, sheetName, startRowIndex, endRowIndex, startColumnIndex, endColumnIndex);
    }

    /**
     * 读取excel的正文内容
     * @param filePath
     * @param sheetIndex sheet的下标索引值
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcelBody(String filePath, int sheetIndex) throws IOException {
        File file = new File(filePath);
        return readExcelBody(file, sheetIndex);
    }

    /**
     * 读取excel的正文内容
     * @param filePath
     * @param sheetName sheet的名称
     * @return
     * @throws IOException
     */
    public static List<List<Object>> readExcelBody(String filePath, String sheetName) throws IOException {
        File file = new File(filePath);
        return readExcelBody(file, sheetName);
    }

    /**
     * 根据workbook获取该workbook的所有sheet
     * @param wb
     * @return List<Sheet>
     */
    public static List<Sheet> getAllSheets(Workbook wb) {
        int numOfSheets = wb.getNumberOfSheets();
        List<Sheet> sheets = Lists.newArrayList();
        for (int i = 0; i < numOfSheets; i++) {
            sheets.add(wb.getSheetAt(i));
        }
        return sheets;
    }

    /**
     * 根据excel文件来获取workbook
     * @param file
     * @return workbook
     * @throws IOException
     */
    public static Workbook getWorkbook(File file) throws IOException {
        Workbook wb = null;
        if (file.exists() && file.isFile()) {
            String fileName = file.getName();
            String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(fileName.lastIndexOf(".") + 1); // 获取文件后缀

            if ("xls".equals(extension)) { // for office2003
                wb = new HSSFWorkbook(new FileInputStream(file));
            } else if ("xlsx".equals(extension)) { // for office2007
                wb = new XSSFWorkbook(new FileInputStream(file));
            } else {
                throw new IOException("不支持的文件类型");
            }
        }
        return wb;
    }

    /**
     * 根据excel文件来获取workbook
     * @param filePath
     * @return workbook
     * @throws IOException
     */
    public static Workbook getWorkbook(String filePath) throws IOException {
        File file = new File(filePath);
        return getWorkbook(file);
    }

    /**
     * 根据excel文件输出路径来获取对应的workbook
     * @param filePath
     * @return
     * @throws IOException
     */
    public static Workbook getExportWorkbook(String filePath) throws IOException {
        Workbook wb = null;
        File file = new File(filePath);

        String fileName = file.getName();
        String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(fileName.lastIndexOf(".") + 1); // 获取文件后缀

        if ("xls".equals(extension)) { // for 少量数据
            wb = new HSSFWorkbook();
        } else if ("xlsx".equals(extension)) { // for 大量数据
            wb = new SXSSFWorkbook(5000); // 定义内存里一次只留5000行
        } else {
            throw new IOException("不支持的文件类型");
        }
        return wb;
    }

    /**
     * 根据workbook和sheet的下标索引值来获取sheet
     * @param wb
     * @param sheetIndex
     * @return sheet
     */
    public static Sheet getSheet(Workbook wb, int sheetIndex) {
        return wb.getSheetAt(sheetIndex);
    }

    /**
     * 根据workbook和sheet的名称来获取sheet
     * @param wb
     * @param sheetName
     * @return sheet
     */
    public static Sheet getSheet(Workbook wb, String sheetName) {
        return wb.getSheet(sheetName);
    }

    /**
     * 根据sheet获取该sheet的所有数据
     * @param sheet
     * @return
     */
    public static List<List<Object>> getSheetData(Workbook wb, Sheet sheet) {
        List<List<Object>> list = Lists.newArrayList();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            boolean allRowIsBlank = isBlankRow(wb, row);
            if (allRowIsBlank) { // 整行都空，就跳过
                continue;
            }
            List<Object> rowData = getRowData(wb, row);
            list.add(rowData);
        }
        return list;
    }

    /**
     * 读取正文数据， 从第二行起
     * @param wb
     * @param sheet
     * @return
     * @throws IOException
     */
    public static List<List<Object>> getSheetBodyData(Workbook wb, Sheet sheet) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        // 获取总行数
        int rowNum = sheet.getPhysicalNumberOfRows();
        // 获取标题行
        Row headerRow = sheet.getRow(0);
        // 标题总列数
        int colNum = headerRow.getPhysicalNumberOfCells();
        // 获取正文内容， 正文内容应该从第二行开始,第一行为表头的标题
        list.addAll(getSheetData(wb, sheet, 1, rowNum, 0, colNum));
        return list;
    }

    /**
     * 根据sheet获取该sheet的指定行列的数据[startRowIndex, endRowIndex), [startColumnIndex, endColumnIndex)
     * @param wb
     * @param sheet
     * @param startRowIndex    开始行索引值
     * @param endRowIndex    结束行索引值
     * @param startColumnIndex    开始列索引值
     * @param endColumnIndex    结束列索引值
     * @return
     * @throws IOException
     */
    public static List<List<Object>> getSheetData(Workbook wb, Sheet sheet, int startRowIndex, int endRowIndex,
                                                  int startColumnIndex, int endColumnIndex) throws IOException {
        List<List<Object>> list = Lists.newArrayList();
        if (startRowIndex > endRowIndex || startColumnIndex > endColumnIndex) {
            return list;
        }

        /**
         * getLastRowNum方法能够正确返回最后一行的位置；
         * getPhysicalNumberOfRows方法能够正确返回物理的行数；
         */
        // 获取总行数
        int rowNum = sheet.getPhysicalNumberOfRows();
        // int rowNum = sheet.getLastRowNum();
        // 获取标题行
        Row headerRow = sheet.getRow(0);
        // 标题总列数
        int colNum = headerRow.getPhysicalNumberOfCells();

        if (endRowIndex > rowNum) {
            throw new IOException("行的最大下标索引超过了该sheet实际总行数(包括标题行)" + rowNum);
        }
        if (endColumnIndex > colNum) {
            throw new IOException("列的最大下标索引超过了实际标题总列数" + colNum);
        }
        for (int i = startRowIndex; i < endRowIndex; i++) {
            Row row = sheet.getRow(i);
            boolean allRowIsBlank = isBlankRow(wb, row);
            if (allRowIsBlank) { // 整行都空，就跳过
                continue;
            }
            List<Object> rowData = getRowData(wb, row, startColumnIndex, endColumnIndex);
            list.add(rowData);
        }
        return list;
    }

    /**
     * 根据指定列区间获取行的数据
     * @param wb
     * @param row
     * @param startColumnIndex    开始列索引值
     * @param endColumnIndex    结束列索引值
     * @return
     */
    public static List<Object> getRowData(Workbook wb, Row row, int startColumnIndex, int endColumnIndex) {
        List<Object> rowData = Lists.newArrayList();
        for (int j = startColumnIndex; j < endColumnIndex; j++) {
            Cell cell = row.getCell(j);
            rowData.add(getCellValue(wb, cell));
        }
        return rowData;
    }

    /**
     * 判断整行是不是都为空
     * @param row
     * @return
     */
    public static boolean isBlankRow(Workbook wb, Row row) {
        boolean allRowIsBlank = true;
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Object cellValue = getCellValue(wb, cellIterator.next());
            if (cellValue != null && !"".equals(cellValue)) {
                allRowIsBlank = false;
                break;
            }
        }
        return allRowIsBlank;
    }

    /**
     * 获取行的数据
     * @param wb
     * @param row
     * @return
     */
    public static List<Object> getRowData(Workbook wb, Row row) {
        List<Object> rowData = Lists.newArrayList();
        /**
         * 不建议用row.cellIterator(), 因为空列会被跳过， 后面的列会前移， 建议用for循环， row.getLastCellNum()是获取最后一个不为空的列是第几个
         * 结论：空行可以跳过， 空列最好不要跳过
         */
        /*Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
        	Cell cell = cellIterator.next();
            Object cellValue = getCellValue(wb, cell);
            rowData.add(cellValue);
        }*/
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            Object cellValue = getCellValue(wb, cell);
            rowData.add(cellValue);
        }
        return rowData;
    }

    /**
     * 获取单元格值
     * @param cell
     * @return
     */
    public static Object getCellValue(Workbook wb, Cell cell) {
        if (cell == null
                || (cell.getCellType().equals(CellType.STRING) && StringUtils.isBlank(cell.getStringCellValue()))) {
            return null;
        }
        /*if (cell == null) {
            return "";
        }*/
        // 如果该单元格为数字， 则设置该单元格类型为文本格式
        /*CellStyle cellStyle = wb.createCellStyle();
        DataFormat dataFormat = wb.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat("@"));
        cell.setCellStyle(cellStyle);
        cell.setCellType(Cell.CELL_TYPE_STRING);*/

        DecimalFormat df = new DecimalFormat("0");// 格式化 number String字符
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 格式化日期字符串
        DecimalFormat nf = new DecimalFormat("0");// 格式化数字

        switch (cell.getCellType()) {
            case BLANK:
                // return "";
                return null;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                return cell.getErrorCellValue();
            case FORMULA:
                return cell.getNumericCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else if ("@".equals(cell.getCellStyle().getDataFormatString())) {
                    String value = df.format(cell.getNumericCellValue());
                    if (StringUtils.isBlank(value)) {
                        return null;
                    }
                    return value;
                } else if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                    String value = nf.format(cell.getNumericCellValue());
                    if (StringUtils.isBlank(value)) {
                        return null;
                    }
                    return value;
                } else {
                    return cell.getNumericCellValue();
                /*double doubleValue = cell.getNumericCellValue();
                long longValue = (long) doubleValue;
                if (doubleValue - longValue > 0) {
                	return String.valueOf(doubleValue);
                } else {
                	return longValue;
                }*/
                /*DecimalFormat df = new DecimalFormat("#");
                String value = df.format(cell.getNumericCellValue()).toString();
                return value;*/
                }
            case STRING:
                String value = cell.getStringCellValue();
                if (StringUtils.isBlank(value)) {
                    return null;
                } else {
                    return value;
                }
                // return cell.getRichStringCellValue();
            default:
                // return "";
                return null;
        }
    }

    /**
     * 根据sheet返回该sheet的物理总行数
     * sheet.getPhysicalNumberOfRows方法能够正确返回物理的行数
     * @param sheet
     * @return
     */
    public static int getSheetPhysicalRowNum(Sheet sheet) {
        // 获取总行数
        int rowNum = sheet.getPhysicalNumberOfRows();
        return rowNum;
    }

    /**
     * 获取操作的行数
     * @param startRowIndex sheet的开始行位置索引
     * @param endRowIndex sheet的结束行位置索引
     * @return
     */
    public static int getSheetDataPhysicalRowNum(int startRowIndex, int endRowIndex) {
        int rowNum = -1;
        if (startRowIndex >= 0 && endRowIndex >= 0 && startRowIndex <= endRowIndex) {
            rowNum = endRowIndex - startRowIndex + 1;
        }
        return rowNum;
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于单个sheet
     *
     * @param <T>
     * @param headers   表格属性列名数组
     * @param dataset   需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                  javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param filePath  excel文件输出路径
     */
    public static <T> void exportExcel(String[] headers, Collection<T> dataset, String filePath) {
        exportExcel(headers, dataset, filePath, null);
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于单个sheet
     *
     * @param <T>
     * @param headers 表格属性列名数组
     * @param dataset 需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
     * @param filePath  excel文件输出路径
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
     */
    public static <T> void exportExcel(String[] headers, Collection<T> dataset, String filePath, String pattern) {
        try {
            // 声明一个工作薄
            Workbook workbook = getExportWorkbook(filePath);
            if (workbook != null) {
                // 生成一个表格
                Sheet sheet = workbook.createSheet();
                write2Sheet(sheet, headers, dataset, pattern);
                try (OutputStream out = new FileOutputStream(filePath)) {
                    workbook.write(out);
                }
            }
        } catch (IOException e) {
            mLogger.error(e.toString(), e);
        }
    }

    /**
     * 导出数据到Excel文件
     * @param dataList 要输出到Excel文件的数据集
     * @param filePath  excel文件输出路径
     */
    public static void exportExcel(String[][] dataList, String filePath) {
        try {
            // 声明一个工作薄
            Workbook workbook = getExportWorkbook(filePath);
            if (workbook != null) {
                // 生成一个表格
                Sheet sheet = workbook.createSheet();

                for (int i = 0; i < dataList.length; i++) {
                    String[] r = dataList[i];
                    Row row = sheet.createRow(i);
                    for (int j = 0; j < r.length; j++) {
                        Cell cell = row.createCell(j);
                        // cell max length 32767
                        if (r[j].length() > 32767) {
                            mLogger.warn("异常处理", "--此字段过长(超过32767),已被截断--" + r[j]);
                            r[j] = r[j].substring(0, 32766);
                        }
                        cell.setCellValue(r[j]);
                    }
                }
                // 自动列宽
                if (dataList.length > 0) {
                    int colcount = dataList[0].length;
                    for (int i = 0; i < colcount; i++) {
                        sheet.autoSizeColumn(i);
                    }
                }
                OutputStream out = new FileOutputStream(new File(filePath));
                workbook.write(out);
                out.close();
            }
        } catch (IOException e) {
            mLogger.error(e.toString(), e);
        }
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于多个sheet
     * @param sheets ExcelSheet的集体
     * @param filePath excel文件路径
     */
    public static <T> void exportExcel(List<ExcelSheet<T>> sheets, String filePath) {
        exportExcel(sheets, filePath, null);
    }

    /**
     * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于多个sheet
     *
     * @param sheets    ExcelSheet的集合
     * @param filePath  excel文件输出路径
     * @param pattern   如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
     */
    public static <T> void exportExcel(List<ExcelSheet<T>> sheets, String filePath, String pattern) {
        if (CollectionUtils.isEmpty(sheets)) {
            return;
        }
        try {
            // 声明一个工作薄
            Workbook workbook = getExportWorkbook(filePath);
            if (workbook != null) {
                for (ExcelSheet<T> sheetInfo : sheets) {
                    // 生成一个表格
                    Sheet sheet = workbook.createSheet(sheetInfo.getSheetName());
                    write2Sheet(sheet, sheetInfo.getHeaders(), sheetInfo.getDataset(), pattern);
                }
                try (OutputStream out = new FileOutputStream(filePath)) {
                    workbook.write(out);
                }
            }
        } catch (IOException e) {
            mLogger.error(e.toString(), e);
        }
    }

    /**
     * 每个sheet的写入
     * @param sheet   页签
     * @param headers 表头
     * @param dataset 数据集合
     * @param pattern 日期格式
     */
    public static <T> void write2Sheet(Sheet sheet, String[] headers, Collection<T> dataset, String pattern) {
        // 产生表格标题行
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
        }
        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            T t = (T) it.next();
            if (t instanceof Map) { // row data is map
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) t;
                int cellNum = 0;
                for (String k : headers) {
                    if (!map.containsKey(k)) {
                        mLogger.error("Map 中 不存在 key [" + k + "]");
                        continue;
                    }
                    Cell cell = row.createCell(cellNum);
                    Object value = map.get(k);
                    if (value == null) {
                        cell.setCellValue(StringUtils.EMPTY);
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }
                    cellNum++;
                }
            } else if (t instanceof Object[]) { // row data is Object[]
                Object[] tObjArr = (Object[]) t;
                for (int i = 0; i < tObjArr.length; i++) {
                    Cell cell = row.createCell(i);
                    Object value = tObjArr[i];
                    if (value == null) {
                        cell.setCellValue(StringUtils.EMPTY);
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }
                }
            } else if (t instanceof List<?>) { // row data is List
                List<?> rowData = (List<?>) t;
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = rowData.get(i);
                    if (value == null) {
                        cell.setCellValue(StringUtils.EMPTY);
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }
                }
            } else { // row data is vo
                // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
                Field[] fields = t.getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Cell cell = row.createCell(i);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    try {
                        Class<?> tClazz = t.getClass();
                        Method getMethod = tClazz.getMethod(getMethodName, new Class[]{});
                        Object value = getMethod.invoke(t, new Object[]{});
                        String textValue = null;
                        if (value instanceof Integer) {
                            int intValue = (Integer) value;
                            cell.setCellValue(intValue);
                        } else if (value instanceof Float) {
                            float fValue = (Float) value;
                            cell.setCellValue(fValue);
                        } else if (value instanceof Double) {
                            double dValue = (Double) value;
                            cell.setCellValue(dValue);
                        } else if (value instanceof Long) {
                            long longValue = (Long) value;
                            cell.setCellValue(longValue);
                        } else if (value instanceof Boolean) {
                            boolean bValue = (Boolean) value;
                            cell.setCellValue(bValue);
                        } else if (value instanceof Date) {
                            Date date = (Date) value;
                            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                            textValue = sdf.format(date);
                        } else {
                            // 其它数据类型都当作字符串简单处理
                            textValue = value.toString();
                        }
                        if (textValue != null) {
                            // HSSFRichTextString richString = new
                            // HSSFRichTextString(textValue);
                            cell.setCellValue(textValue);
                        } else {
                            cell.setCellValue(StringUtils.EMPTY);
                        }
                    } catch (Exception e) {
                        mLogger.error(e.getMessage(), e);
                    }

                }
            }
        }
        // 设定自动宽度
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * EXCEL文件下载
     * @param path
     * @param response
     */
    public static void download(String path, HttpServletResponse response) {
        try (InputStream inputStream = new FileInputStream(path);
             InputStream fis = new BufferedInputStream(inputStream)) {
            // path是指欲下载的文件的路径。
            File file = new File(path);
            // 取得文件名。
            String filename = file.getName();
            // 以流的形式下载文件。

            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            try (OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {
                response.setContentType("application/vnd.ms-excel;charset=gb2312");
                toClient.write(buffer);
                toClient.flush();
            }
        } catch (IOException ex) {
            mLogger.error(ex.getMessage(), ex);
        }
    }

    public static List<List<Object>> readExcel(InputStream inputStream, String type) throws Exception {
        List<List<Object>> list = Lists.newArrayList();
        Workbook wookbook = null;

        try {
            if ("xlsx".equalsIgnoreCase(type)) {
                wookbook = new XSSFWorkbook(inputStream);
            } else {
                wookbook = new HSSFWorkbook(inputStream);
            }

            if (wookbook != null) {
                Sheet sheet = wookbook.getSheetAt(0);
                list = getSheetData(wookbook, sheet);
            }
        } catch (Exception var17) {
            throw new Exception("analysis excel exception!", var17);
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }

        }

        return list;
    }

    /**
     * 分别测试以下示例并成功通过：
     * 1. 单个sheet， 数据集类型是List<List<Object>>
     * 2. 单个sheet， 数据集类型是List<Object[]>
     * 3. 多个sheet， 数据集类型是List<ExcelSheet<List<Object>>>
     * 4. 多个sheet， 数据集类型是List<ExcelSheet<List<Object>>>
     * 5. 多个sheet， 数据集类型是List<ExcelSheet<List<Object>>>, 支持大数据量
     * @param args
     */
    public static void main(String[] args) {
        // List<List<Object>> list = new ArrayList<List<Object>>();

        /*try {

            // list = readExcel(new File("D:/test.xlsx"));
            // 导入
            // list = readExcel(new File("D:/test.xlsx"), 1);
            list = readExcelBody("D:/test.xlsx", 1);

            List<Object[]> dataList = new ArrayList<Object[]>();
            for (int i = 0; i < list.size(); i++) {
                Object[] objArr = new Object[list.get(i).size()];
                List<Object> objList = list.get(i);
                for (int j = 0; j < objList.size(); j++) {
                    objArr[j] = objList.get(j);
                }
                dataList.add(objArr);
            }
            for (int i = 0; i < dataList.size(); i++) {
            	System.out.println(Arrays.toString(dataList.get(i)));
            }

            String[] headers = { "代理商ID", "代理商编码", "系统内代理商名称", "贷款代理商名称", "入网时长", "佣金账期", "佣金类型", "金额" };
            String filePath = "d://out_" + System.currentTimeMillis() + ".xlsx";

            ExcelSheet<List<Object>> sheet = new ExcelSheet<List<Object>>();
            sheet.setHeaders(headers);
            sheet.setSheetName("按入网时间提取佣金数");
            sheet.setDataset(list);

            ExcelSheet<Object[]> sheet = new ExcelSheet<Object[]>();
            sheet.setHeaders(headers);
            sheet.setSheetName("按入网时间提取佣金数");
            sheet.setDataset(dataList);

            // List<ExcelSheet<List<Object>>> sheets = new
            // ArrayList<ExcelSheet<List<Object>>>();
            List<ExcelSheet<Object[]>> sheets = new ArrayList<ExcelSheet<Object[]>>();
            sheets.add(sheet);
            // 导出
            // exportExcel(headers, list, os);
            // exportExcel(headers, dataList, os);
            exportExcel(sheets, filePath);
            // list = readExcel(new File("D:/test.xlsx"), "按入网时间提取佣金数");
            // list = readExcel(new File("D:/test.xlsx"), 0, 1, 85, 0, 6);
            // list = readExcel(new File("D:/test.xlsx"), "按入网时间提取佣金数", 1061,
            // 1062, 0, 8);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // 有3个sheet的数据， 每个sheet数据为50万行， 共150万行数据输出到Excel文件, 性能测试。
        List<ExcelSheet<List<Object>>> sheetsData = new ArrayList<>();

        int sheetRowNum = 50000;

        for (int i = 0; i < 3; i++) {
            ExcelSheet<List<Object>> sheetData = new ExcelSheet<>();
            String[] headers = {"姓名", "手机号码", "性别", "身份证号码", "家庭住址"};
            String sheetName = "第" + (i + 1) + "个sheet";

            List<List<Object>> sheetDataList = new ArrayList<>();
            for (int j = 0; j < sheetRowNum; j++) {
                List<Object> rowData = new ArrayList<>();
                rowData.add("小明");
                rowData.add("18888888888");
                rowData.add("男");
                rowData.add("123123123123123123");
                rowData.add("广州市");
                sheetDataList.add(rowData);
            }
            sheetData.setSheetName(sheetName);
            sheetData.setHeaders(headers);
            sheetData.setDataset(sheetDataList);

            sheetsData.add(sheetData);
        }
        String filePath = "d://out_" + System.currentTimeMillis() + ".xlsx";
        exportExcel(sheetsData, filePath);
        System.out.println("-----end-----");
        /*for (int i = 0; i < list.size(); i++) {
        	System.out.println(list.get(i));
        }*/
    }
}