package com.hitales.dao.ch.xgwk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.ILabDao;
import com.hitales.entity.LabDetail;
import com.hitales.entity.LabBasic;
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
@Repository("xgAssayDao")
public class XGAssayDaoImpl extends BaseDao implements ILabDao<LabBasic,LabDetail> {

    @Override
    protected String generateQuerySql() {
        String sql = "select id,`病人ID号`,`住院号`,`报告日期` from `检验结果` GROUP BY `住院号`,`报告日期`";
        return sql;
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
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ? group by t.`诊断名称`";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);
    }

    @Override
    public String findRequiredColByCondition(String dataSource, String applyId) {
        return null;
    }

    @Override
    public List<LabBasic> findBasicArrayByCondition(String dataSource, String applyId) {
        return null;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.`住院号`) from (select `住院号`,`报告日期` from `检验结果` GROUP BY `住院号`,`报告日期`) t", Integer.class);
    }

    @Override
    public List<LabDetail> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findArrayListByCondition(): condition: " + params.toString());
        if (params.length == 0) {
            return null;
        }
        String sql = "select `标本` AS 'assaySpecimen' , `检验项目` AS 'assayName',`结果` AS 'assayResult',`单位` AS 'assayUnit',`异常值` AS 'resultFlag',`报告日期` AS 'assayTime' from `检验结果` where `住院号` =? and `报告日期`=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(LabDetail.class), params[0], params[1]);
    }

    class AssayRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setPatientId(rs.getString("病人ID号"));
            record.setSourceId(rs.getString("id"));
            record.setGroupRecordName(rs.getString("住院号"));
            record.setReportDate(rs.getString("报告日期"));
            return record;
        }
    }

}
