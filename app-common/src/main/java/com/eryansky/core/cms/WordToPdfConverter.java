package com.eryansky.core.cms;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * word转pdf 仅支持docx
 * 注：兼容性效果未完全验证
 *
 * @author Eryan
 * @date 2024-10-29
 */
public class WordToPdfConverter {

    private static Logger log = LoggerFactory.getLogger(WordToPdfConverter.class);

    /**
     * word转PDF文件
     *
     * @param wordFile word文件 仅支持docx格式
     * @param pdfFile  PDF文件
     * @throws IOException
     */
    public void convertDocxToPdf(File wordFile, File pdfFile) throws IOException {
        try (InputStream in = new FileInputStream(wordFile);
             OutputStream out = new FileOutputStream(pdfFile)) {

            XWPFDocument document = new XWPFDocument(in);
            PdfConverter.getInstance().convert(document, out, null);
        }
    }


    public static void main(String[] args) throws IOException {
        WordToPdfConverter wordToPdfConverter = new WordToPdfConverter();
        wordToPdfConverter.convertDocxToPdf(new File("d:\\1.docx"), new File("d:\\2.pdf"));
        wordToPdfConverter.convertDocxToPdf(new File("d:\\116001298200.docx"), new File("d:\\116001298200.pdf"));
    }

}
