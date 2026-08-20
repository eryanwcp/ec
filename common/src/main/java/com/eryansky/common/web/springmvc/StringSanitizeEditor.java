package com.eryansky.common.web.springmvc;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import java.beans.PropertyEditorSupport;

/**
 * 替代传统 StringEscapeEditor 的安全性 HTML 清理编辑器
 */
public class StringSanitizeEditor extends PropertyEditorSupport {

    private final boolean trim;
    private final Safelist safelist;

    /**
     * 默认构造函数：使用 none() 白名单，剥离所有 HTML 标签（适用于普通纯文本输入）
     */
    public StringSanitizeEditor() {
        this(true, Safelist.none());
    }

    /**
     * 构造函数
     * @param trim 是否自动去除字符串两端空格
     * @param safelist Jsoup 的 Safelist 白名单规则（如 Safelist.basic(), Safelist.relaxed() 等）
     */
    public StringSanitizeEditor(boolean trim, Safelist safelist) {
        super();
        this.trim = trim;
        this.safelist = safelist != null ? safelist : Safelist.none();
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        return value != null ? value.toString() : "";
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) {
            setValue(null);
        } else {
            String value = text;
            if (trim) {
                value = value.trim();
            }
            // 使用 Jsoup 进行安全清理，清除如 <script>、onerror= 等恶意代码
            value = Jsoup.clean(value, safelist);
            setValue(value);
        }
    }
}