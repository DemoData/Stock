package com.hitales.service.bdsz.fs;

import com.hitales.common.constant.CommonConstant;
import com.hitales.entity.Record;
import com.hitales.service.bdsz.zl.BDZLAssayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("bdfsAssayService")
public class BDFSAssayServiceImpl extends BDZLAssayServiceImpl {


    @Override
    protected void customInitInfo(Record record) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz2018032801");
        record.setDepartment("风湿免疫科");
        record.setFormat("table");
        record.setDeleted(false);
        record.setSource("化验");
        record.setStatus("AMD识别完成");
        record.setRecordType("化验记录");
        record.setSubRecordType("化验");
        record.setPatientId(StringUtils.isEmpty(record.getPatientId()) ? CommonConstant.EMPTY_FLAG : "bdsz_" + record.getPatientId());
        record.setCreateTime(currentTimeMillis);
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        super.customProcess(record, orgOdCatCaches, patientCaches, dataSource);
        record.setOdCategories(new String[]{"风湿"});
    }
}
