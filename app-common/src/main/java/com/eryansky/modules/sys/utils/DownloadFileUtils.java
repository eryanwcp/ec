package com.eryansky.modules.sys.utils;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;

/**
 * 文件下载工具类
 */
public class DownloadFileUtils {


    private static Logger logger = LoggerFactory.getLogger(DownloadFileUtils.class);

    private DownloadFileUtils() {
    }

    /**
     * 文件下载 分片断点下载
     *
     * @param downloadFile
     * @param downloadFileName
     * @param response
     * @param request
     * @throws Exception
     */
    public static void downRangeFile(File downloadFile, String downloadFileName, HttpServletResponse response, HttpServletRequest request) throws Exception {
        // 文件不存在
        if (!downloadFile.exists() || !downloadFile.canRead()) {
            // 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 记录文件大小
        long fileLength = downloadFile.length();
        String _fileName = null != downloadFileName ? downloadFileName : downloadFile.getName();
        String range = request.getHeader("Range");

        if (request.getParameter("showHeader") != null) {
            Enumeration<String> paramNames = request.getHeaderNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                if (name != null && name.length() > 0) {
                    String value = request.getHeader(name);
                    logger.info("request {}:{}", name, value);
                }
            }
        }


        //开始下载位置
        long startByte = 0;
        //结束下载位置
        long endByte = fileLength - 1;
        int responseStatus = HttpServletResponse.SC_OK;
        //有range的话
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            responseStatus = HttpServletResponse.SC_PARTIAL_CONTENT;
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //根据range解析下载分片的位置区间
                if (ranges.length == 1) {
                    //情况1，如：bytes=-1024  从开始字节到第1024个字节的数据
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //情况2，如：bytes=1024-  第1024个字节到最后字节的数据
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //情况3，如：bytes=1024-2048  第1024个字节到2048个字节的数据
                else if (ranges.length == 2 && StringUtils.isNotBlank(ranges[0]) && StringUtils.isNotBlank(ranges[1])) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }

            } catch (NumberFormatException e) {
                logger.error(e.getMessage(),e);
                loggerHTTPHeader(request,response);
                startByte = 0;
                endByte = fileLength - 1;
                responseStatus = HttpServletResponse.SC_OK;
            }
        }

        //要下载的长度
        long contentLength = endByte - startByte + 1;
        String contentType = response.getContentType();
        if (null == contentType) {
            //设置response的Content-Type,set the MIME type
            String mimeType = request.getServletContext().getMimeType(_fileName);
            if (null != mimeType) {
                response.setContentType(mimeType);
            } else {
                response.setContentType("application/x-download");
            }
        }
        // 告诉客户端允许断点续传多线程连接下载,响应的格式是:Accept-Ranges: bytes
        response.setHeader("Accept-Ranges", "bytes");

        //Content-Length 表示资源内容长度，即：文件大小
        response.setHeader("Content-Length", String.valueOf(contentLength));
        //Content-Range 表示响应了多少数据，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + fileLength);
        response.setStatus(responseStatus);

        String contentDisposition = response.getHeader("Content-Disposition");
        if(null == contentDisposition){
            WebUtils.setDownloadableHeader(request, response, _fileName);
        }

        if (request.getParameter("showHeader") != null) {
            Collection<String> responseHeaderNames = response.getHeaderNames();
            for (String name : responseHeaderNames) {
                String value = response.getHeader(name);
                logger.info("response {}:{}", name, value);
            }
        }


        //已传送数据大小
        long transmitted = 0;
        try (BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
             RandomAccessFile randomAccessFile = new RandomAccessFile(downloadFile, "r");) {
            byte[] buff = new byte[2048];
            int len = 0;
            randomAccessFile.seek(startByte);
            //判断是否到了最后不足2048（buff的length）个byte
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            //处理不足buff.length部分
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buff, 0, (int) (contentLength - transmitted));
                outputStream.write(buff, 0, len);
                transmitted += len;
            }

            outputStream.flush();
            response.flushBuffer();
        } catch (IOException e) {
            if(!"ClientAbortException".equals(e.getClass().getSimpleName())){
                logger.error(e.getMessage(), e);
            }
            /**
             * 在写数据的时候， 对于 ClientAbortException 之类的异常，
             * 是因为客户端取消了下载，而服务器端继续向浏览器写入数据时， 抛出这个异常，这个是正常的。
             * 尤其是对于迅雷这种吸血的客户端软件， 明明已经有一个线程在读取 bytes=1275856879-1275877358，
             * 如果短时间内没有读取完毕，迅雷会再启第二个、第三个。。。线程来读取相同的字节段， 直到有一个线程读取完毕，迅雷会 KILL
             * 掉其他正在下载同一字节段的线程， 强行中止字节读出，造成服务器抛 ClientAbortException。
             * 所以，我们忽略这种异常
             */
            // ignore
        }
    }

    /**
     * 打印HTTP请求、响应日志
     *
     * @param request
     * @param response
     */
    public static void loggerHTTPHeader(HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> paramNames = request.getHeaderNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            if (name != null && name.length() > 0) {
                String value = request.getHeader(name);
                logger.info("request {}:{}", name, value);
            }
        }
        Collection<String> responseHeaderNames = response.getHeaderNames();
        for (String name : responseHeaderNames) {
            String value = response.getHeader(name);
            logger.info("response {}:{}", name, value);
        }
    }


    /**
     * @param fileName
     * @return
     */
    public static String getContentType(String fileName) {
        String contentType = "application/octet-stream";
        if (fileName.lastIndexOf(".") < 0) {
            return contentType;
        }
        String fileSuffix = fileName.toLowerCase().substring(fileName.lastIndexOf(".") + 1);

        if (fileSuffix.equals("html") || fileSuffix.equals("htm") || fileSuffix.equals("shtml")) {
            contentType = "text/html";
        } else if (fileSuffix.equals("css")) {
            contentType = "text/css";
        } else if (fileSuffix.equals("xml")) {
            contentType = "text/xml";
        } else if (fileSuffix.equals("gif")) {
            contentType = "image/gif";
        } else if (fileSuffix.equals("jpeg") || fileSuffix.equals("jpg")) {
            contentType = "image/jpeg";
        } else if (fileSuffix.equals("js")) {
            contentType = "application/x-javascript";
        } else if (fileSuffix.equals("atom")) {
            contentType = "application/atom+xml";
        } else if (fileSuffix.equals("rss")) {
            contentType = "application/rss+xml";
        } else if (fileSuffix.equals("mml")) {
            contentType = "text/mathml";
        } else if (fileSuffix.equals("txt")) {
            contentType = "text/plain";
        } else if (fileSuffix.equals("jad")) {
            contentType = "text/vnd.sun.j2me.app-descriptor";
        } else if (fileSuffix.equals("wml")) {
            contentType = "text/vnd.wap.wml";
        } else if (fileSuffix.equals("htc")) {
            contentType = "text/x-component";
        } else if (fileSuffix.equals("png")) {
            contentType = "image/png";
        } else if (fileSuffix.equals("tif") || fileSuffix.equals("tiff")) {
            contentType = "image/tiff";
        } else if (fileSuffix.equals("wbmp")) {
            contentType = "image/vnd.wap.wbmp";
        } else if (fileSuffix.equals("ico")) {
            contentType = "image/x-icon";
        } else if (fileSuffix.equals("jng")) {
            contentType = "image/x-jng";
        } else if (fileSuffix.equals("bmp")) {
            contentType = "image/x-ms-bmp";
        } else if (fileSuffix.equals("svg")) {
            contentType = "image/svg+xml";
        } else if (fileSuffix.equals("jar") || fileSuffix.equals("var") || fileSuffix.equals("ear")) {
            contentType = "application/java-archive";
        } else if (fileSuffix.equals("doc")) {
            contentType = "application/msword";
        } else if (fileSuffix.equals("pdf")) {
            contentType = "application/pdf";
        } else if (fileSuffix.equals("rtf")) {
            contentType = "application/rtf";
        } else if (fileSuffix.equals("xls")) {
            contentType = "application/vnd.ms-excel";
        } else if (fileSuffix.equals("ppt")) {
            contentType = "application/vnd.ms-powerpoint";
        } else if (fileSuffix.equals("7z")) {
            contentType = "application/x-7z-compressed";
        } else if (fileSuffix.equals("rar")) {
            contentType = "application/x-rar-compressed";
        } else if (fileSuffix.equals("swf")) {
            contentType = "application/x-shockwave-flash";
        } else if (fileSuffix.equals("rpm")) {
            contentType = "application/x-redhat-package-manager";
        } else if (fileSuffix.equals("der") || fileSuffix.equals("pem") || fileSuffix.equals("crt")) {
            contentType = "application/x-x509-ca-cert";
        } else if (fileSuffix.equals("xhtml")) {
            contentType = "application/xhtml+xml";
        } else if (fileSuffix.equals("zip")) {
            contentType = "application/zip";
        } else if (fileSuffix.equals("mid") || fileSuffix.equals("midi") || fileSuffix.equals("kar")) {
            contentType = "audio/midi";
        } else if (fileSuffix.equals("mp3")) {
            contentType = "audio/mpeg";
        } else if (fileSuffix.equals("ogg")) {
            contentType = "audio/ogg";
        } else if (fileSuffix.equals("m4a")) {
            contentType = "audio/x-m4a";
        } else if (fileSuffix.equals("ra")) {
            contentType = "audio/x-realaudio";
        } else if (fileSuffix.equals("3gpp") || fileSuffix.equals("3gp")) {
            contentType = "video/3gpp";
        } else if (fileSuffix.equals("mp4")) {
            contentType = "video/mp4";
        } else if (fileSuffix.equals("mpeg") || fileSuffix.equals("mpg")) {
            contentType = "video/mpeg";
        } else if (fileSuffix.equals("mov")) {
            contentType = "video/quicktime";
        } else if (fileSuffix.equals("flv")) {
            contentType = "video/x-flv";
        } else if (fileSuffix.equals("m4v")) {
            contentType = "video/x-m4v";
        } else if (fileSuffix.equals("mng")) {
            contentType = "video/x-mng";
        } else if (fileSuffix.equals("asx") || fileSuffix.equals("asf")) {
            contentType = "video/x-ms-asf";
        } else if (fileSuffix.equals("wmv")) {
            contentType = "video/x-ms-wmv";
        } else if (fileSuffix.equals("avi")) {
            contentType = "video/x-msvideo";
        } else if (fileSuffix.equals("apk")) {
            contentType = "application/vnd.android.package-archive";
        }

        return contentType;
    }

}