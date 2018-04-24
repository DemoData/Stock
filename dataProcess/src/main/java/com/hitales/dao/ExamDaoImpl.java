package com.hitales.dao;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.IInspectionDao;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository("inspectionDao")
public class ExamDaoImpl extends BaseDao implements IInspectionDao<Map<String, Object>> {
    private Element examReport = null;
    private Element examDetail = null;
    private Element diagnosis;
    private Element groupRecordName;
    private String examReportTable;
    private String examDetailTable;

    {
        //TODO:这个路劲可以优化到baseService中去，然后通过basicInfo传入路径过来，再把得到的path传递给dao
        String path = this.getClass().getClassLoader().getResource("shly/exam-radiology.xml").getPath();
        SAXReader reader = new SAXReader();
        File xml = new File(path);
        try {
            Element rootElement = reader.read(xml).getRootElement();
            Element descriptor = rootElement.element("item-descriptor");
            List<Element> queryList = rootElement.element("queryList").elements();
            for (Element query : queryList) {
                String id = query.attribute("id").getValue();
                if ("odCategories".equals(id)) {
                    diagnosis = query;
                }
                if ("condition".equals(id)) {
                    groupRecordName = query;
                }
            }
            List<Element> tables = descriptor.elements("table");
            for (Element tableElement : tables) {
                String tableType = tableElement.attribute("type").getValue();
                if ("primary".equals(tableType)) {
                    examReport = tableElement;
                } else {
                    examDetail = tableElement;
                }
            }
            examReportTable = examReport.attribute("name").getValue();
            examDetailTable = examDetail.attribute("name").getValue();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

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
    public List<Map<String, Object>> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
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

    class InspectionRowMapper implements RowMapper<Map<String, Object>> {

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {


            return null;
        }
    }

}
