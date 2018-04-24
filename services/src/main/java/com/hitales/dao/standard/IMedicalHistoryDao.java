package com.hitales.dao.standard;

import com.hitales.entity.MedicalHistory;

import java.util.List;

/**
 * @author aron
 */
public interface IMedicalHistoryDao extends TextDao<MedicalHistory> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    int batchUpdateContent(String dataSource, List<Object[]> params);
}
