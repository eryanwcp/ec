/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.utils;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.modules.notice._enum.MessageChannel;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 系统使用的静态变量.
 *
 * @author Eryan
 * @date 2020-12-22
 */
public class NoticeConstants extends AppConstants {

    /**
     * 消息支持的提醒方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static String getMessageTipChannel() {
        String code = "system.message.tipChannel";
        return getConfigValue(code);
    }

    /**
     * 消息支持的提醒方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static List<String> getMessageTipChannelList() {
        String value = getMessageTipChannel();
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }

    /**
     * 消息支持的提醒方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static List<MessageChannel> getMessageTipChannels() {
        List<String> list = getMessageTipChannelList();
        return getMessageChannels(list);
    }


    /**
     * 消息默认提醒方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static String getMessageDefaultTipChannel() {
        String code = "system.message.tipChannel.default";
        return getConfigValue(code);
    }

    /**
     * 消息默认提醒方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static List<String> getMessageDefaultTipChannelList() {
        String value = getMessageDefaultTipChannel();
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }

    /**
     * 消息默认提醒方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static List<MessageChannel> getMessageDefaultTipChannels() {
        List<String> list = getMessageDefaultTipChannelList();
        return getMessageChannels(list);
    }

    private static List<MessageChannel> getMessageChannels(List<String> list) {
        List<MessageChannel> result = Lists.newArrayList();
        list.forEach(v->{
            MessageChannel messageChannel = MessageChannel.getByValue(StringUtils.trim(v));
            if(null != messageChannel){
                result.add(messageChannel);
            }
        });
        return result;
    }


    /**
     * 通知提醒支持方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static String getNoticeTipChannel() {
        String code = "system.notice.tipChannel";
        return getConfigValue(code);
    }

    /**
     * 通知提醒支持方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static List<String> getNoticeTipChannelList() {
        String value = getNoticeTipChannel();
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }

    /**
     * 通知提醒支持方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static List<MessageChannel> getNoticeTipChannels() {
        List<String> list = getNoticeTipChannelList();
        return getMessageChannels(list);
    }



    /**
     * 通知提醒默认方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static String getNoticeDefaultTipChannel() {
        String code = "system.notice.tipChannel.default";
        return getConfigValue(code);
    }

    /**
     * 通知提醒默认方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static List<String> getNoticeDefaultTipChannelList() {
        String value = getNoticeDefaultTipChannel();
        if(StringUtils.isNotBlank(value)){
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }

    /**
     * 通知提醒默认方式，多个之间以“,”分割 {@link MessageChannel}
     * @return
     */
    public static List<MessageChannel> getNoticeDefaultTipChannels() {
        List<String> list = getNoticeDefaultTipChannelList();
        return getMessageChannels(list);
    }
}
