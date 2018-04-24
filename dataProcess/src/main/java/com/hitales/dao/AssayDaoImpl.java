package com.hitales.dao;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.constant.CommonConstant;
import com.hitales.dao.standard.INewAssayDao;
import com.hitales.entity.LabDetail;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository("assayDao")
public class AssayDaoImpl extends BaseDao implements INewAssayDao {

    private Element table = null;
    private Element labBasic = null;
    private Element labDetail = null;
    private Element diagnosis;
    private Element patient;

    private String groupCol;
    private String displayCol;
    private String labBasicTable;
    private String labDetailTable;
    private boolean isMultiple;

    {
        //TODO:这个路劲可以优化到baseService中去，然后通过basicInfo传入路径过来，再把得到的path传递给dao
        String path = this.getClass().getClassLoader().getResource("config/shly/Lab.xml").getPath();
        SAXReader reader = new SAXReader();
        File xml = new File(path);
        try {
            Element rootElement = reader.read(xml).getRootElement();
            diagnosis = rootElement.element("diagnosis");
            patient = rootElement.element("patient");

            table = rootElement.element("table");
            String tableName = table.attribute("name").getValue();
            groupCol = table.attribute("groupCol").getValue();
            displayCol = table.attribute("displayCol").getValue();
            super.setTableName(tableName);

            labBasic = rootElement.element("labBasic");
            labDetail = rootElement.element("labDetail");
            labBasicTable = labBasic.attribute("name").getValue();
            labDetailTable = labDetail.attribute("name").getValue();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(*) from (select " + displayCol + " from " + getTableName() + " GROUP BY " + groupCol + ") t", Integer.class);
    }

    @Override
    public List<Record> findRecord(String dataSource, int PageNum, int PageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), PageNum, PageSize);
    }

    @Override
    public List<Map<String, Object>> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findArrayListByCondition(): 查找化验报告通过: " + params[0]);
        String conditionCol = labDetail.attribute("conditionCol").getValue();
        List<Element> elements = labDetail.elements();
        StringBuffer displayNames = new StringBuffer();
        for (Element element : elements) {
            displayNames.append(element.attribute("displayName").getValue()).append(",");
        }
        String columns = displayNames.substring(0, displayNames.length() - 1);

        String sql = "select " + columns + " from " + labDetailTable + " where " + conditionCol + "=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Map<String, Object>> assays = jdbcTemplate.query(sql, new LabRowMapper(labDetail), params[0]);
        return assays;
    }

    @Override
    public List<Map<String, Object>> findBasicArrayByCondition(String dataSource, String applyId) {
        boolean multiple = Boolean.valueOf(labBasic.attribute("multiple").getValue());
        this.isMultiple = multiple;
        String conditionCol = labBasic.attribute("conditionCol").getValue();
        List<Element> elements = labBasic.elements();
        StringBuffer displayNames = new StringBuffer();
        for (Element element : elements) {
            displayNames.append(element.attribute("displayName").getValue()).append(",");
        }
        String columns = displayNames.substring(0, displayNames.length() - 1);
        String sql = "select " + columns + " from " + labBasicTable + " where " + conditionCol + "=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Map<String, Object>> assays = jdbcTemplate.query(sql, new LabRowMapper(labBasic), applyId);
        return assays;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String condition) {
        String tableName = diagnosis.attribute("name").getValue();
        String displayCol = diagnosis.attribute("displayCol").getValue();
        String conditionCol = diagnosis.attribute("conditionCol").getValue();
        String groupCol = diagnosis.attribute("groupCol").getValue();
        String sql = "select " + displayCol + " from " + tableName + " where " + conditionCol + "= ? group by " + groupCol;
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, condition);
    }

    @Override
    public String findRequiredColByCondition(String dataSource, String condition) {
        log.debug("findPatientIdByGroupRecordName(): 查找PatientId by " + condition);
        String tableName = patient.attribute("name").getValue();
        String displayCol = patient.attribute("displayCol").getValue();
        String conditionCol = patient.attribute("conditionCol").getValue();
        String groupCol = patient.attribute("groupCol").getValue();
        String sql = "select " + displayCol + " from " + tableName + " where " + conditionCol + "= ? group by " + groupCol;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<String> strings = null;
        try {
            strings = jdbcTemplate.queryForList(sql, String.class, condition);
            if (strings == null || strings.isEmpty()) {
                return null;
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("Can not found patientId via condition:" + condition);
            e.printStackTrace();
            return null;
        }
        return strings.get(0);
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select " + displayCol + " from " + getTableName() + " GROUP BY " + groupCol;
        return sql;
    }

    @Override
    protected RowMapper<Record> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new AssayRowMapper(table));
        }
        return getRowMapper();
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    class AssayRowMapper implements RowMapper<Record> {

        private Element element;

        public AssayRowMapper(Element table) {
            this.element = table;
        }

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {

            Map<String, Object> dataMap = new HashMap<>();
            Iterator iterator = element.elementIterator();
            while (iterator.hasNext()) {
                Element columnElement = (Element) iterator.next();
                String beanName = columnElement.attribute("name").getValue();
                String sourceValue = columnElement.attribute("sourceName").getValue();
                if (beanName == null || sourceValue == null) {
                    continue;
                }
                dataMap.put(beanName, rs.getObject(sourceValue) == null ? "" : rs.getObject(sourceValue));
            }
            return map2Bean(dataMap, Record.class);
        }
    }

    class LabRowMapper implements RowMapper<Map<String, Object>> {

        private Element element;

        public LabRowMapper(Element table) {
            this.element = table;
        }

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {

            Map<String, Object> dataMap = new HashMap<>();
            Iterator iterator = element.elementIterator();
            while (iterator.hasNext()) {
                Element columnElement = (Element) iterator.next();
                String colName = columnElement.attribute("name").getValue();
                String displayName = columnElement.attribute("displayName").getValue();

                if (colName == null || displayName == null) {
                    continue;
                }
                dataMap.put(displayName, rs.getObject(colName) == null ? "" : rs.getObject(colName));
            }
            return dataMap;
        }
    }
}