package com.hitales.dao;

import com.hitales.common.config.MongoDataSourceConfig;
import com.hitales.common.config.MysqlDataSourceConfig;
import com.hitales.common.config.SqlServerDataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String tableName;

    protected static Map<String, JdbcTemplate> jdbcTemplatePool = new HashMap<>();

    protected JdbcTemplate getJdbcTemplate(String dataSource) {
        if (jdbcTemplatePool.isEmpty()) {
            initialJdbcPool();
        }
        return jdbcTemplatePool.get(dataSource);
    }

    private void initialJdbcPool() {
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
        log.debug("findOrgOdCatByGroupRecordName(): 查找诊断名称通过一次就诊号: " + groupRecordName);
//        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ? group by t.`诊断名称`";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<String> results = jdbcTemplate.queryForList(sql, String.class, groupRecordName);
        return results;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public MongoTemplate getMongoTemplate() {
        return this.hrsMongoTemplate;
    }

    protected <T> T map2Bean(Map<String, Object> map, Class<T> type) {
        T bean = null;
        try {
            bean = type.newInstance();
            BeanUtils.populate(bean, map);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return bean;
    }
}
