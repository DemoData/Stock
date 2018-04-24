package com.hitales.service.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.TextFormatter;
import com.hitales.dao.standard.TextDao;
import com.hitales.dao.standard.IOperationMainDao;
import com.hitales.entity.OperationMain;
import com.hitales.entity.Record;
import com.hitales.service.TextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxOperationMainService")
public class OperationMainServiceImpl extends TextService<OperationMain> {

    @Autowired
    IOperationMainDao operationMainDao;

    @Override
    protected TextDao<OperationMain> currentDao() {
        return operationMainDao;
    }

    @Override
    protected void customProcess(Record record, OperationMain operationMain, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = record.getGroupRecordName();
        if (StringUtils.isEmpty(groupRecordName)) {
            return;
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = operationMainDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
            String patientId = operationMainDao.findRequiredColByCondition(dataSource, groupRecordName);
            patientCaches.put(groupRecordName, patientId);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        record.setPatientId(patientCaches.get(groupRecordName));
    }

    @Override
    protected Map<String, String> getFormattedText(OperationMain operationMain) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        List<Map<String, String>> infoList = new ArrayList<>();
        for (OperationMain.ColumnMapping operationMainEnum : OperationMain.ColumnMapping.values()) {
            Map<String, String> row = new HashMap<>();
            if (!operationMainEnum.isRequired()) {
                continue;
            }
            row.put(TextFormatter.PROP_NAME, operationMainEnum.propName());
            row.put(TextFormatter.COLUMN_NAME, operationMainEnum.columnName());
            infoList.add(row);
        }
        return TextFormatter.textFormatter(infoList, operationMain);
    }

    @Override
    protected void customInitInfo(Record record, OperationMain operationMain) {
        record.setGroupRecordName(operationMain.getGroupRecordName());
        record.setSourceId(operationMain.getId().toString());
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
