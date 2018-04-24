package com.hitales.dao.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IOperationMainDao;
import com.hitales.entity.OperationMain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class OperationMainDaoImpl extends BaseDao implements IOperationMainDao {

    @Override
    protected String generateQuerySql() {
        String sql = "select * from `手术事件主记录` where 第一台上护士 is not null and 第一台上护士 !=''";
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new OperationMainRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<OperationMain> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ? group by t.`诊断名称`";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);
    }

    @Override
    public String findPatientIdByGroupRecordName(String dataSource, String groupRecordName) {
        log.debug("findPatientIdByGroupRecordName(): 查找PatientId通过一次就诊号: " + groupRecordName);
        String sql = "select t.`病人ID号` from `患者基本信息` t where t.`一次就诊号`= ? group by t.`一次就诊号`";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<String> patientList = jdbcTemplate.queryForList(sql, String.class, groupRecordName);
        if (patientList == null || patientList.isEmpty()) {
            return null;
        }
        return "shch_" + patientList.get(0);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(id) from `手术事件主记录` where 第一台上护士 is not null and 第一台上护士 !=''", Integer.class);
    }

    class OperationMainRowMapper implements RowMapper<OperationMain> {

        @Override
        public OperationMain mapRow(ResultSet rs, int rowNum) throws SQLException {
            OperationMain operationMain = new OperationMain();
            operationMain.setId(rs.getInt("id"));
            putValue2Bean(operationMain, rs);
            return operationMain;
        }

        private void putValue2Bean(Object bean, ResultSet rs) {
            List<Map<String, String>> properties = new ArrayList<>();
            for (OperationMain.ColumnMapping operationEnum : OperationMain.ColumnMapping.values()) {
                Map<String, String> row = new HashMap<>();
                row.put("propName", operationEnum.propName());
                row.put("columnName", operationEnum.columnName());
                properties.add(row);
            }
            for (Map<String, String> property : properties) {
                String propName = property.get("propName");
                String columnName = property.get("columnName");
                try {
                    PropertyDescriptor pd = new PropertyDescriptor(propName, bean.getClass());
                    pd.getWriteMethod().invoke(bean, rs.getObject(columnName));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
