package com.hitales.dao;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.util.BeanUtil;
import com.hitales.dao.standard.IAdviceDao;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 医嘱Dao
 */
@Slf4j
@Repository("adviceDao")
public class AdviceDaoImpl extends BaseDao implements IAdviceDao<Map<String, Object>, Map<String, Object>> {
    private Element record = null;
    private Element adviceReport = null;
    private Element adviceDetail = null;
    private Element diagnosis;
    private Element groupRecordName;
    private String adviceReportTable;
    private String adviceDetailTable;
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
            List<Element> tables = descriptor.elements("table");
            for (Element tableElement : tables) {
                String tableType = tableElement.attribute("type").getValue();
                if ("primary".equals(tableType)) {
                    adviceReport = tableElement;
                } else {
                    adviceDetail = tableElement;
                }
            }
            record = descriptor.element("record");
            adviceReportTable = adviceReport == null ? "" : adviceReport.attribute("name").getValue();
            adviceDetailTable = adviceDetail == null ? "" : adviceDetail.attribute("name").getValue();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String generateQuerySql() {
        if (record == null) {
            throw new RuntimeException("record table can not be null!");
        }
        String columns = record.attribute("display-column").getValue();
        String groupBy = record.attribute("group-column").getValue();
        String tableName = record.attribute("name").getValue();
        String sql = "select " + columns + " from " + tableName + " group by " + groupBy;
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new RecordRowMapper(record));
        }
        return getRowMapper();
    }

    @Override
    public List<Record> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<Map<String, Object>> findBasicArrayByCondition(String dataSource, String applyId) {
        if (adviceReport == null) {
            return null;
        }
        String mainId = adviceReport.attribute("id-column-names").getValue();
        List<Element> elements = adviceReport.elements();
        StringBuffer colNames = new StringBuffer();
        for (Element element : elements) {
            String name = element.attribute("column-name").getValue();
            colNames.append(name).append(",");
        }
        String columns = colNames.substring(0, colNames.length() - 1);
        String sql = "select " + columns + " from " + adviceReportTable + " where " + mainId + "=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Map<String, Object>> assays = jdbcTemplate.query(sql, new ExamRowMapper(adviceReport), applyId);
        return assays;
    }

    @Override
    public List<Map<String, Object>> findArrayListByCondition(String dataSource, String... params) {
        if (adviceDetail == null) {
            return null;
        }
        log.debug("findArrayListByCondition(): 查找化验报告通过: " + params[0]);
        String fid = adviceDetail.attribute("id-column-names").getValue();
        List<Element> elements = adviceDetail.elements();
        StringBuffer colNames = new StringBuffer();
        for (Element element : elements) {
            colNames.append(element.attribute("column-name").getValue()).append(",");
        }
        String columns = colNames.substring(0, colNames.length() - 1);

        String sql = "select " + columns + " from " + adviceDetailTable + " where " + fid + "=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Map<String, Object>> assays = jdbcTemplate.query(sql, new ExamRowMapper(adviceDetail), params[0]);
        return assays;
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String condition) {
        if(diagnosis == null){
            return null;
        }
        String tableName = diagnosis.attribute("name").getValue();
        String displayCol = diagnosis.attribute("display-column").getValue();
        String conditionCol = diagnosis.attribute("condition-column").getValue();
        String groupCol = diagnosis.attribute("group-column").getValue();
        String sql = "select " + displayCol + " from " + tableName + " where " + conditionCol + "= ? group by " + groupCol;
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, condition);
    }

    @Override
    public String findRequiredColByCondition(String dataSource, String condition) {
        if(groupRecordName == null){
            return null;
        }
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
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        if (!loadedXml) {
            loadXml();
        }
        String tableName = record.attribute("name").getValue();
        return getJdbcTemplate(dataSource).queryForObject("select count(*) from " + tableName, Integer.class);
    }

    class RecordRowMapper extends GenericRowMapper<Record> {
        private Element element;

        public RecordRowMapper(Element pElement) {
            this.element = pElement;
        }

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> dataMap = new HashMap<>();
            generateData(rs, element, dataMap);
            return BeanUtil.map2Bean(dataMap, Record.class);
        }
    }

    class ExamRowMapper extends GenericRowMapper<Map<String, Object>> {
        private Element element;

        public ExamRowMapper(Element pElement) {
            this.element = pElement;
        }

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> dataMap = new HashMap<>();
            generateData(rs, element, dataMap);
            return dataMap;
        }
    }

}
