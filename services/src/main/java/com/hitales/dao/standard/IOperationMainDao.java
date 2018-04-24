package com.hitales.dao.standard;

import java.util.List;

/**
 * @author aron
 */
public interface IOperationMainDao<T> extends TextDao<T> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findRequiredColByCondition(String dataSource, String applyId);

}
