package com.hitales.dao.standard;

import com.hitales.dao.TableDao;
import com.hitales.entity.MedicalOrder;

import java.util.List;

/**
 * @author aron
 */
public interface IMedicalOrderDao extends TableDao<MedicalOrder> {
    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);
}
