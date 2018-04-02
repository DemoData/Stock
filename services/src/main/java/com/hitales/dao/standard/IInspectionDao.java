package com.hitales.dao.standard;

import com.hitales.dao.TextDao;
import com.hitales.entity.Inspection;

import java.util.List;

public interface IInspectionDao extends TextDao<Inspection> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);
}
