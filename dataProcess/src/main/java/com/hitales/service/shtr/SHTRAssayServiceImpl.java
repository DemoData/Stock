package com.hitales.service.shtr;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.constant.CommonConstant;
import com.hitales.dao.TableDao;
import com.hitales.dao.standard.IAssayDao;
import com.hitales.entity.Assay;
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
@Service("shtrAssayService")
public class SHTRAssayServiceImpl extends TableService<Assay> {

    @Autowired
    @Qualifier("shtrAssayDao")
    private IAssayDao assayDao;

    @Override
    protected String[] getArrayCondition(Record record) {
        return new String[]{record.getGroupRecordName(), record.getReportDate()};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {

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
        record.setHospitalId("57b1e21fd897cd373ec7a0ed");
        record.setBatchNo("shtr2018040201");
        record.setDepartment("医务科");
        record.setFormat("table");
        record.setDeleted(false);
        record.setSource("化验");
        record.setStatus("AMD识别完成");
        record.setRecordType("化验记录");
        record.setSubRecordType("化验");
        record.setPatientId(StringUtils.isEmpty(record.getPatientId()) ? CommonConstant.EMPTY_FLAG : "shtr_" + record.getPatientId());
        record.setCreateTime(currentTimeMillis);
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

}
