package com.hitales.dao.ch.jyk;

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
@Repository("jyInspectionDao")
public class InspectionDaoImpl extends BaseDao implements IExamDao<Exam> {

    @Override
    protected String generateQuerySql() {
        String sql = "select * from `检查报告`";
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
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String column, String groupRecordName) {
        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ? group by t.`诊断名称`";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(id) from `检查报告`", Integer.class);
    }

    class InspectionRowMapper implements RowMapper<Exam> {

        @Override
        public Exam mapRow(ResultSet rs, int rowNum) throws SQLException {
            Exam inspection = new Exam();
            inspection.setId(rs.getInt("id"));
            inspection.setGroupRecordName(rs.getString(Exam.ColumnMapping.GROUP_RECORD_NAME.columnName()));
            inspection.setPatientId(rs.getString(Exam.ColumnMapping.PATIENT_ID.columnName()));
            inspection.setAbnormalFlag(rs.getString(Exam.ColumnMapping.ABNORMAL_FLAG.columnName()));

            inspection.setResultContent(rs.getString(Exam.ColumnMapping.RESULT_CONTENT.columnName()));
            inspection.setResultDesc(rs.getString(Exam.ColumnMapping.RESULT_DESC.columnName()));
            inspection.setInspectionState(rs.getString(Exam.ColumnMapping.INSPECTION_STATE.columnName()));
            inspection.setReportId(rs.getString(Exam.ColumnMapping.REPORT_ID.columnName()));
            inspection.setReportFixDate(rs.getString(Exam.ColumnMapping.REPORT_FIX_DATE.columnName()));
            inspection.setTypeName(rs.getString(Exam.ColumnMapping.TYPE_NAME.columnName()));
            inspection.setApplyDate(rs.getString(Exam.ColumnMapping.APPLY_DATE.columnName()));
            inspection.setBirthday(rs.getString(Exam.ColumnMapping.BIRTHDAY.columnName()));
            inspection.setDoctorName(rs.getString(Exam.ColumnMapping.DOCTOR_NAME.columnName()));
            inspection.setHospitalFlag(rs.getString(Exam.ColumnMapping.HOSPITAL_FLAG.columnName()));
            inspection.setAuditor(rs.getString(Exam.ColumnMapping.AUDITOR.columnName()));
            inspection.setObserveReason(rs.getString(Exam.ColumnMapping.OBSERVE_REASON.columnName()));
            inspection.setInspectionMethod(rs.getString(Exam.ColumnMapping.INSPECTION_METHOD.columnName()));
            inspection.setDiagnosis(rs.getString(Exam.ColumnMapping.DIAGNOSIS.columnName()));
            inspection.setApplyNo(rs.getString(Exam.ColumnMapping.APPLY_NO.columnName()));

            inspection.setSex(rs.getString(Exam.ColumnMapping.SEX.columnName()));
            inspection.setReportDate(rs.getString(Exam.ColumnMapping.REPORT_DATE.columnName()));
            inspection.setAuditDate(rs.getString(Exam.ColumnMapping.AUDIT_DATE.columnName()));
            inspection.setApplyProjectName(rs.getString(Exam.ColumnMapping.APPLY_PROJECT_NAME.columnName()));
            inspection.setAdvice(rs.getString(Exam.ColumnMapping.ADVICE.columnName()));
            inspection.setAge(rs.getString(Exam.ColumnMapping.AGE.columnName()));

            return inspection;
        }
    }

}
