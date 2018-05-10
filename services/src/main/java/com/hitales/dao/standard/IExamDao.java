package com.hitales.dao.standard;

import java.util.List;

/**
 * @author aron
 */
@Deprecated
public interface IExamDao<T> extends TextDao<T> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String conditionCol, String condition);

    String findRequiredColByCondition(String dataSource, String condition);
}
