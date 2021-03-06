package com.hitales.dao.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IMedicalHistoryDao;
import com.hitales.entity.MedicalHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository("jyMedicalHistoryDao")
public class MedicalHistoryDaoImpl extends BaseDao implements IMedicalHistoryDao<MedicalHistory> {

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(*) from `病历文书` where status=0", Integer.class);
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select id,`一次就诊号`,`病人ID号`,`更新内容`,`病历名称` from `病历文书` where status=0 ";
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new MedicalHistoryRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<MedicalHistory> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ? group by t.`诊断名称`";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);
    }

    @Override
    public int batchUpdateContent(String dataSource, List<Object[]> params) {
        return 0;
    }


    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            super.insert(records, collectionName);
        }
    }

    class MedicalHistoryRowMapper implements RowMapper<MedicalHistory> {

        @Override
        public MedicalHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            MedicalHistory medicalHistory = new MedicalHistory();
            medicalHistory.setId(rs.getInt("id"));
            medicalHistory.setGroupRecordName(rs.getString(MedicalHistory.ColumnMapping.GROUP_RECORD_NAME.value()));
            medicalHistory.setPatientId(rs.getString(MedicalHistory.ColumnMapping.PATIENT_ID.value()));
            //用于获取所属类型
//            medicalHistory.setMapping(rs.getString("mapping"));
            medicalHistory.setMedicalHistoryName(rs.getString(MedicalHistory.ColumnMapping.MEDICAL_HISTORY_NAME.value()));
            medicalHistory.setMedicalContent(rs.getString(MedicalHistory.ColumnMapping.MEDICAL_CONTENT.value()));
            return medicalHistory;
        }
    }

}
