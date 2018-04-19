package com.hitales.dao;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.config.SqlServerDataSourceConfig;
import com.hitales.dao.standard.IPatientDao;
import com.hitales.entity.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author aron
 */
@Slf4j
@Repository("patientDao")
public class PatientDaoImpl extends BaseDao implements IPatientDao {

    @Override
    public List<Patient> findPatients(String dataSource, int pageNum, int pageSize) {
        log.info(">>>>>>>>>>>Searching patients from : " + dataSource + "<<<<<<<<<<<<<<<");
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        if (dataSource.contains(SqlServerDataSourceConfig.SQL_SERVER)) {
            return super.queryForListInSqlServer(jdbcTemplate, pageNum, pageSize, "Patient", null, null);
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
        if (dataSource == null) {
            return 0;
        }
        String sql = null;
        if (dataSource.contains(SqlServerDataSourceConfig.SQL_SERVER)) {
            sql = "select count(t.id) from shtr_patient_20180402 t";
        } else {
            sql = "select count(t.`病人ID号`) from (select `病人ID号` from `患者基本信息` group by `病人ID号`) t";
        }
        return getJdbcTemplate(dataSource).queryForObject(sql, Integer.class);
    }

    @Override
    protected String generateQuerySql() {
//        String sql = "select t.id AS 'id',CONCAT('shch_', t.`病人ID号`) AS 'patientId',t.`性别` AS 'sex',t.`就诊年龄` AS 'age',t.`就诊日期` AS 'clinicDate',CONCAT('',(LEFT(t.`就诊日期`,4) - t.`就诊年龄`)) AS 'birthDay' from `患者基本信息` t group by t.`病人ID号`";
        String sql = "select id,性别 AS 'sex',就诊年龄 AS 'age',出院日期 AS 'outHospitalDate',病人ID号 AS 'patientId' from `患者基本信息` group by `病人ID号`";
        return sql;
    }

    @Override
    protected RowMapper<Patient> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new PatientRowMapper());
        }
        return getRowMapper();
    }

    class PatientRowMapper implements RowMapper<Patient> {

        @Override
        public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
            Patient patient = new Patient();
            /*patient.setPatientId("shch_" + rs.getString("病人ID号"));
            patient.setName(rs.getString("姓名"));*/

            patient.setId(rs.getInt("id"));
            String age = rs.getString("age");
            if (age != null) {
                age = age.substring(0, age.indexOf("."));
                patient.setAge(age);
            }

            if (rs.getString("outHospitalDate") != null) {
                String outHospitalYear = rs.getString("outHospitalDate").substring(0, 4);
                Integer birthYear = Integer.valueOf(outHospitalYear) - Integer.valueOf(age);
                patient.setBirthDay(birthYear.toString());
            }
//            patient.setName(rs.getString("name"));
            patient.setPatientId(rs.getString("patientId"));

//            Object birthYear = rs.getObject("birthday");
//            patient.setBirthDay(birthYear == null ? "" : birthYear.toString().substring(0, 8));

//            patient.setAddress(rs.getString("Address"));
            patient.setSex(rs.getString("sex"));

            return patient;
        }

    }
}