# 📝 Week 5 Development Log: Relational Persistence & JPA Entities

## 🎯 Objectives
* Implement the persistence layer skeleton for the Hexagonal Architecture.
* Configure database version control using Flyway.
* Map domain models to Spring Data JPA entities.

## 🛠️ Executed Tasks
1. **Flyway Migrations:** Created `V1__create_core_tables.sql` mapping the complete domain schema to PostgreSQL 16.
2. **JPA Entities Construction:** Created `JpaEntity` classes for `User`, `BankAccount`, `Property`, `Subscription`, `Contract`, and `Payment`.
3. **Data Type Alignment:** Handled Enum mapping as Strings and ensured `NUMERIC` precision for financial values (Money).
4. **Spring Data Repositories:** Created native interfaces extending `JpaRepository` for all entities.
5. **Initial Adapters & Mappers:** Configured manual mapping strategies between pure Domain objects and JPA proxies to isolate the core.

## 🛑 Blockers & Solutions
* **Schema Drift:** Addressed mismatches between rich domain attributes and SQL columns by syncing `V1.sql` directly with the aggregate requirements.