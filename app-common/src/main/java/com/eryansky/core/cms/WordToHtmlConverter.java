package com.eryansky.core.cms;


import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.disk._enum.FolderType;
import com.eryansky.modules.disk.utils.DiskUtils;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * word转html 支持doc、docx 自动导入图片
 * 更新：2016-06-30 支持docx格式
 * @author Eryan
 * @date 2015-01-09
 */
public class WordToHtmlConverter {

    private static Logger log = LoggerFactory.getLogger(WordToHtmlConverter.class);

    /**
     * doc/docx转换成html 
     * @param fileName
     * @return
     * @throws Exception
     */
    public String convertToHtml(String fileName)
            throws Exception{
        if(StringUtils.endsWithIgnoreCase(fileName,".docx")){
            return docxToHtml(fileName);
        }
        return docToHtml(fileName);

    }

    /**
     * doc/docx转换成html
     * @param multipartFile
     * @return
     * @throws Exception
     */
    public String convertToHtml(MultipartFile multipartFile)
            throws Exception{
        if (StringUtils.endsWithIgnoreCase(multipartFile.getOriginalFilename(), ".docx")) {
            return  docxToHtml(multipartFile.getInputStream());
        } else {
            return  docToHtml(multipartFile.getInputStream());
        }

    }

    /**
     * docx文件转html
     * @param fileName
     * @return
     * @throws Exception
     */
    public String docxToHtml(String fileName) throws Exception {
        InputStream input = Files.newInputStream(Paths.get(fileName));
        return docxToHtml(input);
    }

    /**
     * docx文件转html
     * @param inputStream
     * @return
     * @throws Exception
     */
    public String docxToHtml(InputStream inputStream) throws IOException {
        XWPFDocument document = new XWPFDocument(inputStream);
        XHTMLOptions options = XHTMLOptions.create().indent(4);
        // 设置图片生成路径
        options.setImageManager(new ImageManagerExtend(null,null));

        String content = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XHTMLConverter.getInstance().convert(document, baos, options);
            content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            //替换UEditor无法识别的转义字符
//            content = content.replaceAll("&ldquo;", "\"").replaceAll("&rdquo;", "\"").replaceAll("&mdash;", "-");
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return content;
    }

    /**
     * doc文件转html
     * @param fileName
     * @return
     * @throws Exception
     */
    public String docToHtml( String fileName)
            throws IOException,
            ParserConfigurationException {
        InputStream inputStream = Files.newInputStream(Paths.get(fileName));
        return docToHtml(inputStream);

    }

    /**
     * doc文件转html
     * @param inputStream
     * @return
     * @throws Exception
     */
    public String docToHtml(InputStream inputStream)
            throws IOException,
            ParserConfigurationException {
        HWPFDocument wordDocument = new HWPFDocument(inputStream);//WordToHtmlUtils.loadDoc(new FileInputStream(inputFile));
        org.apache.poi.hwpf.converter.WordToHtmlConverter wordToHtmlConverter = new org.apache.poi.hwpf.converter.WordToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument()
        );
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();

        wordToHtmlConverter.setPicturesManager((content, pictureType, suggestedName, widthInches, heightInches) -> {
            com.eryansky.modules.disk.mapper.File pictureFile = null;
            try {
                pictureFile = DiskUtils.saveSystemFile("WORD_IMAGES", FolderType.HIDE.getValue(),null != sessionInfo ? sessionInfo.getUserId():null,new ByteArrayInputStream(content),suggestedName);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
            return pictureFile.getUrl();
        });
        wordToHtmlConverter.processDocument(wordDocument);

        Document htmlDocument = wordToHtmlConverter.getDocument();
        String content = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
            DOMSource domSource = new DOMSource(htmlDocument);
            StreamResult streamResult = new StreamResult(out);

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");////是否添加空格
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
            content = new String(out.toByteArray(), StandardCharsets.UTF_8);
            //替换UEditor无法识别的转义字符
//            content = content.replaceAll("&ldquo;", "\"").replaceAll("&rdquo;", "\"").replaceAll("&mdash;", "-");
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return content;

    }

}
