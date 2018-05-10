package com.hitales.dao;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.util.BeanUtil;
import com.hitales.dao.standard.IAdviceDao;
import com.hitales.dao.standard.IMedicalHistoryDao;
import com.hitales.entity.MedicalHistory;
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
import java.util.List;
import java.util.Map;

/**
 * 医嘱Dao
 */
@Slf4j
@Repository("medicalContentDao")
public class MedicalContentDaoImpl extends BaseDao implements IMedicalHistoryDao<MedicalHistory> {
    private Element record = null;
    private Element diagnosis;
    private Element conditionElement;
    private String recordTable;

    protected void loadXml() {
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
                } else {
                    conditionElement = query;
                }
            }
            record = descriptor.element("record");
            recordTable = record == null ? "" : record.attribute("name").getValue();

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

        StringBuffer condition = new StringBuffer(" ");
        if (record.attribute("where-column") != null) {
            String whereColumn = record.attribute("where-column").getValue();
            String whereValue = record.attribute("where-value").getValue();
            String whereType = record.attribute("where-type").getValue();
            condition.append("where ").append(whereColumn).append("=");
            if ("int".equals(whereType)) {
                condition.append(whereValue);
            }
            if ("string".equals(whereType)) {
                condition.append("'").append(whereValue).append("'");
            }
        }
        String sql = "select " + columns + " from " + recordTable + condition.toString();
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
    public List<MedicalHistory> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String condition) {
        if (diagnosis == null) {
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
        if (conditionElement == null) {
            return null;
        }
        log.debug("findRequiredColByCondition(): query by " + condition);
        String tableName = conditionElement.attribute("name").getValue();
        String displayCol = conditionElement.attribute("display-column").getValue();
        String conditionCol = conditionElement.attribute("condition-column").getValue();
        String groupCol = conditionElement.attribute("group-column").getValue();
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
        String prefix = conditionElement.attribute("prefix") == null ? "" : conditionElement.attribute("prefix").getValue();
        return prefix + strings.get(0);
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
        String tableName = record.attribute("name").getValue();
        StringBuffer condition = new StringBuffer(" ");
        if (record.attribute("where-column") != null) {
            String whereColumn = record.attribute("where-column").getValue();
            String whereValue = record.attribute("where-value").getValue();
            String whereType = record.attribute("where-type").getValue();
            condition.append("where ").append(whereColumn).append("=");
            if ("int".equals(whereType)) {
                condition.append(whereValue);
            }
            if ("string".equals(whereType)) {
                condition.append("'").append(whereValue).append("'");
            }
        }
        return getJdbcTemplate(dataSource).queryForObject("select count(*) from " + tableName + condition.toString(), Integer.class);
    }

    @Override
    public int batchUpdateContent(String dataSource, List<Object[]> params) {
        return 0;
    }

    class RecordRowMapper extends GenericRowMapper<MedicalHistory> {
        private Element element;

        public RecordRowMapper(Element pElement) {
            this.element = pElement;
        }

        @Override
        public MedicalHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> dataMap = new HashMap<>();
            generateData(rs, element, dataMap);
            return BeanUtil.map2Bean(dataMap, MedicalHistory.class);
        }
    }

}
