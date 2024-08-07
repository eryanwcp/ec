package com.eryansky.encrypt.spel;

import org.springframework.expression.ParserContext;
import org.springframework.lang.NonNull;

/**
 * The type Sp el parser context.
 *
 * @author : 尔演@Eryan
 *
 */
public class SpELParserContext{
    /**
     * The constant PREFIX.
     */
    public static final String PREFIX = "::";
    /**
     * The constant SUFFIX.
     */
    public static final String SUFFIX = "::";

    /**
     * The constant PARSER_CONTEXT.
     */
    public static final ParserContext PARSER_CONTEXT = new ParserContext() {
        @Override
        public boolean isTemplate() {
            return true;
        }

        @NonNull
        @Override
        public String getExpressionPrefix() {
            return PREFIX;
        }

        @NonNull
        @Override
        public String getExpressionSuffix() {
            return SUFFIX;
        }
    };
}
