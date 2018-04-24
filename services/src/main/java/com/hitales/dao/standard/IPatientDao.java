package com.hitales.dao.standard;

import com.alibaba.fastjson.JSONObject;
import com.hitales.entity.Patient;

import java.util.List;

/**
 * @author aron
 */
public interface IPatientDao extends IDataDao {

    JSONObject findPatientByIdInHRS(String pid);

    List<Patient> findPatients(String dataSource, int pageNum, int pageSize);

    void batchInsert2HRS(List<JSONObject> records);

    void save2HRS(JSONObject patient);

    Integer getCount(String dataSource);

}
