# Product Guide: mybatis-dynamic

## Overview

`mybatis-dynamic` is a dynamic ORM framework built on top of MyBatis, designed to accelerate the development of Java applications with complex and evolving data models. It empowers developers to define data models in Java, and the framework will automatically generate and update the database schema. This dynamic modeling capability extends to runtime, allowing for on-the-fly modifications to the model structure.

## Target Audience

The primary users for `mybatis-dynamic` are **Java developers using Spring Boot who need to rapidly build applications with complex data models**. The framework is designed to seamlessly integrate into their existing workflows and provide significant productivity gains.

## Key Problems Solved

`mybatis-dynamic` addresses several common pain points in modern application development:

*   **Reduces Boilerplate Code:** It significantly cuts down on the amount of repetitive code required for CRUD (Create, Read, Update, Delete) operations and dynamic queries in MyBatis. This allows developers to focus on business logic rather than data access plumbing.
*   **Simplifies Schema Management:** The framework automates the process of database schema creation and evolution. Developers can manage their database structure directly from their Java models, eliminating the need for manual SQL scripts and reducing the risk of schema-out-of-sync errors.
*   **Visualizes Complex Relationships:** With its integrated visualization tool, `mybatis-dynamic` provides a clear and interactive way to understand the relationships between different data models, which is invaluable for complex domains.

## Core Features

*   **Dynamic Modeling**: Define data models as Java classes and let the framework handle the database schema.
*   **Runtime Model Modification**: Adapt your data models at runtime to meet changing requirements.
*   **Rich Querying API**: A fluent API for building complex queries, including joins and recursive queries.
*   **Spring Boot Integration**: Effortless integration with Spring Boot, including auto-configuration for DAOs and services.
*   **Model Relationship Visualization**: A web-based tool to visualize and explore model relationships.
*   **Extensible and Modular:** The project is well-modularized, with a core engine that can be used independently of Spring.
