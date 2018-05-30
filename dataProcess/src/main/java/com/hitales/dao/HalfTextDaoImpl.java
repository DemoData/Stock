package com.hitales.dao;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.IExamDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 检查Dao
 */
@Slf4j
@Repository("textDao")
public class HalfTextDaoImpl extends BaseDao implements IExamDao<Map<String, Object>> {
    private Element examReport = null;
    private Element examDetail = null;
    private Element diagnosis;
    private Element groupRecordName;
    private String examReportTable;
    private String examDetailTable;

    protected void loadXml() {
        reset();
        InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(super.getXmlPath());
        SAXReader reader = new SAXReader();
        try {
            Element rootElement = reader.read(resourceStream).getRootElement();
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
            List<Element> tables = descriptor.elements("table");
            for (Element tableElement : tables) {
                String tableType = tableElement.attribute("type").getValue();
                if ("primary".equals(tableType)) {
                    examReport = tableElement;
                } else {
                    examDetail = tableElement;
                }
            }
            examReportTable = examReport == null ? "" : examReport.attribute("name").getValue();
            examDetailTable = examDetail == null ? "" : examDetail.attribute("name").getValue();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        examReport = null;
        examDetail = null;
        diagnosis = null;
        groupRecordName = null;
        examReportTable = null;
        examDetailTable = null;
    }

    @Override
    protected String generateQuerySql() {
        if (examReport == null) {
            return null;
        }
        List<Element> elements = examReport.elements();
        StringBuffer colNames = new StringBuffer();
        for (Element element : elements) {
            if (element.attribute("ignore-column") != null) {
                continue;
            }
            colNames.append("t1.").append(element.attribute("column-name").getValue()).append(",");
        }
        String tableName = examReportTable + " t1 ";
        if (examDetail != null) {
            String examReportId = examReport.attribute("id-column-names").getValue();
            List<Element> detailElements = examDetail.elements();
            for (Element element : detailElements) {
                colNames.append("t2.").append(element.attribute("column-name").getValue()).append(",");
            }
            String examDetailId = examDetail.attribute("id-column-names").getValue();
            tableName = examReportTable + " t1," + examDetailTable + " t2 where t1." + examReportId + "=t2." + examDetailId;
        }
        String columns = colNames.substring(0, colNames.length() - 1);

        String sql = "select " + columns + " from " + tableName;
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new ExamRowMapper(examReport, examDetail));
            return getRowMapper();
        }
        ExamRowMapper examRowMapper = (ExamRowMapper) getRowMapper();
        examRowMapper.setElementMain(examReport);
        examRowMapper.setElementDetail(examDetail);
        return getRowMapper();
    }

    @Override
    public List<Map<String, Object>> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String conditionCol, String condition) {
        if (StringUtils.isBlank(condition)) {
            return null;
        }
        String tableName = diagnosis.attribute("name").getValue();
        String displayCol = diagnosis.attribute("display-column").getValue();
        String conditionColumn = conditionCol;
        if (StringUtils.isEmpty(conditionColumn)) {
            conditionColumn = diagnosis.attribute("condition-column").getValue();
        }
        String groupCol = diagnosis.attribute("group-column").getValue();
        StringBuffer sql = new StringBuffer("select " + displayCol + " from " + tableName);
        if (StringUtils.isNotBlank(conditionColumn)) {
            sql.append(" where " + conditionColumn + "= ? ");
        }
        if (StringUtils.isNotBlank(groupCol)) {
            sql.append(" group by " + groupCol);
        }
        return super.findOrgOdCatByGroupRecordName(sql.toString(), dataSource, condition);
    }

    @Override
    public String findRequiredColByCondition(String dataSource, String condition) {
        if (StringUtils.isBlank(condition)) {
            return null;
        }
        log.debug("findRequiredColByCondition(): query by " + condition);
        String tableName = groupRecordName.attribute("name").getValue();
        String displayCol = groupRecordName.attribute("display-column").getValue();
        String conditionCol = groupRecordName.attribute("condition-column").getValue();
        String groupCol = groupRecordName.attribute("group-column").getValue();
        StringBuffer sql = new StringBuffer("select " + displayCol + " from " + tableName);
        if (StringUtils.isNotBlank(conditionCol)) {
            sql.append(" where " + conditionCol + "= ? ");
        }
        if (StringUtils.isNotBlank(groupCol)) {
            sql.append(" group by " + groupCol);
        }
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<String> strings = null;
        try {
            strings = jdbcTemplate.queryForList(sql.toString(), String.class, condition);
            if (strings == null || strings.isEmpty()) {
                return null;
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("Can not found patientId via condition:" + condition);
            e.printStackTrace();
            return null;
        }
        if (displayCol.toLowerCase().contains("patient")) {
            return groupRecordName.attribute("prefix").getValue() + strings.get(0);
        }
        return strings.get(0);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        super.getCount(dataSource);
        String tableName = examReportTable;
        if (examDetail != null) {
            String examReportId = examReport.attribute("id-column-names").getValue();
            String examDetailId = examDetail.attribute("id-column-names").getValue();
            tableName = examReportTable + " t1," + examDetailTable + " t2 where t1." + examReportId + "=t2." + examDetailId;
        }
        return getJdbcTemplate(dataSource).queryForObject("select count(*) from " + tableName, Integer.class);
    }

    class ExamRowMapper implements RowMapper<Map<String, Object>> {
        private Element elementMain;
        private Element elementDetail;

        public ExamRowMapper(Element elementMain, Element elementDetail) {
            this.elementMain = elementMain;
            this.elementDetail = elementDetail;
        }

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> dataMap = new HashMap<>();
            if (elementMain != null) {
                generateData(rs, elementMain, dataMap);
            }
            if (elementDetail != null) {
                generateData(rs, elementDetail, dataMap);
            }
            return dataMap;
        }

        private void generateData(ResultSet rs, Element element, Map<String, Object> dataMap) throws SQLException {
            Iterator iterator = element.elementIterator();
            while (iterator.hasNext()) {
                Element columnElement = (Element) iterator.next();
                String colName = columnElement.attribute("column-name").getValue();
                String displayName = columnElement.attribute("display-name").getValue();

                if (colName == null || displayName == null) {
                    continue;
                }
                Object value = rs.getObject(colName) == null ? "" : rs.getObject(colName);
                if (displayName.contains("patientId")) {
                    StringBuffer patientPrefix = new StringBuffer(columnElement.attribute("patient-prefix").getValue());
                    value = patientPrefix.append(value.toString()).toString();
                }
                //处理字段值映射
                List<Element> options = columnElement.elements("option");
                if (options != null && !options.isEmpty()) {
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
        }

        public void setElementMain(Element elementMain) {
            this.elementMain = elementMain;
        }

        public void setElementDetail(Element elementDetail) {
            this.elementDetail = elementDetail;
        }
    }

}
