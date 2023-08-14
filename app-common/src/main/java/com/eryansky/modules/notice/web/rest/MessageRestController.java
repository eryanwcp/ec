package com.eryansky.modules.notice.web.rest;

import com.eryansky.common.model.Result;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.listener.SystemInitListener;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.server.result.WSResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 消息接口
 */
@RestApi
@Controller
@RequestMapping(value = {"rest/message","rest/notice/message"})
public class MessageRestController extends SimpleController {


    @Override
    protected void initBinder(WebDataBinder binder) {
        super.defaultWebDataBinder(binder);
    }

    /**
     * 发送消息
     * @param data 参数说明:
     * {
     * 	 appId:"appId",
     * 	 serviceId:"serviceId",
     * 	 senderId:"",
     * 	 sendTime:"yyyy-MM-dd HH:mm:ss",
     *   title:"标题",
     *   category:"分类",
     *   content:"消息内容",
     *   linkUrl:"",
     *   receiveIds:["loginName1","loginName2",...]
     * }
     * appId      :应用编码   必选
     * serviceId  :服务ID  可选
     * senderId   :发送者账号(第三方系统账号，需在统一平台做账号映射) 可选
     * sendTime   :发布时间(格式：yyyy-MM-dd HH:mm:ss)  可选
     * title    :消息标题     可选
     * category    :消息分类     可选
     * content    :消息内容     必选
     * linkUrl    :消息链接    可选
     * receiveIds :接收者账号(第三方系统账号，需在统一平台做账号映射) 必选
     * @return
     * {
     * code:1,
     * msg:"提示信息",
     * data:"消息ID"
     * }
     * code 1:成功 0:失败
     * msg 提示信息
     * data 返回的数据信息
     */
    @Logging(value = "消息接口-发送消息",logType = LogType.REST)
    @PostMapping(value = { "sendMessage"})
    @ResponseBody
    public Result sendMessage(String data) {
        if(SystemInitListener.Static.apiWebService != null){
            WSResult wsResult = SystemInitListener.Static.apiWebService.sendMessage(data);
            return WSResult.SUCCESS.equals(wsResult.getCode()) ? Result.successResult().setMsg(wsResult.getMessage()).setData(wsResult.getData()): Result.errorResult().setMsg(wsResult.getMessage());
        }
        return Result.errorResult();
    }


    /**
     * 发送消息
     * @param data 参数说明:
     * {
     * 	 appId:"appId",
     * 	 messageId:"messageId"
     * }
     * appId      :应用编码   必选
     * messageId  :消息ID  必选
     * @return
     * {
     * code:1,
     * msg:"提示信息",
     * data:"消息ID"
     * }
     * code 1:成功 0:失败
     * msg 提示信息
     * data 返回的数据信息
     */
    @Logging(value = "消息接口-推送消息",logType = LogType.REST)
    @PostMapping(value = { "pushMessage"})
    @ResponseBody
    public Result pushMessage(String data) {
        if(SystemInitListener.Static.apiWebService != null){
            WSResult wsResult = SystemInitListener.Static.apiWebService.pushMessage(data);
            return WSResult.SUCCESS.equals(wsResult.getCode()) ? Result.successResult().setMsg(wsResult.getMessage()).setData(wsResult.getData()): Result.errorResult().setMsg(wsResult.getMessage());
        }
        return Result.errorResult();
    }

    /**
     * 发送通知
     * @param data 参数说明:
     * data:json字符串
     * data={
     * 	 senderId:"",
     * 	 sendTime:"yyyy-MM-dd HH:mm:ss",
     *   type:"通知类型",
     *   title:"标题",
     *   content:"内容",
     *   date:"",
     *   receiveType:"user",
     *   receiveIds:["loginName1","loginName2",...]
     *   tipType:["message","weixin"]
     * }
     * appId      :应用编码   必选
     * serviceId  :服务ID  可选
     * senderId   :发布账号 必选
     * sendTime       :发布时间(格式：yyyy-MM-dd HH:mm:ss)  可选
     * senderOrganCode   :发布部门编码 可选
     * type       :公告类型     可选
     * title      :标题     必选
     * content    :内容（支持HTML富文本）     必选
     * receiveType:接收对象类型（部门：organ，用户：user）默认为"organ" 可选（暂不可用）
     * receiveIds :接收对象(组织机构编码/用户账号) 必选
     * tipType    :消息通道（消息：Message，企业微信:QYWeixin，邮件:Mail，短信:SMS，APP:APP） 默认为："['Message','QYWeixin',"APP"]" 可选
     * @return 返回消息ID
     */
    @Logging(value = "消息接口-发送通知",logType = LogType.REST)
    @PostMapping(value = { "sendNotice"})
    @ResponseBody
    public Result sendNotice(String data) {
        if(SystemInitListener.Static.apiWebService != null){
            WSResult wsResult = SystemInitListener.Static.apiWebService.sendNotice(data);
            return WSResult.SUCCESS.equals(wsResult.getCode()) ? Result.successResult().setMsg(wsResult.getMessage()).setData(wsResult.getData()): Result.errorResult().setMsg(wsResult.getMessage());
        }
        return Result.errorResult();
    }
}
