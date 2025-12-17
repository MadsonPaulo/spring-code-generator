# spring-code-generator

spring-code-generator is a Java/Spring-based code generation tool that produces entities, DTOs, repositories, and services from existing SQL Server tables and views.

It is designed for database-first and legacy-driven projects where schema metadata already exists and consistent code generation is required.

## Scope and Limitations

- SQL Server only
- Relies on database metadata (tables, views, columns, keys)
- Not intended to be a multi-database or ORM-agnostic generator

## Project Assumptions

The generator assumes that the target project follows a standard layered architecture organized under a single root package.

The expected package layout includes the following logical layers:

- dto  
- entity  
- repository  
- service  

Based on this structure, the user specifies:
- which tables and/or views should be processed
- which artifacts should be generated (DTO, entity, repository, service)

The generator automatically:
- creates the corresponding classes
- assigns them to the appropriate packages
- resolves class and attribute names consistently

## Naming Conventions

The generator was originally built for corporate SQL Server naming standards commonly found in legacy and enterprise systems, but it also supports modern snake_case naming.

Examples of table code to the generated Java class name:
- T999AAAA -> T999Aaaa
- V999AAAA -> V999Aaaa
- user_profile -> UserProfile

This allows the tool to work with both legacy enterprise databases and modern, cloud-oriented schemas.

## Running Without SQL Server (Mock Profile)

For development and testing, the project provides a mock profile using application-mock.properties.

When this profile is active:
- repository access is mocked
- no SQL Server instance is required
- the application can be started normally

Run with:
-Dspring.profiles.active=mock

## Intended Use

This project is intended for:
- enterprise modernization efforts
- database-first or legacy-driven systems
- teams that want to reduce boilerplate while keeping full control over generated code

## Notes

This repository focuses on code generation and structural consistency.  
It does not handle runtime persistence or schema management.