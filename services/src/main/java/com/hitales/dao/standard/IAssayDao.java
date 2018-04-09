package com.hitales.dao.standard;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.TableDao;
import com.hitales.entity.Assay;
import com.hitales.entity.AssayApply;
import com.hitales.entity.Record;

import java.util.List;

/**
 * @author aron
 */
public interface IAssayDao extends TableDao<Assay> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    JSONObject findRecordByIdInHRS(String applyId);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);

    List<AssayApply> findBasicArrayByCondition(String dataSource, String applyId);

}
