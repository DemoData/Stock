package com.hitales.dao.standard;

import java.util.List;

/**
 * @author aron
 */
@Deprecated
public interface IOperationDetailDao<Basic,Sub> extends TableDao<Basic,Sub> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findRequiredColByCondition(String dataSource, String applyId);

}
