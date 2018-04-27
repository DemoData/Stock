package com.hitales.dao;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.config.SqlServerDataSourceConfig;
import com.hitales.common.util.BeanUtil;
import com.hitales.dao.standard.IPatientDao;
import com.hitales.entity.Patient;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
 * 患者基本信息Dao
 * @author aron
 */
@Slf4j
@Repository("patientDao")
public class PatientDaoImpl extends BaseDao implements IPatientDao {

    private Element table = null;

    private String tableName;
    private String groupCol;
    private String displayCol;
    private boolean loadedXml = false;

    private void loadXml() {
        String path = this.getClass().getClassLoader().getResource(super.getXmlPath()).getPath();
        SAXReader reader = new SAXReader();
        File xml = new File(path);
        try {
            Document document = reader.read(xml);
            table = document.getRootElement().element("table");
            tableName = table.attribute("name").getValue();
            groupCol = table.attribute("group-column").getValue();
            displayCol = table.attribute("display-column").getValue();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Patient> findPatients(String dataSource, int pageNum, int pageSize) {
        log.info(">>>>>>>>>>>Searching patients from : " + dataSource + "<<<<<<<<<<<<<<<");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        if (dataSource.contains(SqlServerDataSourceConfig.SQL_SERVER)) {
            return super.queryForListInSqlServer(jdbcTemplate, pageNum, pageSize, tableName, null, null);
        }
        return super.queryForList(jdbcTemplate, pageNum, pageSize);
    }

    @Override
    public JSONObject findPatientByIdInHRS(String pid) {
        Query patientQuery = new Query();
        patientQuery.addCriteria(Criteria.where("_id").is(pid));
        JSONObject patient = hrsMongoTemplate.findOne(patientQuery, JSONObject.class, "Patient");
        return patient;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, "Patient");
        }
    }

    @Override
    public void save2HRS(JSONObject patient) {
        synchronized (this) {
            hrsMongoTemplate.insert(patient, "Patient");
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        if (!loadedXml) {
            loadXml();
        }
        if (dataSource == null) {
            return 0;
        }
        String sql = null;
        if (dataSource.contains(SqlServerDataSourceConfig.SQL_SERVER)) {
            sql = "select count(t.id) from " + tableName + " t";
        } else {
            sql = "select count(*) from (select " + groupCol + " from " + tableName + " group by " + groupCol + ") t";
        }
        return getJdbcTemplate(dataSource).queryForObject(sql, Integer.class);
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select " + displayCol + " from " + tableName + " group by " + groupCol;
        return sql;
    }

    @Override
    protected RowMapper<Patient> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new PatientRowMapper(table));
        }
        return getRowMapper();
    }

    class PatientRowMapper implements RowMapper<Patient> {
        private Element element;

        public PatientRowMapper(Element table) {
            this.element = table;
        }

        @Override
        public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> dataMap = new HashMap<>();
            Iterator iterator = element.elementIterator();
            while (iterator.hasNext()) {
                Element columnElement = (Element) iterator.next();
                String beanName = columnElement.attribute("bean-name").getValue();
                String sourceValue = columnElement.attribute("column-name").getValue();
                if (beanName == null || sourceValue == null) {
                    continue;
                }
                dataMap.put(beanName, rs.getObject(sourceValue));
            }
            Patient patient = BeanUtil.map2Bean(dataMap, Patient.class);
            return patient;
        }

    }
}