package com.hitales.dao.bdsz.zl;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.constant.CommonConstant;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IAssayDao;
import com.hitales.entity.LabDetail;
import com.hitales.entity.LabBasic;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository("bdzlAssayDao")
public class BDZLAssayDaoImpl extends BaseDao implements IAssayDao {

    @Override
    protected String generateQuerySql() {
        return null;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new AssayRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<Record> findRecord(String dataSource, int pageNum, int pageSize) {
//        return super.queryForListInSqlServer(getJdbcTemplate(dataSource), pageNum, pageSize, "TM_LAB_ROUTINE_RESULT", "GROUPRECORDNAME", "group by GROUPRECORDNAME");
        //TODO: sqlserver 分组无法通过id分页问题
        return getJdbcTemplate(dataSource).query("select PID,RID,GROUPRECORDNAME from TM_LAB_ROUTINE_RESULT group by PID,RID,GROUPRECORDNAME", this.generateRowMapper());
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        String sql = "select t.Diagnosis from Diagnosis t where t.groupRecordName= ? group by t.Diagnosis";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        //北大深圳的化验不分页
        return 1000;
//        return getJdbcTemplate(dataSource).queryForObject("select count(t.GROUPRECORDNAME) from (select GROUPRECORDNAME from TM_LAB_ROUTINE_RESULT group by GROUPRECORDNAME) t", Integer.class);
    }

    @Override
    public List<LabDetail> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findArrayListByCondition(): params: " + params[0]);
        String sql = "select PID AS 'patientId', ITEM_CH_NAME AS 'assayName', ITEM_TIME AS 'assayTime',ITEM_RESULT_DES_CODE AS 'resultFlag',ITEM_RESULT_DES_NAME AS 'assayResult',ITEM_RESULT_NUM AS 'assayValue',ITEM_RESULT_UNIT AS 'assayUnit',RESULT_REFERENCE AS 'referenceRange' from TM_LAB_ROUTINE_RESULT where RID =?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(LabDetail.class), params[0]);
    }

    @Override
    public JSONObject findRecordByIdInHRS(String applyId) {
        return null;
    }

    @Override
    public String findPatientIdByGroupRecordName(String dataSource, String applyId) {
        log.debug("findPatientIdByGroupRecordName(): 查找PatientId通过一次就诊号: " + applyId);
        String sql = "select PID from Record where groupRecordName=? group by groupRecordName,PID";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        Map<String, Object> result = null;
        try {
            result = jdbcTemplate.queryForMap(sql, applyId);
        } catch (EmptyResultDataAccessException e) {
            log.info("findPatientIdByGroupRecordName(): can not found PatientId by GroupRecordName");
            return null;
        }
        return result.get("PID").toString();
    }

    @Override
    public List<LabBasic> findBasicArrayByCondition(String dataSource, String applyId) {
        return null;
    }

    class AssayRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            Object pid = rs.getObject("PID");
            if (pid == null) {
                pid = CommonConstant.EMPTY_FLAG;
            }
            if (!(pid instanceof String) && pid.toString().indexOf(".") > 0 && pid.toString().indexOf("E") > 0) {
                pid = new BigDecimal(pid.toString()).toPlainString();
            } else if (pid.toString().indexOf(".") > 0) {
                pid = pid.toString().substring(0, pid.toString().indexOf("."));
            } else {
                pid = pid.toString();
            }

            Object groupRecordName = rs.getObject("GROUPRECORDNAME");
            if (groupRecordName == null) {
                groupRecordName = CommonConstant.EMPTY_FLAG;
            }
            if (!(groupRecordName instanceof String) && groupRecordName.toString().indexOf(".") > 0 && groupRecordName.toString().indexOf("E") > 0) {
                groupRecordName = new BigDecimal(groupRecordName.toString()).toPlainString();
            } else if (groupRecordName.toString().indexOf(".") > 0) {
                groupRecordName = groupRecordName.toString().substring(0, groupRecordName.toString().indexOf("."));
            } else {
                groupRecordName = groupRecordName.toString();
            }

            record.setPatientId(pid.toString());
            record.setSourceId(groupRecordName.toString());
            record.setGroupRecordName(groupRecordName.toString());
            record.setId(rs.getString("RID"));
            return record;
        }
    }

}
