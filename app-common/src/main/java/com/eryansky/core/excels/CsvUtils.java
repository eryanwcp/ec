package com.eryansky.core.excels;

import com.eryansky.common.web.utils.WebUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

/**
 * CSV 工具类
 */
public class CsvUtils {
    /**
     * 写入csv文件
     * @param headers  列头
     * @param data     数据内容
     * @param filePath 创建的csv文件路径
     **/
    public static void writeCsv(String[] headers, List<Object[]> data, String filePath) throws IOException {
        //初始化csvformat
        CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        //创建FileWriter对象
        try(FileWriter fileWriter = new FileWriter(filePath, true);
            //创建CSVPrinter对象
            CSVPrinter printer = new CSVPrinter(fileWriter, formator)){
            //写入列头数据
            printer.printRecord(headers);
            if (null != data) {
                //循环写入数据
                for (Object[] lineData : data) {
                    printer.printRecord(lineData);
                }
            }
            fileWriter.flush();
        }
    }


    /**
     * 写入csv文件
     * @param headers  列头
     * @param data     数据内容
     * @param outputStream 创建的csv文件路径
     **/
    public static void writeCsv(String[] headers, List<Object[]> data, OutputStream outputStream) throws IOException {
        // 写入bom, 防止中文乱码
        byte[] bytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        outputStream.write(bytes);
        //初始化csvformat
        CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        try(//创建FileWriter对象
            Writer outputStreamWriter = new OutputStreamWriter(outputStream);
            //创建CSVPrinter对象
            CSVPrinter printer = new CSVPrinter(outputStreamWriter, formator)){
            //写入列头数据
            printer.printRecord(headers);
            if (null != data) {
                //循环写入数据
                for (Object[] lineData : data) {
                    printer.printRecord(lineData);
                }
            }
            printer.flush();
            outputStreamWriter.flush();
        }

    }


    /**
     * 写入csv文件
     * @param title  文件名
     * @param headers  列头
     * @param data     数据内容
     * @param request
     * @param response
     **/
    public static void exportToCsv(String title, String[] headers, List<Object[]> data, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sFileName = title + ".csv";
        WebUtils.setDownloadableHeader(request, response, sFileName);
        response.setHeader("Connection", "close");
        response.setHeader("Content-Type", "text/csv");
        OutputStream outputStream = response.getOutputStream();
        // 写入bom, 防止中文乱码
        byte[] bytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        outputStream.write(bytes);
        //初始化csvformat
        CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        try(//创建FileWriter对象
            Writer outputStreamWriter = new OutputStreamWriter(outputStream);
            //创建CSVPrinter对象
            CSVPrinter printer = new CSVPrinter(outputStreamWriter, formator)){
            //写入列头数据
            printer.printRecord(headers);
            if (null != data) {
                //循环写入数据
                for (Object[] lineData : data) {
                    printer.printRecord(lineData);
                }
            }
            printer.flush();
            outputStreamWriter.flush();
        }
    }

    /**
     * 写入csv文件
     * @param title  文件名
     * @param headers  列头
     * @param data     数据内容
     * @param request
     * @param response
     **/
    public static void exportToCsv(String title, String[] headers, List<Object[]> data, HttpServletRequest request, HttpServletResponse response, String charsetName) throws IOException {
        String sFileName = title + ".csv";
        WebUtils.setDownloadableHeader(request, response, sFileName);
        response.setHeader("Connection", "close");
        response.setHeader("Content-Type", "text/csv");
        OutputStream outputStream = response.getOutputStream();
        // 写入bom, 防止中文乱码
        byte[] bytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        outputStream.write(bytes);
        //初始化csvformat
        CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        try(
            //创建FileWriter对象
            Writer outputStreamWriter = new OutputStreamWriter(outputStream,charsetName);
            //创建CSVPrinter对象
            CSVPrinter printer = new CSVPrinter(outputStreamWriter, formator)){
            //写入列头数据
            printer.printRecord(headers);
            if (null != data) {
                //循环写入数据
                for (Object[] lineData : data) {
                    printer.printRecord(lineData);
                }
            }
            printer.flush();
            outputStreamWriter.flush();
        }
    }



}