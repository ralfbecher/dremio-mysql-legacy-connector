package com.dremio.exec.store.jdbc.dialect;

import com.dremio.exec.store.jdbc.dialect.arp.ArpDialect;
import com.dremio.exec.store.jdbc.dialect.arp.ArpYaml;
import com.dremio.exec.store.jdbc.JdbcPluginConfig;
import com.dremio.exec.store.jdbc.JdbcSchemaFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom dialect for MySQL Legacy that extends ArpDialect to handle
 * MySQL 5.0 specific behavior.
 *
 * Key issue: MySQL 5.0's JDBC driver returns empty results for
 * getTables(null, null, %, TABLE) - it requires a specific catalog.
 * We use information_schema queries instead.
 */
public class MysqlLegacyDialect extends ArpDialect {

  private static final Logger logger = LoggerFactory.getLogger(MysqlLegacyDialect.class);

  // Query to get all tables from information_schema
  // Excludes system databases (information_schema, mysql, performance_schema, sys)
  // CAT = catalog (database), SCH = schema (null for MySQL), NME = table name
  private static final String SCHEMA_FETCH_QUERY =
      "SELECT TABLE_SCHEMA CAT, NULL SCH, TABLE_NAME NME " +
      "FROM information_schema.tables " +
      "WHERE TABLE_SCHEMA NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys') " +
      "AND TABLE_TYPE = 'BASE TABLE'";

  public MysqlLegacyDialect(ArpYaml yaml) {
    super(yaml);
  }

  @Override
  public JdbcSchemaFetcher newSchemaFetcher(JdbcPluginConfig config) {
    logger.info("Creating MySQL Legacy schema fetcher with information_schema query");
    return new ArpSchemaFetcher(SCHEMA_FETCH_QUERY, config);
  }

  @Override
  public boolean supportsCharSet() {
    return false;
  }

  @Override
  public void quoteStringLiteral(StringBuilder buf, String charsetName, String val) {
    buf.append("'");
    buf.append(val.replace("'", "''"));
    buf.append("'");
  }

  @Override
  public boolean supportsNestedAggregations() {
    return false;
  }
}
