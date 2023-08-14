package com.eryansky.fastweixin.api.response;

import com.eryansky.fastweixin.api.entity.Menu;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class GetMenuResponse extends BaseResponse {

    private Menu menu;

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}
