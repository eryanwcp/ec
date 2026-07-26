package com.eryansky.fastweixin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.entity.Menu;
import com.eryansky.fastweixin.api.enums.ResultType;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.GetMenuResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.CollectionUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单相关API
 * 1.3.7支持个性化菜单
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class MenuAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(MenuAPI.class);

    public MenuAPI(ApiConfig config) {
        super(config);
    }

    /**
     * 创建菜单
     * 1.3.7开始支持个性化菜单
     *
     * @param menu 菜单对象
     * @return 调用结果
     */
    public ResultType createMenu(Menu menu) {
        BeanUtil.requireNonNull(menu, "menu is null");
        String url = BASE_API_URL;
        if (BeanUtil.isNull(menu.getMatchrule())) {
            //普通菜单
            LOG.debug("创建普通菜单.....");
            url += "cgi-bin/menu/create?access_token=#";
        } else {
            //个性化菜单
            LOG.debug("创建个性化菜单.....");
            url += "cgi-bin/menu/addconditional?access_token=#";
        }
        BaseResponse response = executePost(url, menu.toJsonString());
        return ResultType.get(response.getErrcode());
    }

    /**
     * 获取所有菜单
     *
     * @return 菜单列表对象
     */
    public GetMenuResponse getMenu() {
        GetMenuResponse response;
        LOG.debug("获取菜单信息.....");
        String url = BASE_API_URL + "cgi-bin/menu/get?access_token=#";

        BaseResponse r = executeGet(url);
        if (isSuccess(r.getErrcode())) {
            JsonNode jsonNode = JSONUtil.getJSONFromString(r.getErrmsg());
            processMenuButtons(jsonNode);
            response = JSONUtil.toBean(jsonNode.toString(), GetMenuResponse.class);
        } else {
            response = JSONUtil.toBean(r.toJsonString(), GetMenuResponse.class);
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
     * 删除所有菜单，包括个性化菜单
     *
     * @return 调用结果
     */
    public ResultType deleteMenu() {
        LOG.debug("删除菜单.....");
        String url = BASE_API_URL + "cgi-bin/menu/delete?access_token=#";
        BaseResponse response = executeGet(url);
        return ResultType.get(response.getErrcode());
    }

    /**
     * 删除个性化菜单
     *
     * @param menuId 个性化菜单ID
     * @return 调用结果
     * @since 1.3.7
     */
    public ResultType deleteConditionalMenu(String menuId) {
        BeanUtil.requireNonNull(menuId, "menuid is null");
        LOG.debug("删除个性化菜单.....");
        String url = BASE_API_URL + "cgi-bin/menu/delconditional?access_token=#";
        Map<String, String> params = new HashMap<String, String>();
        params.put("menuid", menuId);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return ResultType.get(response.getErrcode());
    }

    /**
     * 测试个性化菜单
     *
     * @param userId 可以是粉丝的OpenID，也可以是粉丝的微信号
     * @return 该用户可以看到的菜单
     * @since 1.3.7
     */
    public GetMenuResponse tryMatchMenu(String userId) {
        BeanUtil.requireNonNull(userId, "userId is null");
        LOG.debug("测试个性化菜单.....");
        GetMenuResponse response;
        String url = BASE_API_URL + "cgi-bin/menu/trymatch?access_token=#";
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", userId);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
//        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
//        response = JSONUtil.toBean(resultJson, GetMenuResponse.class);
        if (isSuccess(r.getErrcode())) {
            JsonNode jsonNode = JSONUtil.getJSONFromString(r.getErrmsg());
            processMenuButtons(jsonNode);
            response = JSONUtil.toBean(jsonNode.toString(), GetMenuResponse.class);
        } else {
            response = JSONUtil.toBean(r.toJsonString(), GetMenuResponse.class);
        }
        return response;
    }
}
