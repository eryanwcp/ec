package com.eryansky.server.impl;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import javax.jws.WebService;

import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.modules.notice._enum.MessageChannel;
import com.eryansky.modules.notice._enum.MessageReceiveObjectType;
import com.eryansky.modules.notice._enum.ReceiveObjectType;
import com.eryansky.modules.notice.mapper.Message;
import com.eryansky.modules.notice.service.NoticeService;
import com.eryansky.modules.sys.mapper.Organ;
import com.eryansky.modules.sys.service.OrganService;
import com.eryansky.server.IApiWebService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.server.result.WSResult;


/**
 * 外部API接口实现类
 */
//serviceName与portName属性指明WSDL中的名称, endpointInterface属性指向Interface定义类.
@WebService(serviceName = "ApiWebService", portName = "ApiWebServicePort", endpointInterface = "com.eryansky.server.IApiWebService")
public class ApiWebServiceImpl implements IApiWebService {

    private static Logger logger = LoggerFactory.getLogger(ApiWebServiceImpl.class);


    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    private static final class Static {
        private static UserService userService = SpringContextHolder.getBean(UserService.class);
        private static OrganService organService = SpringContextHolder.getBean(OrganService.class);
        private static NoticeService noticeService = SpringContextHolder.getBean(NoticeService.class);
    }

