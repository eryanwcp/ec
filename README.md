

# EC (企业级应用平台)

## 简介

EC 是一个基于 **Spring Boot 3** 构建的企业级应用平台，服务13年以上。该项目整合了企业日常办公所需的核心功能模块，包括用户与组织管理、细粒度权限控制（RBAC）、云盘文件管理、消息通知系统、RPC 远程调用以及丰富的基础开发辅助工具。

## 核心特性

*   **企业级权限体系**: 自定义权限体系，防Shiro注解，提供了基于注解 (`@RequiresPermissions`, `@RequiresRoles`) 的声明式权限控制，支持数据权限过滤和会话管理。
*   **灵活的实体模型**: 提供基础数据实体 (`BaseEntity`, `DataEntity`, `TreeEntity`, `PTreeEntity`)，简化 CRUD 和树形结构开发。
*   **云盘管理模块**: 包含文件上传、下载、预览及 FTP 远程存储管理，支持文件检索和批量操作。
*   **消息通知系统**: 内置系统消息与公告通知管理，支持多种消息接收对象（用户、组织、群组）和通知渠道。
*   **RPC 远程调用**: 自研轻量级 RPC 框架 (`EnableRPCServer`, `EnableRPCClients`)，支持服务端与消费端的快速集成与加密通信。
*   **常用工具集**: 内置 Word/Excel 处理、CSV 导出、公式处理器等办公常用组件。
*   **定时任务管理**: 基于 Quartz 的分布式任务调度，支持任务监听与自动化清理（如日志清理、缓存清理）。

## 技术栈

*   **核心框架**: Spring Boot 3.x
*   **安全框架**: 自定安全框架，类 Shiro
*   **ORM**: MyBatis
*   **数据库**: MariaDB / MySQL
*   **缓存**: 二级缓存J2Cache扩展，Redis (可选)
*   **视图模板**: Thymeleaf + SiteMesh + Shiro Dialect
*   **构建工具**: Maven
*   **容器化**: Docker (支持 Jib 打包)

## 环境要求

*   JDK 17+
*   MariaDB 10.x / MySQL 8.x
*   Redis (可选，用于缓存和 Session 管理)

## 快速开始

### 1. 配置文件
请在 `application.yml` 或对应配置文件中配置数据库连接信息以及 Redis 连接（如果启用）。

### 2. Docker 部署 (推荐)

项目支持使用 Jib 进行 Docker 镜像构建。

**Docker 本地部署 (构建镜像):**
```bash
mvn clean compile com.google.cloud.tools:jib-maven-plugin:3.5.2:dockerBuild -P docker
mvn clean compile com.google.cloud.tools:jib-maven-plugin:3.5.2:dockerBuild -P docker -DsendCredentialsOverHttp=true
```

**Docker 打包 (生成 Tar 包):**
```bash
mvn package com.google.cloud.tools:jib-maven-plugin:3.5.2:buildTar -P docker
mvn package com.google.cloud.tools:jib-maven-plugin:3.5.2:buildTar -P docker -DsendCredentialsOverHttp=true
```

**Docker 发布 (推送到仓库):**
```bash
mvn package com.google.cloud.tools:jib-maven-plugin:3.5.2:build -P docker
mvn package com.google.cloud.tools:jib-maven-plugin:3.5.2:build -P docker -DsendCredentialsOverHttp=true
```

**加载离线镜像:**
```bash
# Linux
docker load < jib-image.tar
# Windows
docker load -i jib-image.tar
```

### 3. 本地运行
在 IDE 中直接运行 Application 主类即可启动服务。

## 模块说明

项目主要包含以下核心模块：

*   **app-common**: 公共核心模块。
    *   **配置类**: 数据库配置 (`DBConfigurer`)、Web配置 (`MvcConfigurer`)、Quartz配置等。
    *   **安全模块**: 权限拦截器 (`AuthorityInterceptor`)、会话管理 (`SessionInfo`)、Shiro方言等。
    *   **RPC 框架**: 远程调用服务端与客户端的核心实现。
    *   **业务服务**: 包含系统管理 (`sys`)、通知消息 (`notice`)、云盘 (`disk`) 等基础服务代码。

## 接口文档

项目集成了 **SpringDoc OpenAPI**，启动后可通过以下地址访问 Swagger UI：
`http://{host}:{port}/swagger-ui.html`

---

*本项目结构清晰，功能完善，适用于快速搭建企业级内部管理系统。*
