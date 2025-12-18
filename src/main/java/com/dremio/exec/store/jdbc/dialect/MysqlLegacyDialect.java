package com.dremio.exec.store.jdbc.dialect;

import com.dremio.exec.store.jdbc.dialect.arp.ArpDialect;
import com.dremio.exec.store.jdbc.dialect.arp.ArpYaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom dialect for MySQL Legacy that extends ArpDialect to handle
 * MySQL-specific syntax.
 *
 * MySQL uses backticks (`) for identifier quoting and standard single
 * quotes for string literals.
 *
 * MySQL databases are exposed as JDBC catalogs (not schemas).
 * The YAML configuration specifies supports_catalogs: true and
 * supports_schemas: false to match MySQL's catalog-only model.
 */
public class MysqlLegacyDialect extends ArpDialect {

  private static final Logger logger = LoggerFactory.getLogger(MysqlLegacyDialect.class);

  public MysqlLegacyDialect(ArpYaml yaml) {
    super(yaml);
    logger.info("MysqlLegacyDialect initialized");
  }

  // Use default ArpDialect schema fetcher - relies on JDBC metadata
  // with supports_catalogs: true and supports_schemas: false in YAML

  @Override
  public boolean supportsCharSet() {
    // Return false to prevent Calcite from generating charset syntax
    return false;
  }

  @Override
  public void quoteStringLiteral(StringBuilder buf, String charsetName, String val) {
    // MySQL uses standard single-quote string literals
    buf.append("'");
    buf.append(val.replace("'", "''"));
    buf.append("'");
  }

  @Override
  public boolean supportsNestedAggregations() {
    // MySQL 5.0 does not support nested aggregations
    return false;
  }
}
