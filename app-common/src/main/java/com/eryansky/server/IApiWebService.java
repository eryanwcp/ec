package com.eryansky.server;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.eryansky.server.result.WSResult;


/**
 * 接口类
 */
@WebService(name = "IApiWebService")
public interface IApiWebService {
	
	/**
	 * 发送消息
	 * 参数说明:
	 * data:json字符串
	 * data={
	 * 	 appId:"appId",
	 * 	 serviceId:"serviceId",
	 * 	 senderId:"",
	 * 	 sendTime:"yyyy-MM-dd HH:mm:ss",
	 *   title:"标题",
	 *   category:"分类",
	 *   content:"消息内容",
	 *   linkUrl:"",
	 *   receiveType:"user",
	 *   receiveIds:["loginName1","loginName2",...]
	 *   tipType:["Message","QYWeixin"]
	 * }
	 * appId      :应用编码   必选
	 * serviceId  :服务ID  可选
	 * senderId   :发送者账号(第三方系统账号，需在统一平台做账号映射) 可选
	 * sendTime   :发布时间(格式：yyyy-MM-dd HH:mm:ss)  可选
	 * title      :消息标题    可选
	 * category   :消息分类    可选
	 * content    :消息内容    必选
	 * linkUrl    :消息链接    可选
	 * linkSSO    :消息链接是否单点 默认：否（0） 1|0    可选
	 * receiveType:接收对象类型（用户：user，部门：organ）默认为"user" 可选（暂不可用）
	 * receiveIds :接收者账号 必选
	 * tipType    :消息通道（消息：Message，企业微信:QYWeixin，邮件:Mail，短信:SMS，APP:APP） 默认为："['Message','QYWeixin',"APP"]" 可选
	 * @return
	 * 
	 */
	@WebMethod
	WSResult sendMessage(@WebParam(name = "data") String data);

	/**
	 * 发送消息
	 * 参数说明:
	 * data:json字符串
	 * data={
	 * 	 appId:"appId",
	 * 	 serviceId:"serviceId",
	 *   messageId:"消息ID",
	 * }
	 * appId      :应用编码   必选
	 * messageId  :消息ID  必选
	 * @return
	 *
	 */
	WSResult pushMessage(@WebParam(name = "data") String data);

	/**
	 * 发送通知
	 * 参数说明:
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
	 * appId   :应用编码   必选
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
	 * @return
	 *
	 */
	@WebMethod
	WSResult sendNotice(@WebParam(name = "data") String data);
}
