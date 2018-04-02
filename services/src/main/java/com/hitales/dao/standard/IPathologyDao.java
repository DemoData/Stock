package com.hitales.dao.standard;

import com.hitales.dao.TextDao;
import com.hitales.entity.Pathology;

import java.util.List;

/**
 * @author aron
 */
public interface IPathologyDao extends TextDao<Pathology> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

}
