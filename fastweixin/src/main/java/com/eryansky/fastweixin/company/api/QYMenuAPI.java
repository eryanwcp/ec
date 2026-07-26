package com.eryansky.fastweixin.company.api;

import com.eryansky.fastweixin.company.api.config.QYAPIConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.company.api.entity.QYMenu;
import com.eryansky.fastweixin.company.api.enums.QYResultType;
import com.eryansky.fastweixin.company.api.response.GetQYMenuResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.CollectionUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *  自定义菜单管理
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYMenuAPI extends QYBaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(QYMenuAPI.class);

    /**
     * 构造方法，设置apiConfig
     *
     * @param config 微信API配置对象
     */
    public QYMenuAPI(QYAPIConfig config) {
        super(config);
    }

    /**
     * 创建自定义菜单。
     * @param menu 自定义菜单
     * @param agentId 需要生成菜单的应用ID
     * @return 操作结果
     */
    public QYResultType create(QYMenu menu, String agentId){
        BeanUtil.requireNonNull(menu, "菜单不能为空！");
        String url = BASE_API_URL + "cgi-bin/menu/create?access_token=#&agentid=" + agentId;
        BaseResponse response = executePost(url, JSONUtil.toJson(menu));
        return QYResultType.get(response.getErrcode());
    }

    /**
     * 获取菜单列表。
     * @param agentId 目标应用的ID
     * @return QYMenu。与创建菜单时的对象一致。
     */
    public GetQYMenuResponse list(String agentId){
        BeanUtil.requireNonNull(agentId, "应用ID不能为空");
        GetQYMenuResponse response;
        String url = BASE_API_URL + "cgi-bin/menu/get?access_token=#&agentid=" + agentId;
        BaseResponse r = executeGet(url);
        if (isSuccess(r.getErrcode())) {
            JsonNode jsonNode = JSONUtil.getJSONFromString(r.getErrmsg());
            processMenuButtons(jsonNode);
            response = JSONUtil.toBean(jsonNode.toString(), GetQYMenuResponse.class);
        } else {
            response = JSONUtil.toBean(r.toJsonString(), GetQYMenuResponse.class);
        }
        return response;
    }

    private void processMenuButtons(JsonNode root) {
        JsonNode menu = root.get("menu");
        if (menu == null) return;
        JsonNode buttonsNode = menu.get("button");
        if (buttonsNode == null || !buttonsNode.isArray()) return;
        ArrayNode buttons = (ArrayNode) buttonsNode;

        for (JsonNode button : buttons) {
            if (button instanceof ObjectNode buttonNode) {
                JsonNode subButtons = buttonNode.get("sub_button");
                if (subButtons != null && subButtons.isArray()) {
                    for (JsonNode sub : subButtons) {
                        if (sub instanceof ObjectNode subNode) {
                            updateTypeToUpper(subNode);
                        }
                    }
                } else {
                    updateTypeToUpper(buttonNode);
                }
            }
        }
    }

    private void updateTypeToUpper(ObjectNode node) {
        JsonNode typeNode = node.get("type");
        if (typeNode != null && typeNode.isTextual()) {
            node.put("type", typeNode.asText().toUpperCase());
        }
    }

    /**
     * 删除自定义菜单
     * @param agentId 目标应用ID
     * @return 操作结果
     */
    public QYResultType delete(String agentId){
        BeanUtil.requireNonNull(agentId, "AgentId不能为空");
        String url = BASE_API_URL + "cgi-bin/menu/delete?access_token=#&agentid=" + agentId;
        BaseResponse response = executeGet(url);
        return QYResultType.get(response.getErrcode());
    }
}
