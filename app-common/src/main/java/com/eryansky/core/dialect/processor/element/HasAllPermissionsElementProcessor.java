package com.eryansky.core.dialect.processor.element;

import com.eryansky.core.dialect.processor.ShiroFacade;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.List;

import static com.eryansky.core.dialect.processor.ThymeleafFacade.evaluateAsStringsWithDelimiter;
import static com.eryansky.core.dialect.processor.ThymeleafFacade.getRawValue;

public class HasAllPermissionsElementProcessor extends AbstractElementTagProcessor {

    private static final String ELEMENT_NAME = "hasallpermissions";
    private static final int PRECEDENCE = 300;

    private static final String DELIMITER = ",";

    public HasAllPermissionsElementProcessor(String dialectPrefix) {
        super(
                TemplateMode.HTML, // This processor will apply only to HTML mode
                dialectPrefix, // Prefix to be applied to name for matching
                ELEMENT_NAME, // Tag name: match specifically this tag
                true, // Apply dialect prefix to tag name
                null, // No attribute name: will match by tag name
                false, // No prefix to be applied to attribute name
                PRECEDENCE); // Precedence (inside dialect's own precedence)
    }

    @Override
    protected void doProcess(ITemplateContext iTemplateContext,
                             IProcessableElementTag iProcessableElementTag,
                             IElementTagStructureHandler iElementTagStructureHandler) {
        final String rawValue = getRawValue(iProcessableElementTag, "name");
        final List<String> values = evaluateAsStringsWithDelimiter(iTemplateContext, rawValue, DELIMITER);

        if (ShiroFacade.hasAllPermissions(values)) {
            iElementTagStructureHandler.removeTags();
        } else {
            iElementTagStructureHandler.removeElement();
        }
    }
}