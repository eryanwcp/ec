**EC (企业级应用平台)**

## 简介

EC 是一个基于 **Spring Boot 3** 构建的企业级应用开发基础平台，具备 15 年以上长期维护沉淀。项目整合了企业日常办公与系统运维的核心功能模块，包含组织与用户管理、细粒度权限控制（RBAC）、单点登录（SSO）、云盘文件管理、消息通知系统、自研 RPC 远程调用以及丰富的基础开发工具集。

## 核心特性

* **系统管理**：支持组织结构、用户、角色、资源（菜单/按钮）、岗位及数据字典等基础管理。
* **企业级权限与认证**：
  * **单点登录 (SSO)**：内置 SSO 认证中心与客户端集成能力。
  * **声明式权限**：采用自定义仿 Shiro 体系，支持 `@RequiresPermissions` 和 `@RequiresRoles` 注解控制、数据权限过滤及全局会话管理。

* **云盘管理**：提供文件上传下载、在线预览，支持本地文件存储管理与分布式文件管理，支持批量操作与文件检索。
* **消息与通知**：内置系统消息与公告推送，支持按用户、组织、群组等维度多渠道精准投递。
* **自研轻量级 RPC**：提供 `@EnableRPCServer` 与 `@EnableRPCClients` 开箱即用集成，支持服务端与消费端加密通信。
* **任务调度与监控**：基于 Quartz 的分布式任务调度，支持任务监听与自动化清理（如日志、缓存清理）；提供服务端基础指标监控、日志控制台及缓存管理。
* **办公辅助工具集**：内置流水号生成器、Word/Excel/CSV 处理、公式计算引擎、数据加解密、数据脱敏等常用组件。

## 技术栈

* **核心框架**：Spring Boot 3.x (JDK 17+)
* **安全/权限**：自研类 Shiro 安全框架 + 单点登录 (SSO)
* **持久层/数据库**：MyBatis，支持 MariaDB 10.x / MySQL 8.x
* **二级缓存**：J2Cache 扩展（支持 Caffeine 本地缓存 + Redis 集中式缓存）
* **视图模板**：Thymeleaf + SiteMesh + Shiro Dialect
* **服务通信与接口**：自研 RPC 框架
* **容器化与部署**：Maven + Jib 插件打包，支持多节点集群部署（依赖 Redis 处理 Session 及缓存）

## 模块结构

```text
ec
├── app                     # 业务主服务入口与应用配置
├── app-common              # 公共核心模块（数据库配置、Web拦截器、SSO/权限/会话控制、RPC服务端/客户端实现、业务基础代码）
├── client-common           # 客户端通用 SDK & SSO 单点登录客户端组件
├── encrypt-spring-boot-starter # 数据/通信加密扩展 starter
├── fastweixin              # 微信接口对接扩展模块
├── j2cache                 # J2Cache 核心缓存库
├── j2cache-spring-boot-starter # J2Cache Spring Boot Starter 集成包
└── common # 通用工具包

```

## 环境要求

* JDK 17+
* Maven 3.8+
* MariaDB 10.x / MySQL 8.x
* Redis（可选，集群部署、Session 共享与二级缓存时需要）

## 快速开始

**1. 修改配置**
在 `app/src/main/resources` 下，data文件夹下包含数据库初始化脚本。

application-*.properties、config-*.properties、j2cache-*.properties、logback-*.properties对应环境的配置文件中更新数据库连接信息及 Redis 连接参数等.

**2. 本地启动**
在 IDE 中直接运行 `app` 模块下的主启动类即可。

**3. Docker 镜像构建 (基于 Jib)**

```bash
# 本地 Docker 环境构建镜像
mvn clean compile com.google.cloud.tools:jib-maven-plugin:3.5.2:dockerBuild -P docker

# 生成离线 Tar 镜像包
mvn package com.google.cloud.tools:jib-maven-plugin:3.5.2:buildTar -P docker

# 推送镜像至远程仓库
mvn package com.google.cloud.tools:jib-maven-plugin:3.5.2:build -P docker

#可选配置
-DsendCredentialsOverHttp=true

# 加载离线镜像
docker load < jib-image.tar  # Linux
docker load -i jib-image.tar # Windows

```
