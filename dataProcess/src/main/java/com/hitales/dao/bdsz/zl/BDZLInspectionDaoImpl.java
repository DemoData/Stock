package com.hitales.dao.bdsz.zl;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IExamDao;
import com.hitales.entity.Exam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository("bdzlInspectionDao")
public class BDZLInspectionDaoImpl extends BaseDao implements IExamDao<Exam> {

    @Override
    protected String generateQuerySql() {
        return null;
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
        return super.queryForListInSqlServer(getJdbcTemplate(dataSource), pageNum, pageSize, "TM_PACS_RESULT", null, null);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(id) from TM_PACS_RESULT", Integer.class);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String column, String groupRecordName) {
        String sql = "select t.Diagnosis from Diagnosis t where t.groupRecordName= ? group by t.Diagnosis";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);
    }

    class InspectionRowMapper implements RowMapper<Exam> {

        @Override
        public Exam mapRow(ResultSet rs, int rowNum) throws SQLException {
            Exam inspection = new Exam();
            inspection.setId(rs.getInt("id"));
            inspection.setPatientId(rs.getString("PID"));
            inspection.setHospitalId(rs.getString("groupRecordName"));
            String examineType = rs.getString("ExamineType");
            String examineItem = rs.getString("ExamineItem");

            StringBuffer inspectionType = new StringBuffer(StringUtils.isNotEmpty(examineType) ? examineType : "");
            inspectionType.append(StringUtils.isNotEmpty(examineItem) ? "-" + examineItem : "");
            inspection.setInspectionType(inspectionType.toString());

            inspection.setInspectionDate(rs.getString("PerformTime"));
            inspection.setClinicalDiagnosis(rs.getString("ClinicDiagnosis"));
            inspection.setReportClinical(rs.getString("ImageSight"));
            inspection.setDiagnosis(rs.getString("ImageDiagnosis"));
            return inspection;
        }
    }

}
