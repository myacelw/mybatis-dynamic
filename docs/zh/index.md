# MyBatis Dynamic

**MyBatis Dynamic** 是一个基于 MyBatis 构建的强大动态 ORM 框架，旨在简化数据建模和运行时 schema 修改。

## 主要特性

*   **动态建模**：使用注解在 Java 中定义数据模型。框架会自动生成并更新数据库 schema (DDL)。
*   **运行时修改**：模型可以在运行时修改，支持灵活的数据结构。
*   **丰富的 CRUD 和查询**：提供强大的 API 用于增删改查操作以及包括连接查询在内的复杂查询。
*   **Spring Boot 支持**：通过自动配置和简便的模型扫描与 Spring Boot 无缝集成。
*   **零代码 REST API**：（可选）自动为你注册的模型暴露 REST 端点。
*   **可扩展架构**：通过 SPI (Service Provider Interface) 轻松添加对新数据库或自定义命令的支持。

## 模块

本项目模块化设计以分离关注点：

*   `core`：框架的核心。包含建模引擎、数据管理和查询执行逻辑。
*   `spring`：Spring Boot 集成。提供自动配置、`@EnableModelScan` 和 REST 控制器。
*   `draw`：模型关系可视化工具（基于 ELK）。
*   `sample`：演示用法的 Spring Boot 参考应用程序。

## 文档

*   [动态建模简介](intro-to-dynamic-modeling.md)：告别繁琐 XML，用代码定义模型。
*   [快速开始](quick-start.md)：几分钟内上手运行。
*   [核心功能](../core-features.md)：了解 `DataManager`、`ModelService` 和基本 CRUD。
*   [高级功能](advanced-features.md)：掌握链式 API、递归查询和权限管理。
*   [API 参考](../api-reference.md)：注解和 ConditionBuilder 的详细参考。
*   [Spring 集成](spring-integration.md)：在 Spring Boot 中使用框架。
*   [扩展指南](extensions.md)：扩展框架功能。
