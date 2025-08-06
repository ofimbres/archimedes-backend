# ☕️ Copilot Prompt Instructions for Backend Development (Java + Spring Boot + DynamoDB)

This project uses **Java (no Lombok)** with **Spring Boot** and **DynamoDB** for backend development. Copilot should generate **clean**, **production-ready**, and **well-structured** code that balances simplicity with extensibility.

---

## 🔧 General Java Guidelines

- Follow **modern Java best practices** (Java 17+ recommended)
- Use **clear naming conventions** (`camelCase` for variables, `PascalCase` for classes)
- Avoid **Lombok**; explicitly write constructors, getters, and setters
- Keep **code modular** and **readable**
- **Comment non-trivial logic** and **document public APIs**
- Prefer **records** for simple DTOs (Java 16+)
- Use **interfaces** for service contracts when needed, but don’t over-engineer
- Handle **null safety** with `Optional` where appropriate

---

## 🌱 Spring Boot Guidelines

- Use **Spring Boot 3.x**
- Avoid unnecessary complexity; minimize boilerplate
- Use **constructor-based dependency injection**
- Use **Spring Configuration Properties** to externalize settings
- Keep **controller/service/repository layers separated** logically
- Prefer **RESTful conventions** in controller design
- For error handling, use:
  - `@ControllerAdvice`
  - Custom exceptions + standardized error responses (JSON)

---

## 💾 DynamoDB Guidelines

- Follow **single-table or composite key design** depending on use case
- Use **Partition Key + Sort Key** wisely (e.g., `PK`, `SK` pattern)
- Model access patterns up front — not like traditional RDBMS
- Keep table structure **flat and denormalized**
- Prefer **@DynamoDBBean** with **manual getter/setter** over Lombok
- Define indexes (GSI, LSI) based on read/query patterns
- Use **AWS SDK v2 or v3**, or **Spring Data DynamoDB** if acceptable
- Avoid tight coupling to table structure — use helper classes or mapping layers

---

## 🏗️ Project Structure Guidelines

- Organize into clear packages:
  - `controller`, `service`, `repository`, `model`, `config`, `dto`, `exception`
- Use `.yml` over `.properties` for configuration readability
- Example folder structure:
