package com.hitales.dao.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IMicroorganismDao;
import com.hitales.entity.Microorganism;
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
public class MicroorganismDaoImpl extends BaseDao implements IMicroorganismDao {

    @Override
    public List<Record> findRecord(String dataSource, int PageNum, int PageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), PageNum, PageSize);
    }

    @Override
    public List<Microorganism> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findMicroorganismByApplyId(): 查找微生物报告通过检验申请号: " + params[0]);
        String sql = "select `一次就诊号` AS 'groupRecordName',`检验方法编码` AS 'validateMethodCode',`检验时间` AS 'checkDate',`检验申请号` AS 'checkApplyNo',`微生物代码` AS 'microorganismCode',`微生物培养结果` AS 'microorganismGrowResult',`检验值` AS 'checkValue',`检验结果` AS 'checkResult',`抗生素名称` AS 'antibioticName',`微生物名称` AS 'microorganismName',`项目名称` AS 'projectName',`备注` AS 'remark' from `微生物报告明细` where `检验申请号`=? ";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Microorganism> Microorganisms = jdbcTemplate.query(sql, new BeanPropertyRowMapper(Microorganism.class), params[0]);
        return Microorganisms;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.`检验申请号`) from (select `检验申请号` from `微生物报告明细` GROUP BY `检验申请号`) t ", Integer.class);
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
        String sql = "select t.`一次就诊号` AS 'groupRecordName',t.`检验申请号` AS 'applyId' from `微生物报告明细` t GROUP BY t.`检验申请号` ";
        return sql;
    }

    @Override
    protected RowMapper<Record> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new MicroorganismRowMapper());
        }
        return getRowMapper();
    }

    class MicroorganismRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setGroupRecordName(rs.getString("groupRecordName"));
            record.setId(rs.getString("applyId"));
            record.setSourceId(rs.getString("applyId"));
            return record;
        }
    }
}
