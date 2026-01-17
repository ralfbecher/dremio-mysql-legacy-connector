# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Dremio MySQL Legacy ARP Connector - a community plugin enabling Dremio to connect to older MySQL versions (5.0.x) that have compatibility issues with modern JDBC drivers. Built using Dremio's ARP (Abstracted Relational Pattern) framework, targeting Dremio 25.2.0.

## Build Commands

```bash
# Clean build without tests
mvn clean install -DskipTests

# Clean build with tests
mvn clean install
```

The build uses Maven checkstyle plugin (configured at `src/main/checkstyle/`) and compiles for Java 8.

## Testing

There is no automated test suite. `TestMySQLConnection.java` is a standalone connectivity utility for manual testing:

```bash
java -cp mysql-connector-java-5.1.49.jar:. TestMySQLConnection <host> <port> <user> <password>
```

## Architecture

### Core Components

1. **MysqlLegacyConf.java** (`src/main/java/.../conf/`) - Configuration class extending `AbstractArpConf`. Builds JDBC URLs with automatic parameters (zeroDateTimeBehavior, useUnicode, characterEncoding, SSL settings). Uses `@Secret` annotation for password field.

2. **MysqlLegacyDialect.java** (`src/main/java/.../dialect/`) - Custom SQL dialect extending `ArpDialect`. Contains critical fix for MySQL 5.0's broken `getTables()` JDBC method using direct `information_schema` query.

3. **mysql-legacy-arp.yaml** (`src/main/resources/arp/implementation/`) - Declarative ARP metadata defining data type mappings (35+ types), supported operations, and SQL capabilities. Window functions are disabled (MySQL 5.0 limitation).

4. **MYSQL-layout.json** (`src/main/resources/`) - UI form definition for Dremio's connection dialog.

5. **sabot-module.conf** (`src/main/resources/`) - HOCON configuration registering the plugin package for Dremio classpath scanning.

### Key Design Decisions

- Uses MySQL Connector/J 5.1.x driver (`com.mysql.jdbc.Driver`) for legacy MySQL 5.0.x compatibility
- Schema discovery bypasses broken JDBC metadata methods with direct `information_schema.tables` queries
- Window functions (`OVER` clause) disabled - MySQL 5.0 doesn't support them
- FULL OUTER JOIN and EXCEPT operations disabled

### Deployment

Plugin JAR goes to `<DREMIO_HOME>/jars/`, JDBC driver to `<DREMIO_HOME>/jars/3rdparty/`. Requires Dremio restart after installation.
