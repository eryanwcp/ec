package com.eryansky.modules.sys.web;

import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot 自定义错误页面
 */
@Controller
@RequestMapping(value = "${server.error.path:${error.path:/error}}")
public class AppErrorController extends AbstractErrorController {

    private static final Logger logger = LoggerFactory.getLogger(AppErrorController.class);

    @Autowired
    private ErrorAttributes errorAttributes;
    @Autowired
    private ObjectProvider<ErrorViewResolver> errorViewResolvers;

    private final ErrorProperties errorProperties = new ErrorProperties();

    public AppErrorController(ErrorAttributes errorAttributes, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
    }


    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        response.setStatus(status.value());
//        Map<String, Object> errorData = Collections.unmodifiableMap(getErrorAttributes(request, ErrorAttributeOptions.defaults()));
        Map<String, Object> errorData = new java.util.HashMap<>(getErrorAttributes(request, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)));
        errorData.put("clientIP", IpUtils.getIpAddr0(request));
        errorData.put("userAgent", UserAgentUtils.getHTTPUserAgent(request));
        logger.error("{}",JsonMapper.toJsonString(errorData));
        ModelAndView modelAndView = null;
        int statusCode = status.value();
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            modelAndView = new ModelAndView("commons/404.html");
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            modelAndView = new ModelAndView("commons/500.html");
        } else {
            modelAndView = new ModelAndView("commons/error.html");
        }
        errorData.putIfAbsent("path",request.getRequestURI());
        modelAndView.addObject("errorData", errorData);
        return modelAndView;

    }

    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }
//        Map<String, Object> body = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Map<String, Object> body = getErrorAttributes(request, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));
        body.put("clientIP", IpUtils.getIpAddr0(request));
        body.put("userAgent", UserAgentUtils.getHTTPUserAgent(request));
        logger.error("{}",JsonMapper.toJsonString(body));
        return new ResponseEntity<>(body, status);
    }


    /**
     * Provide access to the error properties.
     *
     * @return the error properties
     */
    protected ErrorProperties getErrorProperties() {
        return this.errorProperties;
    }


}