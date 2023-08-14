# ec

### 快速开始
> 项目使用技术
- Spring Boot2
> 依赖外部环境
- redis (可选)
- MariaDB/MySQL


Docker打包


    mvn package com.google.cloud.tools:jib-maven-plugin:3.3.2:buildTar -DsendCredentialsOverHttp=true


Docker发布


    mvn package com.google.cloud.tools:jib-maven-plugin:3.3.2:build -DsendCredentialsOverHttp=true


Docker验证码字体库确实修复方法（在容器中执行）

    apk add --update font-adobe-100dpi ttf-dejavu fontconfig

