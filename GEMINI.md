# GEMINI.md

This document provides a summary of the `mybatis-dynamic` project to be used as a context for engineering tasks.

## Project Overview

`mybatis-dynamic` is a dynamic ORM framework built on top of MyBatis. It provides the following key features:

*   **Dynamic Modeling**: Define data models in Java and the framework will automatically generate and update the database schema.
*   **Runtime Model Modification**: Models can be modified at runtime.
*   **CRUD and Querying**: Provides a rich API for performing CRUD operations and complex queries, including joins and recursive queries.
*   **Spring Boot Integration**: Seamlessly integrates with Spring Boot, providing auto-configuration for DAOs and services.
*   **Model Relationship Visualization**: A tool to visualize the relationships between models.

The project is written in Java and uses Maven for dependency management.

## Modules

The project is divided into the following modules:

*   `core`: The core module that implements the dynamic modeling, data management, and query engine. It can be used as a standalone library without Spring.
*   `spring`: Provides Spring Boot auto-configuration, which simplifies the setup and usage of the framework in a Spring Boot application.
*   `draw`: A web-based tool for visualizing model relationships. It uses the Eclipse Layout Kernel (ELK) for graph layout.
*   `sample`: A sample Spring Boot application that demonstrates how to use the framework.

## Building and Running

### Building the Project

The project can be built using Maven. Run the following command from the root directory of the project:

```bash
mvn clean install
```

### Running the Sample Application

The `sample` module is a Spring Boot application that can be run to see the framework in action.

1.  Navigate to the `sample` directory:

    ```bash
    cd sample
    ```

2.  Run the application using the Spring Boot Maven plugin:

    ```bash
    mvn spring-boot:run
    ```

The application will be available at `http://localhost:8080`.

## Development Conventions

*   **Coding Style**: The code follows standard Java conventions. It uses Lombok to reduce boilerplate code and the SLF4J API for logging.
*   **Dependency Management**: The project uses Maven for dependency management. The parent `pom.xml` file defines the versions of the main dependencies.
*   **Modularity**: The project is well-modularized, with clear separation of concerns between the `core`, `spring`, `draw`, and `sample` modules.
*   **Service Loading**: The `core` module uses the `java.util.ServiceLoader` mechanism to discover and load implementations of its main interfaces, such as `Execution`. This makes the framework extensible.

# Custom Instructions
- Always use Context7 MCP when I need library/API documentation, code generation, setup or configuration steps without me having to explicitly ask.
- 当涉及 Java 架构设计模式或 MyBatis 生态库的 API 查询时，优先通过 Context7 获取最新的官方文档信息。
