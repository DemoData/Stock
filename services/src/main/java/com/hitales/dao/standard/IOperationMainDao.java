package com.hitales.dao.standard;

import com.hitales.entity.OperationMain;

import java.util.List;

/**
 * @author aron
 */
public interface IOperationMainDao extends TextDao<OperationMain> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);

}
