package com.eryansky.modules.sys.sn;

import java.util.Map;

public class CustomStringGenerator implements IGenerator {
    private static final String type = GeneratorConstants.STRING_CUSTOM;

    @Override
    public String getGeneratorType() {
        return type;
    }

    @Override
    public String generate(String format, Map paraMap) {
        return (String) paraMap.get(format);
    }
}