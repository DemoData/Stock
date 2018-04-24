package com.hitales.dao.standard;

import com.hitales.entity.Exam;

import java.util.List;

/**
 * @author aron
 */
public interface IInspectionDao extends TextDao<Exam> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);
}
