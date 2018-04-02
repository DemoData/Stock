package com.hitales.dao.temp.standard;

import com.hitales.entity.MedicalContentSplitModel;

import java.util.List;
import java.util.Map;

public interface ICHYXDao {

    void processTest();

    List<Map<String, Object>> findMedicalContentCountMap();

    List<Map<String, Object>> findCreateDateMedicalNameMapByVisitNumberAndMedicalContent(String visitNumber, String medicalContent);

    Map<String, Object> findByVisitNumberAndMedicalContentLimitOne(String visitNumber, String medicalContent);

    int update(MedicalContentSplitModel medicalContentSplitModel);

    int forbid(MedicalContentSplitModel medicalContentSplitModel);

    void add(Map<String, Object> map);

    void changeJdbcTemplate(String type) throws Exception;

    List<String> datacul(String sql);

    Integer dataculAdd(String sql);

    void addCheckDetail(List<String> headList, List<Map<String, Object>> data);

    void executeSql(String sql);

    List<Map<String, Object>> groupCount(String sql);
}
