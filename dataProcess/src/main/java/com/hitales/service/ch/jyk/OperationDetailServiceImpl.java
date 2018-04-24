package com.hitales.service.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.TableDao;
import com.hitales.dao.ch.jyk.OperationDetailDaoImpl;
import com.hitales.entity.OperationDetail;
import com.hitales.entity.Record;
import com.hitales.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxOperationDetailService")
public class OperationDetailServiceImpl extends TableService<OperationDetail> {

    @Autowired
    OperationDetailDaoImpl operationDetailDao;

    @Override
    protected String[] getArrayCondition(Record record) {
        return new String[]{record.getId()};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = record.getGroupRecordName();
        if (StringUtils.isEmpty(groupRecordName)) {
            return;
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = operationDetailDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
            String patientId = operationDetailDao.findPatientIdByGroupRecordName(dataSource, groupRecordName);
            patientCaches.put(groupRecordName, patientId);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        record.setPatientId(patientCaches.get(groupRecordName));
    }

    @Override
    protected TableDao<OperationDetail> currentDao() {
        return operationDetailDao;
    }

    /**
     * Set Record basic info
     *
     * @param record
     */
    protected void customInitInfo(Record record) {
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

    protected void initInfoArray(Record record, List<OperationDetail> OperationDetailList) {
        if (OperationDetailList == null || OperationDetailList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (OperationDetail operationDetail : OperationDetailList) {
            Map<String, String> map = new HashMap<>();
            map.put(OperationDetail.ColumnMapping.END_TIME.value(), operationDetail.getEndTime() == null ? EMPTY_FLAG : operationDetail.getEndTime());
            map.put(OperationDetail.ColumnMapping.NAME.value(), operationDetail.getName() == null ? EMPTY_FLAG : operationDetail.getName());
            map.put(OperationDetail.ColumnMapping.SERIAL_NUMBER.value(), operationDetail.getSerialNumber() == null ? EMPTY_FLAG : operationDetail.getSerialNumber());
            map.put(OperationDetail.ColumnMapping.PART.value(), operationDetail.getPart() == null ? EMPTY_FLAG : operationDetail.getPart());
            map.put(OperationDetail.ColumnMapping.LEVEL.value(), operationDetail.getLevel() == null ? EMPTY_FLAG : operationDetail.getLevel());
            map.put(OperationDetail.ColumnMapping.KERF_TYPE.value(), operationDetail.getKerfType() == null ? EMPTY_FLAG : operationDetail.getKerfType());
            map.put(OperationDetail.ColumnMapping.SPECIAL_REQUEST.value(), operationDetail.getSpecialRequest() == null ? EMPTY_FLAG : operationDetail.getSpecialRequest());
            map.put(OperationDetail.ColumnMapping.START_TIME.value(), operationDetail.getStartTime() == null ? EMPTY_FLAG : operationDetail.getStartTime());
            map.put(OperationDetail.ColumnMapping.LEVEL_DESC.value(), operationDetail.getLevelDesc() == null ? EMPTY_FLAG : operationDetail.getLevelDesc());
            map.put(OperationDetail.ColumnMapping.TUMOR_SIZE.value(), operationDetail.getTumorSize() == null ? EMPTY_FLAG : operationDetail.getTumorSize());
            detailArray.add(map);
        }
    }

}