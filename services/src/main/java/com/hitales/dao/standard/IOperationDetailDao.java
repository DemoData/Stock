package com.hitales.dao.standard;

import com.hitales.entity.OperationDetail;

import java.util.List;

/**
 * @author aron
 */
public interface IOperationDetailDao extends TableDao<OperationDetail> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);

}
