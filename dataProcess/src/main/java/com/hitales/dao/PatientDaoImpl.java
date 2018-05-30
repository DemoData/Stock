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
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 患者基本信息Dao
 *
 * @author aron
 */
@Slf4j
@Repository("patientDao")
public class PatientDaoImpl extends BaseDao implements IPatientDao {

    private Element table = null;

    private String tableName;
    private String displayCol;
    private String idColumn;

    protected void loadXml() {
        reset();
        //由于是通过jar包启动，需要使用流的形式读取
        InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(super.getXmlPath());
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(resourceStream);
            table = document.getRootElement().element("table");
            tableName = table.attribute("name").getValue();
            idColumn = table.attribute("id-column-names").getValue();
            displayCol = table.attribute("display-column").getValue();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        table = null;
        tableName = null;
        displayCol = null;
        idColumn = null;
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
            List<JSONObject> insertRecords = new ArrayList<>();
            //如果patient已近存在于mongodb中则不再插入
            for (JSONObject record : records) {
                JSONObject patient = hrsMongoTemplate.findOne(Query.query(Criteria.where("_id").is(record.getString("_id"))),
                        JSONObject.class, "Patient");
                if (patient != null) {
                    log.debug("process(): Patient : " + record.getString("_id") + " already exist in DB");
                    continue;
                }
                insertRecords.add(record);
            }
            hrsMongoTemplate.insert(insertRecords, "Patient");
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
        super.getCount(dataSource);
        if (dataSource == null) {
            return 0;
        }
        String sql = null;
        if (dataSource.contains(SqlServerDataSourceConfig.SQL_SERVER)) {
            sql = "select count(t.id) from " + tableName + " t";
        } else {
            sql = "select count(*) from " + tableName;
        }
        return getJdbcTemplate(dataSource).queryForObject(sql, Integer.class);
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select " + displayCol + " from " + tableName + " order by " + idColumn;
        return sql;
    }

    @Override
    protected RowMapper<Patient> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new PatientRowMapper(table));
            return getRowMapper();
        }
        PatientRowMapper rowMapper = (PatientRowMapper) getRowMapper();
        rowMapper.setElement(table);
        return getRowMapper();
    }

    class PatientRowMapper implements RowMapper<Patient> {
        private Element element;

        public PatientRowMapper(Element table) {
            this.element = table;
        }

        public void setElement(Element element) {
            this.element = element;
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
                Object value = rs.getObject(sourceValue);
                if (value == null) {
                    continue;
                }
                if ("patientId".equals(beanName)) {
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
                String type = columnElement.attribute("type") == null ? null : columnElement.attribute("type").getValue();
                if ("birthDay".equals(type)) {
                    //清空之前的值
                    value = "";
                    String inHospitalDateStr = columnElement.attribute("in-hospital-date").getValue();
                    String ageStr = columnElement.attribute("age").getValue();
                    Object inHospitalDate = rs.getObject(inHospitalDateStr) == null ? null : rs.getObject(inHospitalDateStr);
                    String ageValue = rs.getObject(ageStr) == null ? "" : rs.getObject(ageStr).toString();
                    Integer year = null;
                    if (inHospitalDate instanceof Date) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime((Date) inHospitalDate);
                        year = calendar.get(Calendar.YEAR);
                    }
                    if (inHospitalDate instanceof String) {
                        year = Integer.valueOf(((String) inHospitalDate).substring(0, 4));
                    }
                    if (year != null && !StringUtils.isEmpty(ageValue)) {
                        value = year - Integer.valueOf(ageValue);
                    }
                }
                if (StringUtils.isEmpty(value)) {
                    value = columnElement.attribute("default-value") == null ? "" : columnElement.attribute("default-value").getValue();
                }
                dataMap.put(beanName, value);
            }
            Patient patient = BeanUtil.map2Bean(dataMap, Patient.class);
            return patient;
        }

    }
}