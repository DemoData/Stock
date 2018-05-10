package com.hitales.service.shrj;

import com.hitales.entity.Record;
import com.hitales.service.TableServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("shrjTableService")
public class SHRJTableServiceImpl extends TableServiceImpl {

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> groupRecordCaches, String dataSource) {
        super.customProcess(record, orgOdCatCaches, groupRecordCaches, dataSource);

        String identifiedStr = record.getGroupRecordName();
        if (groupRecordCaches.isEmpty() || StringUtils.isEmpty(groupRecordCaches.get(identifiedStr))) {
            String patientId = getAdviceDao().findRequiredColByCondition(dataSource, identifiedStr);
            if (!StringUtils.isEmpty(patientId)) {
                groupRecordCaches.put(identifiedStr, patientId);
            }
        }
        if (!StringUtils.isEmpty(groupRecordCaches.get(identifiedStr))) {
            //一次就诊号
            record.setPatientId(groupRecordCaches.get(identifiedStr));
        }

    }

}
