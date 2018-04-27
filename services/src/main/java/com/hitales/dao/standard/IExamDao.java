package com.hitales.dao.standard;

import java.util.List;

/**
 * @author aron
 */
public interface IExamDao<T> extends TextDao<T> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findRequiredColByCondition(String dataSource, String condition);
}
