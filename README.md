# Dremio MySQL Legacy ARP Connector

## Overview
This is a community based MySQL Legacy Dremio connector made using the ARP framework. It is designed to work with older MySQL versions (5.0.x) that may have compatibility issues with modern JDBC drivers. Check [Dremio Hub](https://github.com/dremio-hub) for more examples and [ARP Docs](https://github.com/dremio-hub/dremio-sqllite-connector#arp-file-format) for documentation.

## What is Dremio?

Dremio delivers lightning fast query speed and a self-service semantic layer operating directly against your data lake storage and other sources. No moving data to proprietary data warehouses or creating cubes, aggregation tables and BI extracts. Just flexibility and control for Data Architects, and self-service for Data Consumers.

## JDBC Driver

This connector uses the **MySQL Connector/J 5.1.x driver** which provides better compatibility with legacy MySQL servers:
- Supports MySQL 5.0.x, 5.1.x, 5.5.x, 5.6.x, and 5.7.x
- Compatible with older authentication methods
- No TLS version conflicts with older MySQL servers

**Required:** Download [MySQL Connector/J 5.1.49](https://dev.mysql.com/downloads/connector/j/5.1.html) and place `mysql-connector-java-5.1.49.jar` in `<DREMIO_HOME>/jars/3rdparty/`

## Usage

### Required Parameters

- Host
- Port (default: 3306)
- Username
- Password

### Optional Parameters

- Database name
- SSL encryption toggle
- Extra JDBC parameters

### Default JDBC Parameters

The connector automatically sets these parameters for MySQL 5.0 compatibility:
- `zeroDateTimeBehavior=convertToNull` - Handles zero dates gracefully
- `useUnicode=true` - Enables Unicode support
- `characterEncoding=UTF-8` - Sets character encoding

### Extra JDBC Parameters Examples

For connection timeout:
```
connectTimeout=10000&socketTimeout=60000
```

For specific character set:
```
characterSetResults=UTF-8
```

For legacy authentication:
```
useOldAliasMetadataBehavior=true
```

## Building and Installation

1. Change the pom's dremio.version to suit your Dremio's version:
   ```xml
   <version.dremio>25.2.0-202410241428100111-a963b970</version.dremio>
   ```

2. Build the connector:
   ```bash
   mvn clean install -DskipTests
   ```

3. Copy files to Dremio:
   ```bash
   cp target/dremio-mysql-legacy-plugin-*.jar <DREMIO_HOME>/jars/
   cp mysql-connector-java-5.1.49.jar <DREMIO_HOME>/jars/3rdparty/
   ```

4. Restart Dremio

## Supported Data Types

| MySQL Type | Dremio Type |
|------------|-------------|
| INT, INTEGER, SMALLINT, TINYINT, MEDIUMINT | integer |
| BIGINT | bigint |
| FLOAT | float |
| DOUBLE, DOUBLE PRECISION, REAL | double |
| DECIMAL, DEC, NUMERIC, FIXED | decimal |
| VARCHAR, CHAR, TEXT, TINYTEXT, MEDIUMTEXT, LONGTEXT | varchar |
| ENUM, SET | varchar |
| DATE | date |
| TIME | time |
| DATETIME, TIMESTAMP | timestamp |
| YEAR | integer |
| BIT, BOOLEAN, BOOL | boolean |
| VARBINARY, BINARY, BLOB, TINYBLOB, MEDIUMBLOB, LONGBLOB | varbinary |

## Supported Operations

- Aggregations: AVG, MAX, MIN, SUM, COUNT, COUNT DISTINCT
- Joins: INNER, LEFT, RIGHT, CROSS (no FULL OUTER)
- Sorting: ORDER BY with LIMIT/OFFSET
- Subqueries: Correlated, scalar, and IN clauses
- Set operations: UNION, UNION ALL

## Limitations

- No window functions (OVER clause) - MySQL 5.0 does not support them
- No EXCEPT operations
- No FULL OUTER JOIN

## Changes

- Initial release for MySQL 5.0.x compatibility
- Uses MySQL Connector/J 5.1.x for legacy support
- ARP configuration for Dremio 25.x
