package com.hitales.dao.standard;

import java.util.List;

/**
 * @author aron
 */
public interface IInspectionDao<T> extends TextDao<T> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);
}
