package com.eryansky.core.rpc;

import com.eryansky.core.rpc.config.ProviderConfig;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

public class RPCImportSelector implements ImportSelector {

    @NonNull
    @Override
    public String[] selectImports(@NonNull AnnotationMetadata importingClassMetadata) {
        return new String[]{ProviderConfig.class.getName()};
    }
}