package com.hitales.dao.ch.xgwk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IExamDao;
import com.hitales.entity.Exam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository("xgInspectionFSDao")
public class XGInspectionFSDaoImpl extends BaseDao implements IExamDao<Exam> {

    @Override
    protected String generateQuerySql() {
        String sql = "select * from `放射`";
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new InspectionRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<Exam> findRecord(String dataSource, int pageNum, int pageSize) {
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
        return getJdbcTemplate(dataSource).queryForObject("select count(id) from `放射`", Integer.class);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String column, String groupRecordName) {
        return null;
    }

    class InspectionRowMapper implements RowMapper<Exam> {

        @Override
        public Exam mapRow(ResultSet rs, int rowNum) throws SQLException {
            Exam inspection = new Exam();
            inspection.setId(rs.getInt("id"));
            inspection.setPatientId(rs.getString("病人ID号"));
            inspection.setHospitalId(rs.getString("住院号"));
            inspection.setInHospitalDate(rs.getString("入院日期"));
            inspection.setOutHospitalDate(rs.getString("出院日期"));
            inspection.setInspectionType(rs.getString("检查类别"));
            inspection.setClinicalDiagnosis(rs.getString("临床诊断"));
            inspection.setApplyProjectName(rs.getString("检查项目名称"));
            inspection.setInspectionType(rs.getString("检查方式"));
            inspection.setReportClinical(rs.getString("报告诊断"));
            inspection.setResultDesc(rs.getString("报告结论"));
            inspection.setAbnormalFlag(rs.getString("是否阳性"));
            inspection.setInspectionDate(rs.getString("检查时间"));
            inspection.setReportDate(rs.getString("报告时间"));
            return inspection;
        }
    }

}
