# Technology Stack: mybatis-dynamic

## Overview

The `mybatis-dynamic` project is built upon a robust and widely adopted technology stack within the Java ecosystem. This document outlines the core technologies used across the project's modules.

## Programming Language

*   **Java**: The entire project is implemented in Java, leveraging its strong typing, extensive ecosystem, and platform independence.

## Frameworks

*   **Spring Boot**: Used for simplified application development, dependency injection, and auto-configuration, particularly within the `spring` and `sample` modules.
*   **MyBatis**: The core ORM framework upon which `mybatis-dynamic` builds its dynamic modeling and querying capabilities.

## Build Tool

*   **Maven**: Manages project dependencies, builds, and releases for all modules within the monorepo structure.

## Database Technologies

The framework itself is designed to be database-agnostic through its dialect system. However, for development, testing, and demonstration purposes, the following database drivers are included:

*   **H2 Database**: An in-memory and file-based relational database, primarily used for testing and local development.
*   **MySQL**: A popular open-source relational database management system.
*   **PostgreSQL**: A powerful, open-source object-relational database system.
*   **OceanBase**: A distributed relational database, also used for testing compatibility.
