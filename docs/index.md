# MyBatis Dynamic

**MyBatis Dynamic** is a powerful dynamic ORM framework built on top of MyBatis, designed to simplify data modeling and runtime schema modifications.

## Key Features

*   **Dynamic Modeling**: Define data models in Java with annotations. The framework automatically generates and updates the database schema (DDL).
*   **Runtime Modification**: Models can be modified at runtime, allowing for flexible data structures.
*   **Rich CRUD & Querying**: Provides a robust API for Create, Read, Update, Delete operations and complex queries including joins.
*   **Spring Boot Support**: Seamless integration with Spring Boot via auto-configuration and easy model scanning.
*   **Zero-Code REST API**: (Optional) Automatically exposes REST endpoints for your registered models.
*   **Extensible Architecture**: Easily add support for new databases or custom commands via SPI (Service Provider Interface).

## Modules

The project is modularized to separate concerns:

*   `core`: The heart of the framework. Contains the modeling engine, data management, and query execution logic.
*   `spring`: Spring Boot integration. Provides auto-configuration, `@EnableModelScan`, and REST controllers.
*   `draw`: A visualization tool for model relationships (ELK-based).
*   `sample`: A reference Spring Boot application demonstrating usage.

## Documentation

*   [Quick Start](quick-start.md): Get up and running in minutes.
*   [Core Features](core-features.md): Learn about `DataManager`, `ModelService`, and basic CRUD.
*   [Advanced Features](advanced-features.md): Master Chain APIs, Recursive Queries, and Permissions.
*   [API Reference](api-reference.md): Detailed reference for Annotations and ConditionBuilder.
*   [Spring Integration](spring-integration.md): Using the framework with Spring Boot.
*   [Extensions](extensions.md): Extending the framework.
