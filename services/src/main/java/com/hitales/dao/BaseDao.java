package com.hitales.dao;

import com.hitales.common.config.MongoDataSourceConfig;
import com.hitales.common.config.MysqlDataSourceConfig;
import com.hitales.common.config.SqlServerDataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO基类
 *
 * @author aron
 */
@Slf4j
@PropertySource("classpath:config/dao.properties")
public abstract class BaseDao extends GenericDao {

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_YXZW_TEMPLATE)
    protected JdbcTemplate yxzwJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_JKCT_TEMPLATE)
    protected JdbcTemplate jkctJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_TNB_TEMPLATE)
    protected JdbcTemplate tnbJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_YX_TEMPLATE)
    protected JdbcTemplate yxJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_XZDM_TEMPLATE)
    protected JdbcTemplate xzdmJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_GA_TEMPLATE)
    protected JdbcTemplate gaJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_ZL_TEMPLATE)
    protected JdbcTemplate zlJdbcTemplate;

    @Autowired
    @Qualifier(SqlServerDataSourceConfig.SQL_SERVER_TEMPLATE)
    protected JdbcTemplate sqlJdbcTemplate;

    @Autowired
    @Qualifier(SqlServerDataSourceConfig.SQL_SERVER_FS_TEMPLATE)
    protected JdbcTemplate fsJdbcTemplate;

    @Autowired
    @Qualifier(SqlServerDataSourceConfig.SQL_SERVER_FS_MZ_TEMPLATE)
    protected JdbcTemplate fsmzJdbcTemplate;

    @Autowired
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    protected MongoTemplate hrsMongoTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_RK_TEMPLATE)
    protected JdbcTemplate rkJdbcTemplate;

    private String xmlPath;

    protected static Map<String, JdbcTemplate> jdbcTemplatePool = new HashMap<>();

    public JdbcTemplate getJdbcTemplate(String dataSource) {
        if (jdbcTemplatePool.isEmpty()) {
            initialJdbcPool();
        }
        return jdbcTemplatePool.get(dataSource);
    }

    /**
     * 这是由于初期存在过多数据源，为了方便动态切换获取，把多个数据源放入一个pool中
     */
    private void initialJdbcPool() {
        //TODO:入库已规范为一个数据库,这里待优化
        jdbcTemplatePool.put(MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE, jkctJdbcTemplate);
        jdbcTemplatePool.put(MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE, yxzwJdbcTemplate);
        jdbcTemplatePool.put(MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE, tnbJdbcTemplate);
        jdbcTemplatePool.put(MysqlDataSourceConfig.MYSQL_YX_DATASOURCE, yxJdbcTemplate);
        jdbcTemplatePool.put(MysqlDataSourceConfig.MYSQL_XZDM_DATASOURCE, xzdmJdbcTemplate);
        jdbcTemplatePool.put(MysqlDataSourceConfig.MYSQL_RK_DATASOURCE, rkJdbcTemplate);
        jdbcTemplatePool.put(MysqlDataSourceConfig.MYSQL_GA_DATASOURCE, gaJdbcTemplate);
        jdbcTemplatePool.put(MysqlDataSourceConfig.MYSQL_ZL_DATASOURCE, zlJdbcTemplate);
        jdbcTemplatePool.put(SqlServerDataSourceConfig.SQL_SERVER_DATASOURCE, sqlJdbcTemplate);
        jdbcTemplatePool.put(SqlServerDataSourceConfig.SQL_SERVER_FS_DATASOURCE, fsJdbcTemplate);
        jdbcTemplatePool.put(SqlServerDataSourceConfig.SQL_SERVER_FS_MZ_DATASOURCE, fsmzJdbcTemplate);
    }

    protected List<String> findOrgOdCatByGroupRecordName(String sql, String dataSource, String groupRecordName) {
        log.debug("findOrgOdCatByGroupRecordName(): " + groupRecordName);
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<String> results = jdbcTemplate.queryForList(sql, String.class, groupRecordName);
        if (results == null || results.isEmpty()) {
            return null;
        }
        List<String> validResult = new ArrayList<>();
        for (String orgOd : results) {
            if (StringUtils.isEmpty(orgOd)) {
                continue;
            }
            validResult.add(orgOd);
        }
        return validResult;
    }

    //兼容以前的老代码所以不设计为抽象方法，用于子类实现
    protected void loadXml() {

    }

    public Integer getCount(String dataSource) {
        this.loadXml();
        return null;
    }

    @Override
    public MongoTemplate getMongoTemplate() {
        return this.hrsMongoTemplate;
    }

    public void initXmlPath(String path) {
        this.xmlPath = path;
    }

    /**
     * 这三个方法用于兼容以前的代码
     *
     * @param dataSource
     * @param params
     * @return
     */
    public List findArrayListByCondition(String dataSource, String... params) {
        return null;
    }

    public List findBasicArrayByCondition(String dataSource, String applyId) {
        return null;
    }

    public String findRequiredColByCondition(String dataSource, String condition) {
        return null;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }
}
