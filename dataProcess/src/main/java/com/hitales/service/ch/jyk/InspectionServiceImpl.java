package com.hitales.service.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.TextFormatter;
import com.hitales.dao.TextDao;
import com.hitales.dao.standard.IInspectionDao;
import com.hitales.entity.Inspection;
import com.hitales.entity.Record;
import com.hitales.service.TextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxInspectionService")
public class InspectionServiceImpl extends TextService<Inspection> {

    @Autowired
    @Qualifier("jyInspectionDao")
    private IInspectionDao inspectionDao;

    @Override
    protected TextDao<Inspection> currentDao() {
        return inspectionDao;
    }

    @Override
    protected void customProcess(Record record, Inspection inspection, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = inspection.getGroupRecordName();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = inspectionDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
    }

    @Override
    protected Map<String, String> getFormattedText(Inspection inspection) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        List<Map<String, String>> infoList = new ArrayList<>();
        for (Inspection.ColumnMapping columnMapping : Inspection.ColumnMapping.values()) {
            Map<String, String> row = new HashMap<>();
            if (!columnMapping.isRequired()) {
                continue;
            }
            row.put(TextFormatter.PROP_NAME, columnMapping.propName());
            row.put(TextFormatter.COLUMN_NAME, columnMapping.columnName());
            infoList.add(row);
        }
        return TextFormatter.textFormatter(infoList, inspection);
    }

    @Override
    protected void customInitInfo(Record record, Inspection inspection) {
        record.setPatientId("shch_" + inspection.getPatientId());
        record.setGroupRecordName(inspection.getGroupRecordName());
        record.setSourceId(inspection.getReportId());
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
