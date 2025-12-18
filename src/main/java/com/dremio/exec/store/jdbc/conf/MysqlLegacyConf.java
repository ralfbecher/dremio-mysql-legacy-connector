package com.dremio.exec.store.jdbc.conf;

import com.dremio.exec.catalog.conf.SourceType;
import com.dremio.exec.catalog.conf.DisplayMetadata;
import com.dremio.exec.catalog.conf.Secret;
import com.dremio.exec.store.jdbc.dialect.MysqlLegacyDialect;
import com.dremio.exec.store.jdbc.JdbcPluginConfig;
import com.dremio.exec.store.jdbc.CloseableDataSource;
import com.dremio.exec.store.jdbc.DataSources;
import com.dremio.services.credentials.CredentialsService;
import com.dremio.options.OptionManager;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;
import java.util.Properties;

import io.protostuff.Tag;

/**
 * Configuration for MySQL Legacy sources.
 *
 * This is an ARP-based connector designed to work with MySQL 5.0.x
 * using the MySQL Connector/J 5.1.x driver.
 */
@SourceType(value = "MYSQL_LEGACY", label = "MySQL Legacy", uiConfig = "MYSQL-layout.json", externalQuerySupported = true)
public class MysqlLegacyConf extends AbstractArpConf<MysqlLegacyConf> {

  private static final String ARP_FILENAME = "arp/implementation/mysql-legacy-arp.yaml";

  // Dialect loaded from ARP YAML - using custom MysqlLegacyDialect for MySQL-specific handling
  private static final MysqlLegacyDialect ARP_DIALECT =
      AbstractArpConf.loadArpFile(ARP_FILENAME, (MysqlLegacyDialect::new));

  /**
   * MySQL Connector/J 5.1.x driver for MySQL 5.0 compatibility.
   *
   * Make sure to put mysql-connector-java-5.1.49.jar in /opt/dremio/jars/3rdparty/
   * Download from: https://dev.mysql.com/downloads/connector/j/5.1.html
   */
  private static final String DRIVER = "com.mysql.jdbc.Driver";

  // ===== UI fields =====

  @NotBlank
  @Tag(1)
  @DisplayMetadata(label = "Host")
  public String host;

  @Tag(2)
  @DisplayMetadata(label = "Port")
  public int port = 3306;

  @Tag(3)
  @DisplayMetadata(label = "Database (optional)")
  public String database;

  @NotBlank
  @Tag(4)
  @DisplayMetadata(label = "Username")
  public String username;

  @NotBlank
  @Tag(5)
  @Secret
  @DisplayMetadata(label = "Password")
  public String password;

  @Tag(6)
  @DisplayMetadata(label = "Use SSL")
  public boolean useSsl = false;

  @Tag(7)
  @DisplayMetadata(label = "Extra JDBC parameters (optional)")
  public String extraParams;

  // ===== Required overrides =====

  @Override
  @JsonIgnore
  public MysqlLegacyDialect getDialect() {
    return ARP_DIALECT;
  }

  @Override
  @JsonIgnore
  public JdbcPluginConfig buildPluginConfig(
      JdbcPluginConfig.Builder configBuilder,
      CredentialsService credentialsService,
      OptionManager optionManager) {
    // Build MySQL JDBC URL
    // Format: jdbc:mysql://host:port/database?param=value&param2=value2
    StringBuilder sb = new StringBuilder();
    sb.append("jdbc:mysql://").append(host).append(":").append(port);
    if (database != null && !database.isEmpty()) {
      sb.append("/").append(database);
    }

    // Add default parameters for MySQL 5.0 compatibility
    sb.append("?zeroDateTimeBehavior=convertToNull");
    sb.append("&useUnicode=true");
    sb.append("&characterEncoding=UTF-8");

    if (useSsl) {
      sb.append("&useSSL=true");
      sb.append("&verifyServerCertificate=false");
    } else {
      sb.append("&useSSL=false");
    }

    if (extraParams != null && !extraParams.isEmpty()) {
      // Ensure params start with ampersand
      if (!extraParams.startsWith("&")) {
        sb.append("&");
      }
      sb.append(extraParams);
    }
    String url = sb.toString();

    return configBuilder
        .withDialect(getDialect())
        .withDatasourceFactory(() -> createDataSource(url))
        .withShowOnlyConnDatabase(false)
        .build();
  }

  /**
   * Creates DataSource using Dremio's DataSources utility (like working connector).
   */
  private CloseableDataSource createDataSource(String url) {
    Properties properties = new Properties();
    return DataSources.newGenericConnectionPoolDataSource(
        DRIVER,
        url,
        username,
        password,
        properties,
        DataSources.CommitMode.DRIVER_SPECIFIED_COMMIT_MODE,
        8,  // maxIdleConns
        60  // idleTimeSec
    );
  }

}
