package com.hitales.service.bdsz.fs;

import com.hitales.entity.Exam;
import com.hitales.entity.Record;
import com.hitales.service.bdsz.zl.BDZLInspectionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("bdfsInspectionService")
public class BDFSInspectionServiceImpl extends BDZLInspectionServiceImpl {

    @Override
    protected void customInitInfo(Record record, Exam inspection) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180328");
        record.setDepartment("风湿免疫科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("检查");
        record.setStatus("AMD识别完成");
        record.setRecordType("检查记录");
        record.setSubRecordType("检查");
        //
        record.setPatientId("bdsz_" + inspection.getPatientId());
        record.setSourceId(inspection.getHospitalId());
        record.setGroupRecordName(inspection.getHospitalId());
    }

    @Override
    protected void customProcess(Record record, Exam entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        super.customProcess(record, entity, orgOdCatCaches, patientCaches, dataSource);
        record.setOdCategories(new String[]{"风湿"});
    }
}
