package com.hitales.service.ch.xgwk;

import com.hitales.common.support.TextFormatter;
import com.hitales.dao.standard.TextDao;
import com.hitales.dao.standard.IExamDao;
import com.hitales.entity.Exam;
import com.hitales.entity.Record;
import com.hitales.service.TextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chxgInspectionFSService")
public class XGInspectionFSServiceImpl extends TextService<Exam> {

    @Autowired
    @Qualifier("xgInspectionFSDao")
    private IExamDao inspectionFSDao;

    @Override
    protected TextDao<Exam> currentDao() {
        return inspectionFSDao;
    }

    @Override
    protected void customProcess(Record record, Exam entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {

    }

    @Override
    protected Map<String, String> getFormattedText(Exam entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        List<Map<String, String>> infoList = new ArrayList<>();
        for (Exam.ColumnMapping columnMapping : Exam.ColumnMapping.values()) {
            Map<String, String> row = new HashMap<>();
            if (!columnMapping.isRequired()) {
                continue;
            }
            row.put(TextFormatter.PROP_NAME, columnMapping.propName());
            row.put(TextFormatter.COLUMN_NAME, columnMapping.columnName());
            infoList.add(row);
        }
        return TextFormatter.textFormatter(infoList, entity);
    }

    @Override
    protected void customInitInfo(Record record, Exam inspection) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setBatchNo("shch20180315");
        record.setDepartment("血管外科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
        record.setRecordType("检查记录");
        record.setSubRecordType("放射");
        //
        record.setPatientId("shch_" + inspection.getPatientId());
        record.setSourceId(inspection.getHospitalId());
        record.setGroupRecordName(inspection.getHospitalId());
        record.setOrgOdCategories(new String[]{});
    }


}