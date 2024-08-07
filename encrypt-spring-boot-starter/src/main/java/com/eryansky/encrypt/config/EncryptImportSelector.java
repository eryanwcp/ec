package com.eryansky.encrypt.config;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * The type Encrypt import selector.
 *
 * @author : 尔演@Eryan
 *
 */
public class EncryptImportSelector implements ImportSelector{

    @NonNull
    @Override
    public String[] selectImports(@NonNull AnnotationMetadata importingClassMetadata) {
        return new String[]{EncryptConfiguration.class.getName()};
    }
}
