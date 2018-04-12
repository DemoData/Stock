package com.hitales.service.ch.jyk;

import com.alibaba.fastjson.JSONObject;
import com.hitales.dao.TableDao;
import com.hitales.dao.standard.IAssayDao;
import com.hitales.entity.Assay;
import com.hitales.entity.AssayApply;
import com.hitales.entity.Record;
import com.hitales.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxAssayService")
public class AssayServiceImpl extends TableService<Assay> {

    @Autowired
    @Qualifier("jyAssayDao")
    private IAssayDao assayDao;

    @Override
    protected String[] getArrayCondition(Record record) {
        //这里是检验申请号
        return new String[]{record.getId()};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        initBasicInfo(record, dataSource);

        String groupRecordName = record.getGroupRecordName();
        if (StringUtils.isEmpty(groupRecordName)) {
            return;
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = assayDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
            String patientId = assayDao.findPatientIdByGroupRecordName(dataSource, groupRecordName);
            patientCaches.put(groupRecordName, patientId);
        }

        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        record.setPatientId(patientCaches.get(groupRecordName));
    }

    @Override
    protected void initBasicInfo(Record record, String dataSource) {
        List<AssayApply> applyList = assayDao.findBasicArrayByCondition(dataSource, record.getId());
        if (applyList == null || applyList.isEmpty()) {
            return;
        }
        JSONObject basicInfo = record.getInfo().getJSONObject("basicInfo");
        //init detail array
        List<String> names = new ArrayList<>();
        for (AssayApply assayApply : applyList) {
            if (StringUtils.isEmpty(assayApply.getAssayName())) {
                log.info("initBasicInfo(): assay name is empty:" + assayApply.toString());
                continue;
            }
            names.add(assayApply.getAssayName());
        }
        basicInfo.put(AssayApply.ColumnMapping.ASSAY_NAME.value(), names.toArray(new String[]{}));
        AssayApply assayApply = applyList.get(0);
        basicInfo.put(AssayApply.ColumnMapping.APPLY_DATE.value(), assayApply.getApplyDate() == null ? EMPTY_FLAG : assayApply.getApplyDate());
        basicInfo.put(AssayApply.ColumnMapping.APPLY_ID.value(), assayApply.getApplyId() == null ? EMPTY_FLAG : assayApply.getApplyId());
        basicInfo.put(AssayApply.ColumnMapping.SPECIMEN.value(), assayApply.getSpecimen() == null ? EMPTY_FLAG : assayApply.getSpecimen());
        basicInfo.put(AssayApply.ColumnMapping.STATE_NAME.value(), assayApply.getStateName() == null ? EMPTY_FLAG : assayApply.getStateName());
        basicInfo.put(AssayApply.ColumnMapping.SUB_ITEM_EN_CODE.value(), assayApply.getSubItemEnCode() == null ? EMPTY_FLAG : assayApply.getSubItemEnCode());
        basicInfo.put(AssayApply.ColumnMapping.SUB_ITEM_EN_NAME.value(), assayApply.getSubItemEnName() == null ? EMPTY_FLAG : assayApply.getSubItemEnName());
    }

    @Override
    protected TableDao<Assay> currentDao() {
        return assayDao;
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

    /**
     * Set Record basic info
     *
     * @param record
     */
    protected void customInitInfo(Record record) {

    }

    protected void initInfoArray(Record record, List<Assay> assayList) {
        if (assayList == null || assayList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (Assay assay : assayList) {
            Map<String, String> map = new HashMap<>();
            map.put(Assay.ColumnMapping.ASSAY_TIME.value(), assay.getAssayTime() == null ? EMPTY_FLAG : assay.getAssayTime());
            map.put(Assay.ColumnMapping.ASSAY_NAME.value(), assay.getAssayName() == null ? EMPTY_FLAG : assay.getAssayName());
            map.put(Assay.ColumnMapping.RESULT_FLAG.value(), assay.getResultFlag() == null ? EMPTY_FLAG : assay.getResultFlag());
            map.put(Assay.ColumnMapping.ASSAY_RESULT.value(), assay.getAssayResult() == null ? EMPTY_FLAG : assay.getAssayResult());
            map.put(Assay.ColumnMapping.ASSAY_VALUE.value(), assay.getAssayValue() == null ? EMPTY_FLAG : assay.getAssayValue());
            map.put(Assay.ColumnMapping.ASSAY_UNIT.value(), assay.getAssayUnit() == null ? EMPTY_FLAG : assay.getAssayUnit());
            map.put(Assay.ColumnMapping.ASSAY_SPECIMEN.value(), assay.getAssaySpecimen() == null ? EMPTY_FLAG : assay.getAssaySpecimen());
            map.put(Assay.ColumnMapping.REFERENCE_RANGE.value(), assay.getReferenceRange() == null ? EMPTY_FLAG : assay.getReferenceRange());
            map.put(Assay.ColumnMapping.ASSAY_STATE.value(), assay.getAssayState() == null ? EMPTY_FLAG : assay.getAssayState());
            map.put(Assay.ColumnMapping.ASSAY_METHODNAME.value(), assay.getAssayMethodName() == null ? EMPTY_FLAG : assay.getAssayMethodName());
            detailArray.add(map);
        }
    }

    protected boolean validateRecord(Record record) {
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        if (detailArray == null || detailArray.isEmpty()) {
            log.debug("validateRecord(): detailArray is empty:" + record.toString());
            return false;
        }
        return true;
    }

}
