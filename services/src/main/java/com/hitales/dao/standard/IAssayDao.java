package com.hitales.dao.standard;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.TableDao;
import com.hitales.entity.LabDetail;
import com.hitales.entity.LabBasic;

import java.util.List;

/**
 * @author aron
 */
public interface IAssayDao extends TableDao<LabDetail> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    JSONObject findRecordByIdInHRS(String applyId);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);

    List<LabBasic> findBasicArrayByCondition(String dataSource, String applyId);

}
