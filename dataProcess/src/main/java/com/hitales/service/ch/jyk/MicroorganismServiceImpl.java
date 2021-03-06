package com.hitales.service.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.standard.TableDao;
import com.hitales.dao.standard.IMicroorganismDao;
import com.hitales.entity.Microorganism;
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
@Service("chyxMicroorganismService")
public class MicroorganismServiceImpl extends TableService<Microorganism,Microorganism> {

    @Autowired
    IMicroorganismDao microorganismDao;

    @Override
    protected String[] getArrayCondition(Record record) {
        return new String[]{record.getId()};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = record.getGroupRecordName();
        if(StringUtils.isEmpty(groupRecordName)){
            return;
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = microorganismDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
            String patientId = microorganismDao.findRequiredColByCondition(dataSource, groupRecordName);
            patientCaches.put(groupRecordName, patientId);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        record.setPatientId(patientCaches.get(groupRecordName));
    }

    @Override
    protected TableDao<Microorganism,Microorganism> currentDao() {
        return microorganismDao;
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

    protected void initInfoArray(Record record, List<Microorganism> microorganismList) {
        if (microorganismList == null || microorganismList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (Microorganism microorganism : microorganismList) {
            Map<String, String> map = new HashMap<>();
            map.put(Microorganism.ColumnMapping.GROUP_RECORD_NAME.value(), microorganism.getGroupRecordName() == null ? EMPTY_FLAG : microorganism.getGroupRecordName());
            map.put(Microorganism.ColumnMapping.VALIDATE_METHOD_CODE.value(), microorganism.getValidateMethodCode() == null ? EMPTY_FLAG : microorganism.getValidateMethodCode());
            map.put(Microorganism.ColumnMapping.CHECK_DATE.value(), microorganism.getCheckDate() == null ? EMPTY_FLAG : microorganism.getCheckDate());
            map.put(Microorganism.ColumnMapping.CHECK_APPLY_NO.value(), microorganism.getCheckApplyNo() == null ? EMPTY_FLAG : microorganism.getCheckApplyNo());
            map.put(Microorganism.ColumnMapping.MICROORGANISM_CODE.value(), microorganism.getMicroorganismCode() == null ? EMPTY_FLAG : microorganism.getMicroorganismCode());
            map.put(Microorganism.ColumnMapping.MICROORGANISM_GROW_RESULT.value(), microorganism.getMicroorganismGrowResult() == null ? EMPTY_FLAG : microorganism.getMicroorganismGrowResult());
            map.put(Microorganism.ColumnMapping.CHECK_VALUE.value(), microorganism.getCheckValue() == null ? EMPTY_FLAG : microorganism.getCheckValue());
            map.put(Microorganism.ColumnMapping.CHECK_RESULT.value(), microorganism.getCheckResult() == null ? EMPTY_FLAG : microorganism.getCheckResult());
            map.put(Microorganism.ColumnMapping.ANTIBIOTIC_NAME.value(), microorganism.getAntibioticName() == null ? EMPTY_FLAG : microorganism.getAntibioticName());
            map.put(Microorganism.ColumnMapping.MICROORGANISM_NAME.value(), microorganism.getMicroorganismName() == null ? EMPTY_FLAG : microorganism.getMicroorganismName());
            map.put(Microorganism.ColumnMapping.PROJECT_NAME.value(), microorganism.getProjectName() == null ? EMPTY_FLAG : microorganism.getProjectName());
            map.put(Microorganism.ColumnMapping.REMARK.value(), microorganism.getRemark() == null ? EMPTY_FLAG : microorganism.getRemark());
            detailArray.add(map);
        }
    }

}