package com.hitales.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class MysqlDataSourceConfig {

    public static final String MYSQL_YXZW_PREFIX = "mysql.yixianzhanwei";
    public static final String MYSQL_YXZW_DATASOURCE = "mysql.yxzwDataSource";
    public static final String MYSQL_YXZW_TEMPLATE = "yxzwJdbcTemplate";

    public static final String MYSQL_JKCT_PREFIX = "mysql.jiankangchati";
    public static final String MYSQL_JKCT_DATASOURCE = "mysql.jkctDataSource";
    public static final String MYSQL_JKCT_TEMPLATE = "jkctJdbcTemplate";

    public static final String MYSQL_TNB_PREFIX = "mysql.tangniaobingxiangguan";
    public static final String MYSQL_TNB_DATASOURCE = "mysql.tnbDataSource";
    public static final String MYSQL_TNB_TEMPLATE = "tnbJdbcTemplate";

    public static final String MYSQL_YX_PREFIX = "mysql.yixianxiangguan";
    public static final String MYSQL_YX_DATASOURCE = "mysql.yxDataSource";
    public static final String MYSQL_YX_TEMPLATE = "yxJdbcTemplate";

    public static final String MYSQL_XZDM_PREFIX = "mysql.xiazhidongmaixiangguan";
    public static final String MYSQL_XZDM_DATASOURCE = "mysql.xzdmDataSource";
    public static final String MYSQL_XZDM_TEMPLATE = "xzdmJdbcTemplate";

    public static final String MYSQL_GA_PREFIX = "mysql.ganai";
    public static final String MYSQL_GA_DATASOURCE = "mysql.gaDataSource";
    public static final String MYSQL_GA_TEMPLATE = "gaJdbcTemplate";

    public static final String MYSQL_RK_PREFIX = "mysql.rk";
    public static final String MYSQL_RK_DATASOURCE = "mysql.rk";
    public static final String MYSQL_RK_TEMPLATE = "rkJdbcTemplate";

    @Primary
    @Bean(name = MYSQL_RK_DATASOURCE)
    @Qualifier(MYSQL_RK_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_RK_PREFIX)
    public DataSource rkDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_YXZW_DATASOURCE)
    @Qualifier(MYSQL_YXZW_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_YXZW_PREFIX)
    public DataSource yxzwDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_JKCT_DATASOURCE)
    @Qualifier(MYSQL_JKCT_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_JKCT_PREFIX)
    public DataSource jkctDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_TNB_DATASOURCE)
    @Qualifier(MYSQL_TNB_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_TNB_PREFIX)
    public DataSource tnbDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_YX_DATASOURCE)
    @Qualifier(MYSQL_YX_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_YX_PREFIX)
    public DataSource yxDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_GA_DATASOURCE)
    @Qualifier(MYSQL_GA_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_GA_PREFIX)
    public DataSource gaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_XZDM_DATASOURCE)
    @Qualifier(MYSQL_XZDM_DATASOURCE)
    @ConfigurationProperties(prefix = MYSQL_XZDM_PREFIX)
    public DataSource xzdmDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = MYSQL_YXZW_TEMPLATE)
    public JdbcTemplate yxzwJdbcTemplate(@Qualifier(MYSQL_YXZW_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = MYSQL_JKCT_TEMPLATE)
    public JdbcTemplate jkctJdbcTemplate(@Qualifier(MYSQL_JKCT_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = MYSQL_TNB_TEMPLATE)
    public JdbcTemplate tnbJdbcTemplate(@Qualifier(MYSQL_TNB_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = MYSQL_YX_TEMPLATE)
    public JdbcTemplate yxJdbcTemplate(@Qualifier(MYSQL_YX_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = MYSQL_XZDM_TEMPLATE)
    public JdbcTemplate xzdmJdbcTemplate(@Qualifier(MYSQL_XZDM_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = MYSQL_GA_TEMPLATE)
    public JdbcTemplate gaJdbcTemplate(@Qualifier(MYSQL_GA_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = MYSQL_RK_TEMPLATE)
    public JdbcTemplate rkJdbcTemplate(@Qualifier(MYSQL_RK_DATASOURCE) DataSource dataSource) {
        log.info(dataSource.toString());
        return new JdbcTemplate(dataSource);
    }
}
