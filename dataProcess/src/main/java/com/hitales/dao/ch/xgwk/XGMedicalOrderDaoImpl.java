package com.hitales.dao.ch.xgwk;

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
@Repository("xgMedicalOrderDao")
public class XGMedicalOrderDaoImpl extends BaseDao implements IMedicalOrderDao<MedicalOrder,MedicalOrder> {

    @Override
    protected String generateQuerySql() {
        String sql = "select id,`病人ID号`,`住院号` from `医嘱` GROUP BY `病人ID号`";
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
            super.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.`病人ID号`) from (select `病人ID号` from `医嘱` GROUP BY `病人ID号`) t", Integer.class);
    }

    @Override
    public List<MedicalOrder> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findArrayList(): params: " + params[0]);
        String sql = "select `类型` AS 'type' , `长/临` AS 'timeType',`内容` AS 'content',`剂量` AS 'dosage',`单位` AS 'unit',`途径` AS 'approach',`频次` AS 'frequency',`开始时间` AS 'startDate',`停止时间` AS 'endDate' from 医嘱 where 病人ID号 =?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(MedicalOrder.class), params[0]);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        return null;
    }

    @Override
    public String findRequiredColByCondition(String dataSource, String applyId) {
        return null;
    }

    class MedicalOrderRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setPatientId(rs.getString("病人ID号"));
            record.setSourceId(rs.getString("id"));
            record.setGroupRecordName(rs.getString("住院号"));
            return record;
        }
    }

}
