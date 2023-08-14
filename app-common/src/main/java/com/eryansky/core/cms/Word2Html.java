package com.eryansky.core.cms;


import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.io.FileUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.utils.AppConstants;
import fr.opensagres.poi.xwpf.converter.core.BasicURIResolver;
import fr.opensagres.poi.xwpf.converter.core.FileImageExtractor;
import fr.opensagres.poi.xwpf.converter.core.XWPFConverterException;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * word转html 支持doc、docx 自动导入图片
 * 更新：2016-06-30 支持docx格式
 * @author Eryan
 * @date 2015-01-09
 */
public class Word2Html {

    private static Logger logger = LoggerFactory.getLogger(Word2Html.class);

    public static final String ENCODING_UTF8 = "UTF-8";

    public static void main(String argv[]) {
        try {
            docToHtml(null, "D://角色栏目.doc", "D://1.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * doc/docx转换成html 
     * @param request
     * @param fileName
     * @return
     * @throws Exception
     */
    public static String convertToHtml(HttpServletRequest request, String fileName)
            throws Exception{
        if(StringUtils.endsWithIgnoreCase(fileName,".docx")){
            return docxToHtml(request,fileName,null);
        }
        return docToHtml(request, fileName, null);

    }

    /**
     * docx文件转html
     * @param request
     * @param fileName
     * @param outPutFile
     * @return
     * @throws Exception
     */
    public static String docxToHtml(HttpServletRequest request, String fileName, String outPutFile) throws Exception {
        InputStream input = new FileInputStream(fileName);
        return docxToHtml(request,input,outPutFile);
    }

    /**
     * docx文件转html
     * @param request
     * @param inputStream
     * @param outPutFile
     * @return
     * @throws Exception
     */
    public static String docxToHtml(final HttpServletRequest request, InputStream inputStream, String outPutFile) throws IOException {
        XWPFDocument document = new XWPFDocument(inputStream);
        XHTMLOptions options = XHTMLOptions.create().indent(4);
        String diskPath = null;
        String urlPath = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        final SessionInfo sessionInfo1 = sessionInfo;
        if (sessionInfo1 != null) {
            diskPath = AppConstants.getAppBasePath() + "/cms/userfiles/" + sessionInfo1.getUserId() + "/images/cms/article/";
            urlPath = request.getContextPath() + "/userfiles/" + sessionInfo1.getUserId() + "/images/cms/article/";
        } else {
            diskPath = AppConstants.getAppBasePath() + "/cms/userfiles/images/cms/article/";
            urlPath = request.getContextPath() + "/userfiles/images/cms/article/";
        }
        diskPath += new SimpleDateFormat("yyyy/MM/").format(new Date());
        urlPath += new SimpleDateFormat("yyyy/MM/").format(new Date());
        File imageFolder = new File(diskPath);
        // 设置图片生成路径
        options.setExtractor(new FileImageExtractor(imageFolder));
        // 设置图片链接，去掉后使用相对路径
        options.URIResolver(new BasicURIResolver(urlPath));
//         options.URIResolver(new FileURIResolver(imageFolder));
//        options.setIgnoreStylesIfUnused(false);
//        options.setFragment(true);

//        OutputStream out = new FileOutputStream(new File(outPutFile));
//        XHTMLConverter.getInstance().convert(document, out, options);

        String content = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XHTMLConverter.getInstance().convert(document, baos, options);
            content = new String(baos.toByteArray(), ENCODING_UTF8);
            //替换UEditor无法识别的转义字符
            content = content.replaceAll("&ldquo;", "\"").replaceAll("&rdquo;", "\"").replaceAll("&mdash;", "-");
        } catch (XWPFConverterException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        if (StringUtils.isNotBlank(outPutFile)) {
            writeFile(content, outPutFile);
        }
        return content;
    }

    /**
     * doc文件转html
     * @param request
     * @param fileName
     * @param outPutFile
     * @return
     * @throws Exception
     */
    public static String docToHtml(HttpServletRequest request, String fileName, String outPutFile)
            throws TransformerException, IOException,
            ParserConfigurationException {
        InputStream inputStream = new FileInputStream(fileName);
        return docToHtml(request, inputStream, outPutFile);

    }

    /**
     * doc文件转html
     * @param request
     * @param inputStream
     * @param outPutFile
     * @return
     * @throws Exception
     */
    public static String docToHtml(final HttpServletRequest request, InputStream inputStream, String outPutFile)
            throws TransformerException, IOException,
            ParserConfigurationException {
        HWPFDocument wordDocument = new HWPFDocument(inputStream);//WordToHtmlUtils.loadDoc(new FileInputStream(inputFile));
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument()
        );
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        final SessionInfo sessionInfo1 = sessionInfo;

        final long nowTime = System.currentTimeMillis();
        wordToHtmlConverter.setPicturesManager(new PicturesManager() {
            public String savePicture(byte[] content,
                                      PictureType pictureType, String suggestedName,
                                      float widthInches, float heightInches) {
                String path = null;
                if (sessionInfo1 != null) {
                    path = request.getContextPath()+"/userfiles/" + sessionInfo1.getUserId() + "/images/cms/article/";
                } else {
                    path = request.getContextPath()+"/userfiles/images/cms/article/";
                }
                path += new SimpleDateFormat("yyyy/MM/").format(new Date());
                return path + nowTime + "_" + suggestedName;
            }
        });
        wordToHtmlConverter.processDocument(wordDocument);
        //save pictures
        List pics = wordDocument.getPicturesTable().getAllPictures();
        if (pics != null) {
            String path = null;
            if (sessionInfo != null) {
                path = AppConstants.getAppBasePath() + "/cms/userfiles/" + sessionInfo.getUserId() + "/images/cms/article/";
            } else {
                path = AppConstants.getAppBasePath() + "/cms/userfiles/images/cms/article/";
            }
            path += new SimpleDateFormat("yyyy/MM/").format(new Date());
            FileUtils.checkSaveDir(path);
            for (int i = 0; i < pics.size(); i++) {
                Picture pic = (Picture) pics.get(i);
                try {
//                    pic.writeImageContent(new FileOutputStream(path+ pic.suggestFullFileName()));
                    pic.writeImageContent(new FileOutputStream(path + nowTime + "_" + pic.suggestFullFileName()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        Document htmlDocument = wordToHtmlConverter.getDocument();
        String content = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
            DOMSource domSource = new DOMSource(htmlDocument);
            StreamResult streamResult = new StreamResult(out);

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, ENCODING_UTF8);
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");////是否添加空格
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
            content = new String(out.toByteArray(), ENCODING_UTF8);
            //替换UEditor无法识别的转义字符
            content = content.replaceAll("&ldquo;", "\"").replaceAll("&rdquo;", "\"").replaceAll("&mdash;", "-");
        } catch (TransformerFactoryConfigurationError e) {
            logger.error(e.getMessage(),e);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(),e);
        } catch (TransformerException e) {
            logger.error(e.getMessage(),e);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
        }
        if(StringUtils.isNotBlank(outPutFile)){
            writeFile(content, outPutFile);
        }
        return content;

    }

    public static void writeFile(String content, String path) {
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            File file = new File(path);
            fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos, ENCODING_UTF8));
            bw.write(content);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fos != null)
                    fos.close();
            } catch (IOException ie) {
            }
        }
    }
}
