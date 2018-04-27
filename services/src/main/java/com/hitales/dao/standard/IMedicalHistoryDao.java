package com.hitales.dao.standard;

import java.util.List;

/**
 * @author aron
 */
public interface IMedicalHistoryDao<T> extends TextDao<T> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    int batchUpdateContent(String dataSource, List<Object[]> params);

    String findRequiredColByCondition(String dataSource, String condition);
}
