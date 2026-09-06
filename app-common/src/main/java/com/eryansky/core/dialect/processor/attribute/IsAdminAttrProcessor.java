package com.eryansky.core.dialect.processor.attribute;

import com.eryansky.core.dialect.processor.ShiroFacade;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * 当前用户是管理员时才保留元素（替代 JSP &lt;e:isAdmin&gt;）.
 */
public class IsAdminAttrProcessor extends AbstractAttributeTagProcessor {

    private static final String ATTRIBUTE_NAME = "isAdmin";
    private static final int PRECEDENCE = 300;

    public IsAdminAttrProcessor(String dialectPrefix) {
        super(
                TemplateMode.HTML,
                dialectPrefix,
                null,
                false,
                ATTRIBUTE_NAME,
                true,
                PRECEDENCE,
                true);
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext,
                             IProcessableElementTag iProcessableElementTag,
                             AttributeName attributeName,
                             String attributeValue,
                             IElementTagStructureHandler iElementTagStructureHandler) {
        if (ShiroFacade.isAdmin()) {
            iElementTagStructureHandler.removeAttribute(attributeName);
        } else {
            iElementTagStructureHandler.removeElement();
        }
    }
}
