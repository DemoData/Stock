package com.hitales.dao.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IMedicalOrderDao;
import com.hitales.entity.MedicalOrder;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository("medicalOrderDao")
public class MedicalOrderDaoImpl extends BaseDao implements IMedicalOrderDao {

    @Override
    protected String generateQuerySql() {
        String sql = "select `一次就诊号` from `住院药品医嘱` GROUP BY `一次就诊号`";
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new MedicalOrderRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<Record> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.`一次就诊号`) from (select `一次就诊号` from `住院药品医嘱` GROUP BY `一次就诊号`) t", Integer.class);
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
    public List<MedicalOrder> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findArrayList(): params: " + params[0]);
//        String sql = "select `类型` AS 'type' , `长/临` AS 'timeType',`内容` AS 'content',`剂量` AS 'dosage',`单位` AS 'unit',`途径` AS 'approach',`频次` AS 'frequency',`开始时间` AS 'startDate',`停止时间` AS 'endDate' from 医嘱 where 病人ID号 =?";
        String sql = "select `医嘱结束时间` AS 'medicalEndDate' , `住院药品名称` AS 'medicalName',`医嘱开始时间` AS 'medicalStartDate',`录入时间` AS 'medicalRecordDate' from 住院药品医嘱 where 一次就诊号 =?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(MedicalOrder.class), params[0]);
    }

    class MedicalOrderRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setSourceId(rs.getString("一次就诊号"));
            record.setGroupRecordName(rs.getString("一次就诊号"));
            return record;
        }
    }

}
