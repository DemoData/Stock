package com.hitales.dao.standard;

import com.hitales.entity.LabBasic;
import com.hitales.entity.LabDetail;

import java.util.List;

/**
 * @author aron
 */
public interface IAssayDao extends TableDao<LabDetail> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);

    List<LabBasic> findBasicArrayByCondition(String dataSource, String applyId);

}
