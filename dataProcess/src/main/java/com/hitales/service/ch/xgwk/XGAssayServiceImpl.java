package com.hitales.service.ch.xgwk;

import com.hitales.common.util.TimeUtil;
import com.hitales.dao.standard.TableDao;
import com.hitales.dao.standard.IAssayDao;
import com.hitales.entity.LabDetail;
import com.hitales.entity.Record;
import com.hitales.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chxgAssayService")
public class XGAssayServiceImpl extends TableService<LabDetail> {

    @Autowired
    @Qualifier("xgAssayDao")
    private IAssayDao assayDao;

    private Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();

    @Override
    protected String[] getArrayCondition(Record record) {
        String groupRecordName = record.getGroupRecordName();
        String reportDate = record.getReportDate();
        if (StringUtils.isBlank(groupRecordName)) {
            log.error("getArrayCondition(): patientId is null");
            return null;
        }
        return new String[]{groupRecordName, reportDate};
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {

    }

    @Override
    protected TableDao<LabDetail> currentDao() {
        return assayDao;
    }

    /**
     * Set Record basic info
     *
     * @param record
     */
    protected void customInitInfo(Record record) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setBatchNo("shch20180316");
        record.setDepartment("血管外科");
        record.setFormat("table");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
        record.setRecordType("化验记录");
        record.setSubRecordType("化验");
        record.setPatientId("shch_" + record.getPatientId());
        record.setOrgOdCategories(new String[]{});
        record.setCreateTime(currentTimeMillis);
    }

    protected void initInfoArray(Record record, List<LabDetail> assayList) {
        if (assayList == null || assayList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (LabDetail assay : assayList) {
            Map<String, String> map = new HashMap<>();
            map.put(LabDetail.ColumnMapping.ASSAY_SPECIMEN.value(), assay.getAssaySpecimen() == null ? EMPTY_FLAG : assay.getAssaySpecimen());
            map.put(LabDetail.ColumnMapping.ASSAY_NAME.value(), assay.getAssayName() == null ? EMPTY_FLAG : assay.getAssayName());
            map.put(LabDetail.ColumnMapping.ASSAY_RESULT.value(), assay.getAssayResult() == null ? EMPTY_FLAG : assay.getAssayResult());
            map.put(LabDetail.ColumnMapping.ASSAY_UNIT.value(), assay.getAssayUnit() == null ? EMPTY_FLAG : assay.getAssayUnit());
            map.put(LabDetail.ColumnMapping.RESULT_FLAG.value(), assay.getResultFlag() == null ? EMPTY_FLAG : assay.getResultFlag());
            map.put(LabDetail.ColumnMapping.ASSAY_TIME.value(), assay.getAssayTime() == null ? EMPTY_FLAG : assay.getAssayTime());
            detailArray.add(map);
        }
    }

}