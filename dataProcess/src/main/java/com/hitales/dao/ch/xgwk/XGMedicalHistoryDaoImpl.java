package com.hitales.dao.ch.xgwk;

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
@Repository("xgMedicalHistoryDao")
public class XGMedicalHistoryDaoImpl extends BaseDao implements IMedicalHistoryDao<MedicalHistory> {

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(id) from `病历文书` where isStock=0", Integer.class);
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select * from `病历文书` where isStock=0";
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
        return null;
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
            medicalHistory.setGroupRecordName(rs.getString("groupRecordName"));
            medicalHistory.setPatientId(rs.getString("pid"));
            //用于获取所属类型
            medicalHistory.setMedicalHistoryName(rs.getString("病历类别"));
            medicalHistory.setMedicalContent(rs.getString("病历文本"));
            medicalHistory.setOrgOdCategories(rs.getString("orgOdCategories"));
            return medicalHistory;
        }
    }

}
