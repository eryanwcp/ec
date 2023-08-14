package com.eryansky.fastweixin.servlet;

import com.eryansky.fastweixin.util.StrUtil;
import com.eryansky.fastweixin.message.aes.WXBizMsgCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 微信企业平台交互操作基类
 *
 * @author Eryan
 * @date 2016-03-15
 */
@Controller
public abstract class QYWeixinControllerSupport extends QYWeixinSupport {

    private static final Logger logger = LoggerFactory.getLogger(QYWeixinControllerSupport.class);

    /**
     * 绑定微信服务器
     *
     * @param request
     * @return
     */
    @GetMapping()
    @ResponseBody
    protected final String bind(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam(value = "msg_signature", required = false) String msgSignature,
                                @RequestParam(value = "nonce", required = false) String nonce,
                                @RequestParam(value = "echostr", required = false) String echoStr,
                                @RequestParam(value = "timestamp", required = false) String timestamp,
                                @RequestBody(required = false) String xml) throws IOException {
        logger.debug("{'msg_signature'：{},'nonce':{},'echostr':{},'timestamp':{}},'body':{}",msgSignature,nonce,echoStr,timestamp,xml);
        String result = legalStr(request,msgSignature,nonce,timestamp,echoStr);
        response.getWriter().write(result);
        return null;
    }

    /**
     * 微信消息交互处理
     *
     * @param request
     * @return
     */
    @PostMapping()
    @ResponseBody
    protected final String process(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam(value = "msg_signature", required = false) String msgSignature,
                                   @RequestParam(value = "nonce", required = false) String nonce,
                                   @RequestParam(value = "echostr", required = false) String echoStr,
                                   @RequestParam(value = "timestamp", required = false) String timestamp,
                                   @RequestBody(required = false) String xml) throws ServletException, IOException{
//        if(StrUtil.isBlank(legalStr(request))){
//            return "";
//        }
        String result = processRequest(request);
        response.getWriter().write(result);
        return null;
    }

    /**
     * 验证请求URL是否正确的方法
     *
     * @param request
     * @return
     */
    protected String legalStr(HttpServletRequest request,String msgSignature,String nonce,String timestamp,String echoStr){
        String _echoStr = "";
        if(StrUtil.isBlank(getToken()) || StrUtil.isBlank(getAESKey()) || StrUtil.isBlank(getCropId())){
            return _echoStr;
        }
        try (WXBizMsgCrypt pc = new WXBizMsgCrypt(getToken(), getAESKey(), getCropId())){
            _echoStr = pc.verifyUrl(msgSignature, timestamp, nonce, echoStr);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return _echoStr;
    }
}