    @Override
    public WSResult sendMessage(String data) {
        logger.debug(data);
        try {
            Map<String, Object> map = JsonMapper.getInstance().fromJson(data, HashMap.class);
            if(map == null){
                logger.error("请求参数格式错误:" + data);
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "请求参数格式错误:data=" + data);
            }

            String appId = (String) map.get("appId");
            String serviceId = (String) map.get("serviceId");
            String senderId = (String) map.get("senderId");
            String title = (String) map.get("title");
            String category = (String) map.get("category");
            String content = (String) map.get("content");
            String linkUrl = (String) map.get("linkUrl");
            String linkSSO = (String) map.get("linkSSO");
            if (StringUtils.isNotBlank(linkUrl)){
                linkUrl = EncodeUtils.urlDecode(linkUrl);
            }
            String date = (String) map.get("date");
            Date sendTime = DateUtils.parseDate(date);
            List<String> receiveIds = (List) map.get("receiveIds");
            String receiveType = (String) map.get("receiveType");
            List<String> tipType = (List) map.get("tipType");
            String _receiveType = receiveType;
            List<MessageChannel> messageChannels = Lists.newArrayList();
            try {
                checkOptional(appId, "appId");
//                checkOptional(serviceId, "serviceId");
                checkOptional(content, "content");
                if (Collections3.isEmpty(receiveIds)) {
                    return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "请求参数[receiveIds]不能为空");
                }
                if(StringUtils.isBlank(_receiveType) || !MessageReceiveObjectType.Organ.getValue().equals(_receiveType)){
                    _receiveType = MessageReceiveObjectType.User.getValue();
                }

                if(Collections3.isNotEmpty(tipType)){
                    tipType.forEach(t->{
                        MessageChannel messageChannel = MessageChannel.getByValue(t);
                        if(null != messageChannel){
                            messageChannels.add(messageChannel);
                        }
                    });
                }
            } catch (Exception e) {//2.判断必填参数
                logger.error(e.getMessage());
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, e.getMessage());
            }



            //发送者
            User senderUser = null;
            if (StringUtils.isNotBlank(senderId)) {
                senderUser = Static.userService.getUserByIdOrLoginName(senderId);
//                if (senderUser == null) {
//                    return WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "senderId:" + senderId + ",无相关账号信息");
//                }
            }
            if (senderUser == null) {
                senderUser = Static.userService.getSuperUser();
            }

            MessageReceiveObjectType messageReceiveObjectType = MessageReceiveObjectType.getByValue(_receiveType);
            if(null == messageReceiveObjectType){
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "参数[receiveType]异常："+receiveType);
            }

            List<String> receiveObjectIds = new ArrayList<>();
            if(MessageReceiveObjectType.User.equals(messageReceiveObjectType)){
                for (String localLoginName : receiveIds) {
                    User receiveUser = Static.userService.getUserByIdOrLoginName(localLoginName);
                    if (receiveUser == null) {
                        logger.error("统一平台无相关账号信息：{} {}", appId,localLoginName);
                        return WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "统一平台无相关账号信息："+localLoginName);
                    }
                    receiveObjectIds.add(receiveUser.getId());
                }
            }else if(MessageReceiveObjectType.Organ.equals(messageReceiveObjectType)){
                for (String companyCode : receiveIds) {
                    Organ company = Static.organService.getByIdOrCode(companyCode);
                    if (company == null) {
                        logger.error("统一平台无相关机构信息：{} {}",appId,companyCode);
                        return WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "统一平台无相关机构信息："+companyCode);
                    }
                    List<String> organList = Static.organService.findDepartmentAndGroupOrganIdsByCompanyId(company.getId());
                    if(Collections3.isNotEmpty(organList)){
                        receiveObjectIds.addAll(organList);
                    }
                    if(!receiveObjectIds.contains(company.getId())){
                        receiveObjectIds.add(company.getId());
                    }
                }
            }else {
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "参数[receiveType]异常："+receiveType);
            }

            if(Collections3.isEmpty(receiveObjectIds)){
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "未匹配到接收者账号");
            }

            //发送消息
            try {
                CompletableFuture<Message> messageCompletableFuture = MessageUtils.sendMessage(appId, senderUser.getId(),title,category, content, linkUrl, messageReceiveObjectType, receiveObjectIds,sendTime,messageChannels);
                Message message = null;
                try {
                    message = messageCompletableFuture.get();
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                    return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "未知异常");
                }
                return WSResult.buildResult(WSResult.class, WSResult.SUCCESS, "消息发送成功").setData(null == message ? null:message.getId());
            } catch (Exception e) {
                return WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "消息发送失败");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return WSResult.buildDefaultErrorResult(WSResult.class);
        }
    }

    @Override
    public WSResult pushMessage(String data) {
        logger.debug(data);
        try {
            Map<String, Object> map = JsonMapper.getInstance().fromJson(data, HashMap.class);
            if(map == null){
                logger.error("请求参数格式错误:" + data);
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "请求参数格式错误:data=" + data);
            }

            String appId = (String) map.get("appId");
            String messageId = (String) map.get("messageId");
            try {
                checkOptional(appId, "appId");
                checkOptional(messageId, "messageId");
            } catch (Exception e) {//2.判断必填参数
                logger.error(e.getMessage());
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, e.getMessage());
            }

            //推送消息
            try {
                CompletableFuture<Message> messageCompletableFuture = MessageUtils.pushMessage(appId);
                Message message = null;
                try {
                    message = messageCompletableFuture.get();
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                    return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "未知异常");
                }
                return null == message ? WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "消息推送失败"): WSResult.buildResult(WSResult.class, WSResult.SUCCESS, "消息推送成功").setData(message.getId());
            } catch (Exception e) {
                return WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "消息推送失败");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return WSResult.buildDefaultErrorResult(WSResult.class);
        }
    }


    /*
     * 校验参数是否为null和 ""
     */
    public void checkOptional(String optionalValue, String optionalName) throws Exception {
        if (StringUtils.isBlank(optionalValue)) {
            throw new Exception("请求参数[" + optionalName + "]不能为空");
        }
    }



    @Override
    public WSResult sendNotice(String data) {
        logger.debug(data);
        try {
            Map<String, Object> map = JsonMapper.getInstance().fromJson(data, HashMap.class);
            if(map == null){
                logger.error("请求参数格式错误:" + data);
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "请求参数格式错误:data=" + data);
            }

            String appId = (String) map.get("appId");
            String serviceId = (String) map.get("serviceId");
            String senderId = (String) map.get("senderId");
            String type = (String) map.get("type");
            String title = (String) map.get("title");
            String content = (String) map.get("content");
            String date = (String) map.get("date");
            Date sendTime = DateUtils.parseDate(date);
            String receiveType = (String) map.get("receiveType");
            List<String> receiveIds = (List) map.get("receiveIds");
            List<String> tipType = (List) map.get("tipType");
            String _receiveType = receiveType;
            List<MessageChannel> messageChannels = Lists.newArrayList();
            try {
                checkOptional(appId, "appId");
//                checkOptional(serviceId, "serviceId");
                checkOptional(senderId, "senderId");
                checkOptional(title, "title");
                checkOptional(content, "content");
                if (Collections3.isEmpty(receiveIds)) {
                    return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, "请求参数[receiveIds]不能为空");
                }
                if(StringUtils.isBlank(_receiveType) || ReceiveObjectType.Organ.toString().equalsIgnoreCase(_receiveType)){
                    _receiveType = MessageReceiveObjectType.Organ.getValue();
                }

                if(Collections3.isNotEmpty(tipType)){
                    tipType.forEach(t->{
                        MessageChannel messageChannel = MessageChannel.getByValue(t);
                        if(null != messageChannel){
                            messageChannels.add(messageChannel);
                        }
                    });
                }
            } catch (Exception e) {//2.判断必填参数
                logger.error(e.getMessage());
                return WSResult.buildResult(WSResult.class, WSResult.PARAMETER_ERROR, e.getMessage());
            }

            //发送者
            User senderUser = null;
            if (StringUtils.isNotBlank(senderId)) {
                senderUser = Static.userService.getUserByLoginName(senderId);
//                if (senderUser == null) {
//                    return WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "senderId:" + senderId + ",统一平台无相关账号映射信息");
//                }
            }
            if (senderUser == null) {
                senderUser = Static.userService.getSuperUser();
            }

            List<String> organIds = new ArrayList<>();
            for (String companyCode : receiveIds) {
                Organ company = Static.organService.getByIdOrCode(companyCode);
                if (company == null) {
                    logger.error("{},统一平台无相关机构信息:{}",appId,companyCode);
                    return WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "统一平台无相关机构信息："+companyCode);
                }
                List<String> organList = Static.organService.findDepartmentAndGroupOrganIdsByCompanyId(company.getId());
                if(Collections3.isNotEmpty(organList)){
                    organIds.addAll(organList);
                }
                if(!organIds.contains(company.getId())){
                    organIds.add(company.getId());
                }
            }

            //发送
            try {
                Static.noticeService.sendToOrganNotice(appId, type,title, content, sendTime, senderUser.getId(),senderUser.getDefaultOrganId(),organIds,messageChannels);
                return WSResult.buildResult(WSResult.class, WSResult.SUCCESS, "通知发送成功");
            } catch (Exception e) {
                return WSResult.buildResult(WSResult.class, WSResult.IMAGE_ERROR, "通知发送失败");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return WSResult.buildDefaultErrorResult(WSResult.class);
        }
    }

}
