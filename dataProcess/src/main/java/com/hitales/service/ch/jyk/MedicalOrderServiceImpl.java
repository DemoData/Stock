package com.hitales.service.ch.jyk;

import com.hitales.dao.standard.TableDao;
import com.hitales.dao.standard.IAdviceDao;
import com.hitales.entity.MedicalOrder;
import com.hitales.entity.Record;
import com.hitales.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxMedicalOrderService")
public class MedicalOrderServiceImpl extends TableService<MedicalOrder,MedicalOrder> {

    @Autowired
    @Qualifier("jyMedicalOrderDao")
    private IAdviceDao medicalOrderDao;

    @Override
    protected String[] getArrayCondition(Record record) {
        return new String[]{record.getGroupRecordName()};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = record.getGroupRecordName();
        if (StringUtils.isEmpty(groupRecordName)) {
            return;
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = medicalOrderDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
            String patientId = medicalOrderDao.findRequiredColByCondition(dataSource, groupRecordName);
            patientCaches.put(groupRecordName, patientId);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        record.setPatientId(patientCaches.get(groupRecordName));
    }

    @Override
    protected TableDao<MedicalOrder,MedicalOrder> currentDao() {
        return medicalOrderDao;
    }

    @Override
    protected void customInitInfo(Record record) {
        record.setPatientId("shch_" + record.getPatientId());
    }

    @Override
    protected void initInfoArray(Record record, List<MedicalOrder> assayList) {
        if (assayList == null || assayList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (MedicalOrder medicalOrder : assayList) {
            Map<String, String> map = new HashMap<>();
            map.put(MedicalOrder.ColumnMapping.MEDICAL_NAME.value(), medicalOrder.getMedicalName() == null ? EMPTY_FLAG : medicalOrder.getMedicalName());
            map.put(MedicalOrder.ColumnMapping.MEDICAL_START_DATE.value(), medicalOrder.getMedicalStartDate() == null ? EMPTY_FLAG : medicalOrder.getMedicalStartDate());
            map.put(MedicalOrder.ColumnMapping.MEDICAL_END_DATE.value(), medicalOrder.getMedicalEndDate() == null ? EMPTY_FLAG : medicalOrder.getMedicalEndDate());
            map.put(MedicalOrder.ColumnMapping.MEDICAL_RECORD_DATE.value(), medicalOrder.getMedicalRecordDate() == null ? EMPTY_FLAG : medicalOrder.getMedicalRecordDate());
            detailArray.add(map);
        }
    }

}