/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.bstek.ureport.utils;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author huanzhou
 * @date 2023/10/28
 */
public class PdfUtils {

    private final static Logger log                        = LoggerFactory.getLogger(PdfUtils.class);

//    public static void main(String[] args) throws IOException {
//        // pdf转换成单张图片
//        PdfUtils.convertPdf2Image("https://file-examples-com.github.io/uploads/2017/10/file-sample_150kB.pdf", System.getProperty("java.io.tmpdir") + "anjia", "sample.png");
//        // pdf转换成多张
//        PdfUtils.convertPdf2Images("https://file-examples-com.github.io/uploads/2017/10/file-sample_150kB.pdf", System.getProperty("java.io.tmpdir") + "anjia", "sample.png");
//    }

    /**
     * 将pdf转成一张图片,如果要输出的文件已经存在，则不会进行转换
     *
     * @param url      pdf url
     * @param fileName 文件名带png后缀，例如 xxxx.png
     * @param dir      存放文件夹，如果不存在会自动创建
     * @return 文件，如果报错，则会返回null
     * @throws IOException 文件下载失败
     */
    public static File convertPdf2Image(String url, String dir, String fileName) throws IOException {
        File pdfFile = new File(new File(dir).getAbsolutePath() + File.separator + fileName + ".pdf");
        pdfFile.getParentFile().mkdirs();
        FileUtils.copyURLToFile(new URL(url), pdfFile);
        return convertPdf2Image(pdfFile, dir, fileName);
    }

    /**
     * 将pdf转成一张图片,如果要输出的文件已经存在，则不会进行转换
     *
     * @param pdfFile  pdf 文件
     * @param fileName 文件名带png后缀，例如 xxxx.png
     * @param dir      存放文件夹，如果不存在会自动创建
     * @return 文件，如果报错，则会返回null
     */
    public static File convertPdf2Image(File pdfFile, String dir, String fileName) {
        File pngFile = new File(new File(dir).getAbsolutePath() + File.separator + fileName);
        if (pngFile.exists()) {
            return pngFile;
        }
        try (final PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            // 不知道图片的宽和高，所以先定义个null
            BufferedImage pdfImage = null;
            // pdf有多少页
            int pageSize = document.getNumberOfPages();
            int y = 0;
            for (int i = 0; i < pageSize; ++i) {
                // 每页pdf内容
                BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);
                // 如果是第一页需要初始化 BufferedImage
                if (Objects.isNull(pdfImage)) {
                    // 假设每页一样宽，一样高，高度就是每页高度*总页数
                    pdfImage = new BufferedImage(bim.getWidth(),
                            bim.getHeight() * pageSize, BufferedImage.TYPE_INT_ARGB);
                }
                // 将每页pdf画到总的pdfImage上,x坐标=0，y坐标=之前所有页的高度和
                pdfImage.getGraphics().drawImage(bim, 0, y, null);
                y += bim.getHeight();
            }

            assert pdfImage != null;
            ImageIO.write(pdfImage, "png", pngFile);
            return pngFile;
        } catch (Exception ex) {
            log.error("pdf转换png失败", ex);
        }
        return null;
    }

    /**
     * 将pdf转换成多张图片(1页pdf转换成1张图片),如果要输出的图片已经存在，则不会进行转换
     *
     * @param url          pdf url
     * @param dir          存放目录
     * @param baseFileName pdf文件名
     * @return 如果转换成功会返回 图片文件list，如果失败会返回null
     * @throws IOException 下载文件失败
     */
    public static List<File> convertPdf2Images(String url, String dir, String baseFileName) throws IOException {
        File pdfFile = new File(dir + File.separator + baseFileName + ".pdf");
        pdfFile.getParentFile().mkdirs();
        FileUtils.copyURLToFile(new URL(url), pdfFile);
        return convertPdf2Images(pdfFile, dir, baseFileName);
    }

    /**
     * 将pdf转换成多张图片(1页pdf转换成1张图片),如果要输出的图片已经存在，则不会进行转换
     *
     * @param pdfFile      pdf 文件
     * @param dir          存放目录
     * @param baseFileName pdf文件名
     * @return 如果转换成功会返回 图片文件list，如果失败会返回null
     */
    public static List<File> convertPdf2Images(File pdfFile, String dir, String baseFileName) {
        List<File> images = null;
        File directory = new File(dir);
        directory.mkdirs();
        try (final PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            // pdf有多少页
            int pageSize = document.getNumberOfPages();
            images = new ArrayList<>(pageSize);
            File pageFile;
            for (int i = 0; i < pageSize; ++i) {
                pageFile = new File(String.format("%s%s%s-%s.png", directory.getAbsolutePath(), File.separator, baseFileName, i));
                if (pageFile.exists()) {
                    continue;
                }
                BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);
                ImageIOUtil.writeImage(bim, pageFile.getAbsolutePath(), 300);
                images.add(pageFile);
            }
        } catch (Exception ex) {
            log.error("pdf转换png失败", ex);
        }
        return images;

    }
}
