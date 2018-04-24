package com.hitales.dao.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IOperationDetailDao;
import com.hitales.dao.standard.IOperationDetailDao;
import com.hitales.entity.OperationDetail;
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
@Repository
public class OperationDetailDaoImpl extends BaseDao implements IOperationDetailDao {

    @Override
    public List<Record> findRecord(String dataSource, int PageNum, int PageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), PageNum, PageSize);
    }

    @Override
    public List<OperationDetail> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findOperationDetailByApplyId(): 查找手术报告通过检验申请号: " + params[0]);
        String sql = "select `一次就诊号` AS 'groupRecordName',`结束时间` AS 'endTime',`手术名称` AS 'name',`手术编号` AS 'serialNumber',`手术部位` AS 'part',`手术等级` AS 'level',`切口类型` AS 'kerfType',`特殊要求` AS 'specialRequest',`开始时间` AS 'startTime',`手术等级描述` AS 'levelDesc',`肿瘤大小` AS 'tumorSize' from `手术明细` where `手术编号`=? ";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<OperationDetail> OperationDetails = jdbcTemplate.query(sql, new BeanPropertyRowMapper(OperationDetail.class), params[0]);
        return OperationDetails;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.`手术编号`) from (select `手术编号` from `手术明细` where `手术部位` is not null and `手术部位` !='' GROUP BY `手术编号`) t ", Integer.class);
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
    protected String generateQuerySql() {
        String sql = "select t.`一次就诊号` AS 'groupRecordName',t.`手术编号` AS 'operationId' from `手术明细` t where t.`手术部位` is not null and t.`手术部位` !='' GROUP BY t.`手术编号` ";
        return sql;
    }

    @Override
    protected RowMapper<Record> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new OperationDetailRowMapper());
        }
        return getRowMapper();
    }

    class OperationDetailRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setGroupRecordName(rs.getString("groupRecordName"));
            record.setId(rs.getString("operationId"));
            record.setSourceId(rs.getString("operationId"));
            return record;
        }
    }
}
