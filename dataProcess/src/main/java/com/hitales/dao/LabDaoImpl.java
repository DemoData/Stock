package com.hitales.dao;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.util.BeanUtil;
import com.hitales.dao.standard.ILabDao;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.dao.EmptyResultDataAccessException;
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

/**
 * 化验Dao
 */
@Slf4j
@Repository("assayDao")
public class LabDaoImpl extends BaseDao implements ILabDao<Map<String, Object>, Map<String, Object>> {

    private Element table = null;
    private Element labBasic = null;
    private Element labDetail = null;
    private Element diagnosis;
    private Element groupRecordName;

    private String tableName;
    private String groupCol;
    private String displayCol;
    private String labBasicTable;
    private String labDetailTable;
    private boolean loadedXml = false;

    private void loadXml() {
        String path = this.getClass().getClassLoader().getResource(super.getXmlPath()).getPath();
        SAXReader reader = new SAXReader();
        File xml = new File(path);
        try {
            Element rootElement = reader.read(xml).getRootElement();
            Element descriptor = rootElement.element("item-descriptor");
            List<Element> queryList = rootElement.element("queryList").elements();
            for (Element query : queryList) {
                String id = query.attribute("id").getValue();
                if ("odCategories".equals(id)) {
                    diagnosis = query;
                }
                if ("condition".equals(id)) {
                    groupRecordName = query;
                }
            }
            table = descriptor.element("record");
            tableName = table.attribute("name").getValue();
            groupCol = table.attribute("group-column").getValue();
            displayCol = table.attribute("display-column").getValue();

            labBasic = descriptor.element("labBasic");
            labDetail = descriptor.element("labDetail");
            labBasicTable = labBasic.attribute("name").getValue();
            labDetailTable = labDetail.attribute("name").getValue();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        if (!loadedXml) {
            loadXml();
        }
        return getJdbcTemplate(dataSource).queryForObject("select count(*) from (select " + displayCol + " from " + tableName + " GROUP BY " + groupCol + ") t", Integer.class);
    }

    @Override
    public List<Record> findRecord(String dataSource, int PageNum, int PageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), PageNum, PageSize);
    }

    @Override
    public List<Map<String, Object>> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findArrayListByCondition(): 查找化验报告通过: " + params[0]);
        String conditionCol = labDetail.attribute("condition-column").getValue();
        List<Element> elements = labDetail.elements();
        StringBuffer colNames = new StringBuffer();
        for (Element element : elements) {
            colNames.append(element.attribute("column-name").getValue()).append(",");
        }
        String columns = colNames.substring(0, colNames.length() - 1);

        String sql = "select " + columns + " from " + labDetailTable + " where " + conditionCol + "=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Map<String, Object>> assays = jdbcTemplate.query(sql, new LabRowMapper(labDetail), params[0]);
        return assays;
    }

    @Override
    public List<Map<String, Object>> findBasicArrayByCondition(String dataSource, String applyId) {
        String conditionCol = labBasic.attribute("condition-column").getValue();
        List<Element> elements = labBasic.elements();
        StringBuffer colNames = new StringBuffer();
        for (Element element : elements) {
            String name = element.attribute("column-name").getValue();
            colNames.append(name).append(",");
        }
        String columns = colNames.substring(0, colNames.length() - 1);
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
        String displayCol = diagnosis.attribute("display-column").getValue();
        String conditionCol = diagnosis.attribute("condition-column").getValue();
        String groupCol = diagnosis.attribute("group-column").getValue();
        String sql = "select " + displayCol + " from " + tableName + " where " + conditionCol + "= ? group by " + groupCol;
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, condition);
    }

    @Override
    public String findRequiredColByCondition(String dataSource, String condition) {
        log.debug("findRequiredColByCondition(): query by " + condition);
        String tableName = groupRecordName.attribute("name").getValue();
        String displayCol = groupRecordName.attribute("display-column").getValue();
        String conditionCol = groupRecordName.attribute("condition-column").getValue();
        String groupCol = groupRecordName.attribute("group-column").getValue();
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
        String sql = "select " + displayCol + " from " + tableName + " GROUP BY " + groupCol;
        return sql;
    }

    @Override
    protected RowMapper<Record> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new AssayRowMapper(table));
        }
        return getRowMapper();
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
                String beanName = columnElement.attribute("bean-name").getValue();
                String columnName = columnElement.attribute("column-name").getValue();
                String type = columnElement.attribute("data-type").getValue();
                Object mapValue = "";
                if ("string".equals(type)) {
                    mapValue = rs.getObject(columnName) == null ? "" : rs.getObject(columnName).toString();
                } else if ("map".equals(type)) {
                    String[] split = columnName.split(",");
                    Map<String, Object> condition = new HashMap<>();
                    for (String sourceCol : split) {
                        condition.put(sourceCol, rs.getObject(sourceCol));
                    }
                    mapValue = condition;
                }

                if (beanName == null || columnName == null) {
                    continue;
                }
                if ("patientId".equals(beanName)) {
                    StringBuffer patientPrefix = new StringBuffer(columnElement.attribute("patient-prefix").getValue());
                    mapValue = patientPrefix.append(mapValue.toString()).toString();
                }
                //处理字段值映射
                List<Element> options = columnElement.elements("option");
                if (options != null && !options.isEmpty()) {
                    for (Element option : options) {
                        String optionValue = option.attribute("value").getValue();
                        if (optionValue != null && optionValue.equals(mapValue.toString())) {
                            mapValue = option.getText();
                            break;
                        }
                    }
                }
                dataMap.put(beanName, mapValue == null ? "" : mapValue);
            }
            return BeanUtil.map2Bean(dataMap, Record.class);
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
                String colName = columnElement.attribute("column-name").getValue();
                String displayName = columnElement.attribute("display-name").getValue();

                if (colName == null || displayName == null) {
                    continue;
                }
                Object value = rs.getObject(colName) == null ? "" : rs.getObject(colName);
                //处理字段值映射
                List<Element> options = columnElement.elements("option");
                if (options != null || !options.isEmpty()) {
                    for (Element option : options) {
                        String optionValue = option.attribute("value").getValue();
                        if (optionValue != null && optionValue.equals(value.toString())) {
                            value = option.getText();
                            break;
                        }
                    }
                }
                dataMap.put(displayName, value == null ? "" : value);
            }
            return dataMap;
        }
    }
}