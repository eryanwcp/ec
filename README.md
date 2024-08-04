# ec

### 快速开始
> 项目使用技术
- Spring Boot 2.7.*
> 依赖外部环境
- redis (可选)
- MariaDB/MySQL


Docker打包


    mvn package -P prod
    mvn package com.google.cloud.tools:jib-maven-plugin:3.4.3:buildTar -P docker
    mvn package com.google.cloud.tools:jib-maven-plugin:3.4.3:buildTar -DsendCredentialsOverHttp=true

Docker本地部署


    mvn clean compile com.google.cloud.tools:jib-maven-plugin:3.4.3:dockerBuild -P docker
    mvn clean compile com.google.cloud.tools:jib-maven-plugin:3.4.3:dockerBuild -DsendCredentialsOverHttp=true -P docker

Docker发布


    mvn package com.google.cloud.tools:jib-maven-plugin:3.4.3:build -P docker
    mvn package com.google.cloud.tools:jib-maven-plugin:3.4.3:build -DsendCredentialsOverHttp=true -P docker


Docker加载镜像

    Linux：docker load < jib-image.tar
    Windows：docker load -i jib-image.tar


