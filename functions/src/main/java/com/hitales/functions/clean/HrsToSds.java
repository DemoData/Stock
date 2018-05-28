package com.hitales.functions.clean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.MongoOperations;
import com.hitales.common.util.TimeUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HrsToSds {
    private static MongoOperations hrsMongoOperations;
    private static MongoOperations sdsMongoOperations;
    private static MongoOperations hdpMongoOperations;

    private static Long projectProcessId = System.currentTimeMillis();
    private static String date = TimeUtil.intToStandardTime(projectProcessId);
    private static String HOST = "localhost";
    private static Integer PORT = 3718;
    private static String USERNAME = "xh";
    private static String PASSWORD = "rt0hizu{j9lzJNqi";

    static {
        MongoProperties hrsProperties = new MongoProperties();
        hrsProperties.setHost(HOST);
        hrsProperties.setPort(PORT);
        hrsProperties.setDatabase("HRS-live");
        hrsProperties.setUsername(USERNAME);
        hrsProperties.setPassword(PASSWORD.toCharArray());
        hrsMongoOperations = new MongoOperations(DBConnection.generateTemplate(hrsProperties));

        MongoProperties sdsProperties = new MongoProperties();
        sdsProperties.setHost(HOST);
        sdsProperties.setPort(PORT);
        sdsProperties.setDatabase("SDS-live");
        sdsProperties.setUsername(USERNAME);
        sdsProperties.setPassword(PASSWORD.toCharArray());
        sdsMongoOperations = new MongoOperations(DBConnection.generateTemplate(sdsProperties));

//        MongoProperties hdpProperties = new MongoProperties();
//        hdpProperties.setHost(HOST);
//        hdpProperties.setPort(PORT);
//        hdpProperties.setDatabase("HDP");
//        hdpProperties.setUsername(USERNAME);
//        hdpProperties.setPassword(PASSWORD.toCharArray());
//        hdpMongoOperations = new MongoOperations(DBConnection.generateTemplate(hdpProperties));
        /*MongoProperties hrsProperties = new MongoProperties();
        hrsProperties.setHost("localhost");
        hrsProperties.setPort(3718);
        hrsProperties.setDatabase("HRS-live");
        hrsProperties.setUsername("xh");
        hrsProperties.setPassword("rt0hizu{j9lzJNqi".toCharArray());
        hrsMongoOperations = new MongoOperations(DBConnection.generateTemplate(hrsProperties));

        MongoProperties sdsProperties = new MongoProperties();
        sdsProperties.setHost("localhost");
        sdsProperties.setPort(27017);
        sdsProperties.setDatabase("SDS");
        sdsProperties.setUsername("aron");
        sdsProperties.setPassword("aron".toCharArray());
        sdsMongoOperations = new MongoOperations(DBConnection.generateTemplate(sdsProperties));*/
    }

    public static void main(String[] args) {
        HrsToSds hrsToSds = new HrsToSds();
        hrsToSds.startProcess();
    }

    public void statisticCountHDP() {
        MongoTemplate mongoTemplate = hdpMongoOperations.getMongoTemplate();
        DBCollection dbCollection = mongoTemplate.getCollection("ALA");
        DBObject dbQuery = new BasicDBObject();
        dbQuery.put("projectProcessId", 1526534673469L);
        dbQuery.put("recordType", "化验记录");
        List<String> labItem = dbCollection.distinct("化验组样本", dbQuery);
        log.info(">>>>>>>>> labItem size ：" + labItem.size());
        List<String> itemLabItem = dbCollection.distinct("化验名称", dbQuery);
        log.info(">>>>>>>>> itemLabItem size ：" + itemLabItem.size());
        Map<String, Long> labNameCounter = new HashMap<>();
        Map<String, Long> labItemNameCounter = new HashMap<>();
        for (String item : labItem) {
            Query query = new Query();
            query.addCriteria(Criteria.where("projectProcessId").is(1526534673469L));
            query.addCriteria(Criteria.where("recordType").is("化验记录"));
            query.addCriteria(Criteria.where("化验组样本").is(item));
            long count = mongoTemplate.count(query, "ALA");
            log.info(">>>>>>>>> labItem count ：" + count + "->" + item);
            labNameCounter.put(item, count);
        }
        for (String item : itemLabItem) {
            Query query = new Query();
            query.addCriteria(Criteria.where("projectProcessId").is(1526534673469L));
            query.addCriteria(Criteria.where("recordType").is("化验记录"));
            query.addCriteria(Criteria.where("化验名称").is(item));
            long count = mongoTemplate.count(query, "ALA");
            log.info(">>>>>>>>> itemLabItem count ：" + count + "->" + item);
            labItemNameCounter.put(item, count);
        }

        BufferedWriter resultWriter = null;
        try {
            final String RESULT_FILE_PATH = "./hdp_counter_result2.txt";
            resultWriter = new BufferedWriter(new FileWriter(RESULT_FILE_PATH));
            for (Map.Entry<String, Long> entry : labNameCounter.entrySet()) {
                resultWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            resultWriter.write("===================================\n");
            for (Map.Entry<String, Long> entry : labItemNameCounter.entrySet()) {
                resultWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            resultWriter.write("===================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resultWriter != null) {
                try {
                    resultWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info(">>>>>>>>> 处理结束 <<<<<<<<<<");
    }

    public void statisticCount() {
        Query query = new Query();
        query.addCriteria(Criteria.where("projectProcessId").is(1526534673469L));
        query.addCriteria(Criteria.where("recordType").is("化验记录"));
        int pageNum = 0;
        boolean isFinished = false;
        Map<String, AtomicInteger> labNameCounter = new HashMap<>();
        Map<String, AtomicInteger> labItemNameCounter = new HashMap<>();
//        Map<String, AtomicInteger> microNameCounter = new HashMap<>();
//        Map<String, AtomicInteger> microItemNameCounter = new HashMap<>();
        BufferedWriter resultWriter = null;
        try {
            while (!isFinished) {
                query.with(new PageRequest(pageNum, 10000));
                List<JSONObject> records = hdpMongoOperations.find(query, JSONObject.class, "ALA");
                log.info(">>>>>>>>> found jsonObjects:" + records.size());
                if (records.size() < 10000) {
                    isFinished = true;
                }
                for (JSONObject record : records) {
//                String subRecordType = record.getString("subRecordType");
//                JSONObject info = record.getJSONObject("info");
//                if ("化验".equals(subRecordType)) {
//                JSONObject basicInfo = info.getJSONObject("basicInfo");
//                String specimen = basicInfo.getString("标本");
                    String key1 = record.getString("化验组样本");
                    String key2 = record.getString("化验名称");
                    counter(labNameCounter, key1);
                    counter(labItemNameCounter, key2);

               /* List<JSONObject> detailArray = info.getJSONArray("detailArray").toJavaList(JSONObject.class);
                for (JSONObject detail : detailArray) {
                    String labItemName = detail.getString("化验名称");
                    counter(labItemNameCounter, labItemName);
                }*/
//                }
                /*if ("微生物".equals(subRecordType)) {
                    List<JSONObject> detailArray = info.getJSONArray("detailArray").toJavaList(JSONObject.class);
                    for (JSONObject detail : detailArray) {
                        String labName = detail.getString("项目名称");
                        String labItemName = detail.getString("微生物名称");
                        counter(microNameCounter, labName);
                        counter(microItemNameCounter, labItemName);
                    }
                }*/
                }
                pageNum++;
                log.info(">>>>>>>>> 页数:" + pageNum);
            }
            final String RESULT_FILE_PATH = "/Users/aron/hdp_new_counter_result.txt";
            resultWriter = new BufferedWriter(new FileWriter(RESULT_FILE_PATH));
            for (Map.Entry<String, AtomicInteger> entry : labNameCounter.entrySet()) {
                resultWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            resultWriter.write("===================================\n");
            for (Map.Entry<String, AtomicInteger> entry : labItemNameCounter.entrySet()) {
                resultWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            resultWriter.write("************************************\n");
            /*for (Map.Entry<String, AtomicInteger> entry : microNameCounter.entrySet()) {
                resultWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            resultWriter.write("===================================\n");
            for (Map.Entry<String, AtomicInteger> entry : microItemNameCounter.entrySet()) {
                resultWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            resultWriter.write("===================================");*/
        } catch (Exception e) {
            e.printStackTrace();
            try {
                final String RESULT_FILE_PATH = "/Users/aron/hdp_new_counter_result.txt";
                resultWriter = new BufferedWriter(new FileWriter(RESULT_FILE_PATH));
                for (Map.Entry<String, AtomicInteger> entry : labNameCounter.entrySet()) {

                    resultWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
                }
                resultWriter.write("===================================\n");
                for (Map.Entry<String, AtomicInteger> entry : labItemNameCounter.entrySet()) {
                    resultWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
                }
                resultWriter.write("************************************\n");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (resultWriter != null) {
                try {
                    resultWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info(">>>>>>>>> 处理结束 <<<<<<<<<<");
    }

    private void counter(Map<String, AtomicInteger> counter, String key) {
        AtomicInteger labItemNameCount = counter.get(key);
        if (labItemNameCount != null) {
            labItemNameCount.incrementAndGet();
        } else {
            counter.put(key, new AtomicInteger(1));
        }
    }

    public void startProcess() {
        Query query = new Query();
        query.addCriteria(Criteria.where("batchNo").is("shrj20180508"));
        query.addCriteria(Criteria.where("recordType").is("化验记录"));
        query.addCriteria(Criteria.where("source").is("化验"));
        int pageNum = 0;
        boolean isFinished = false;
        List<JSONObject> msdataList = new ArrayList<>();
        while (!isFinished) {
            query.with(new PageRequest(pageNum, 1000));
            List<JSONObject> records = hrsMongoOperations.find(query, JSONObject.class, "Record");
            log.info(">>>>>>>>> found jsonObjects:" + records.size());
            if (records.size() < 1000) {
                isFinished = true;
            }
            for (JSONObject record : records) {
                JSONObject msRecord = putLab2Msdata(record);
                msdataList.add(msRecord);
            }
            if (!msdataList.isEmpty()) {
                sdsMongoOperations.insert(msdataList, "msdata");
                log.info(">>>>>>>>> inserted :" + msdataList.size());
                msdataList.clear();
            }
            pageNum++;
            log.info(">>>>>>>>> 页数:" + pageNum);
        }
        if (!msdataList.isEmpty()) {
            sdsMongoOperations.insert(msdataList, "msdata");
            log.info(">>>>>>>>>final inserted :" + msdataList.size());
            msdataList.clear();
        }
    }

    private JSONObject putLab2Msdata(JSONObject record) {
        JSONObject msReocrd = new JSONObject();
        initMsdataBasic(msReocrd, record);
        JSONObject msdata = new JSONObject();
        msdata.put("化验", new JSONArray());
        msReocrd.put("msdata", msdata);

        JSONObject info = record.getJSONObject("info");
        JSONObject basicInfo = info.getJSONObject("basicInfo").isEmpty() ? null : info.getJSONObject("basicInfo");
        JSONArray detailArray = info.getJSONArray("detailArray");

        String subRecordType = record.getString("subRecordType");
        if ("化验".equals(subRecordType)) {
            processLab(msdata, basicInfo, detailArray);
        }
        if ("微生物".equals(subRecordType)) {
            processMicro(msdata, detailArray);
        }
        return msReocrd;
    }

    private void processMicro(JSONObject msdata, JSONArray detailArray) {
        for (JSONObject detailItem : detailArray.toJavaList(JSONObject.class)) {
            String labTime = detailItem.getString("检验时间") == null ? "" : detailItem.getString("检验时间");
            String labName = detailItem.getString("项目名称");
            String labCode = detailItem.getString("微生物代码");
            String itemName = detailItem.getString("微生物名称");
            String numValue = detailItem.getString("检验值");
            String textValue = detailItem.getString("微生物培养结果");
            JSONObject lab = generateLab();
            lab.put("时间", labTime);
            lab.put("化验组样本", labName == null ? "" : labName);
            lab.put("化验代码", labCode == null ? "" : labCode);
            lab.put("数值", numValue == null ? "" : numValue);
            lab.put("化验", itemName == null ? "" : itemName);
            lab.put("化验定性结果", textValue == null ? "" : textValue);
            JSONArray msdataLab = msdata.getJSONArray("化验");
            msdataLab.add(lab);
        }
    }
    //处理长海胰腺的化验
    /*private void processLab(JSONObject msdata, JSONObject basicInfo, JSONArray detailArray) {
        //time
        String applyTime = basicInfo.getString("申请时间") == null ? "" : basicInfo.getString("申请时间");
        //
        String itemCode = basicInfo.getString("检验子项编码");
        String specimen = basicInfo.getString("标本");
        for (JSONObject detailItem : detailArray.toJavaList(JSONObject.class)) {
            String labTime = detailItem.getString("化验时间") == null ? "" : detailItem.getString("化验时间");
            String reference = detailItem.getString("参考值");
            String textValue = detailItem.getString("检验结果");
            String numValue = detailItem.getString("检验值");
            String itemName = detailItem.getString("化验名称");
            String flag = detailItem.getString("异常情况");
            String unit = detailItem.getString("化验单位");
            JSONObject lab = generateLab();
            lab.put("时间", StringUtils.isBlank(labTime) ? applyTime : labTime);
            lab.put("化验组样本", specimen == null ? "" : specimen);
            lab.put("化验代码", itemCode == null ? "" : itemCode);
            lab.put("化验", itemName == null ? "" : itemName);
            lab.put("异常", flag == null ? "" : flag);
            lab.put("数值单位", unit == null ? "" : unit);
            lab.put("参考范围", reference == null ? "" : reference);

            formattResultText(lab, textValue, numValue);

            JSONArray msdataLab = msdata.getJSONArray("化验");
            msdataLab.add(lab);
        }
    }*/

    /**
     * 规范化后的化验处理
     *
     * @param msdata
     * @param basicInfo
     * @param detailArray
     */
    private void processLab(JSONObject msdata, JSONObject basicInfo, JSONArray detailArray) {
        String time = "";
        String labName = "";
        if (basicInfo != null) {
            //time
            time = basicInfo.getString("申请时间") == null ? "" : basicInfo.getString("申请时间");
            //
            labName = basicInfo.getString("化验名称");
        }

        for (JSONObject detailItem : detailArray.toJavaList(JSONObject.class)) {

            String checkTime = detailItem.getString("检测时间") == null ? "" : detailItem.getString("检测时间");
            if (StringUtils.isEmpty(time)) time = checkTime;

            String reference = detailItem.getString("参考范围");
            String textValue = detailItem.getString("文本结果");
            String numValue = detailItem.getString("数值结果");
            String itemName = detailItem.getString("化验项名称");
            String flag = detailItem.getString("异常标识");
            String unit = detailItem.getString("结果单位");
            String itemCode = detailItem.getString("化验项代码");
            JSONObject lab = generateLab();
            lab.put("时间", time);
            lab.put("化验组样本", labName == null ? "" : labName);
            lab.put("化验代码", itemCode == null ? "" : itemCode);
            lab.put("化验", itemName == null ? "" : itemName);
            lab.put("异常", flag == null ? "" : flag);
            lab.put("数值单位", unit == null ? "" : unit);
            lab.put("参考范围", reference == null ? "" : reference);

            formattResultText(lab, textValue, numValue);

            JSONArray msdataLab = msdata.getJSONArray("化验");
            msdataLab.add(lab);
        }
    }

    private void formattResultText(JSONObject lab, String textValue, String numValue) {
        if (StringUtils.isNotBlank(numValue) && !"0.0".equals(numValue)) {
            lab.put("数值", numValue == null ? "" : numValue);
            return;
        }
        if (StringUtils.isEmpty(textValue)) {
            return;
        }
        StringBuilder numberValue = new StringBuilder("");
        String labTextValue = textValue;
        //筛选出只包含中文，数字，小数点，括号，空格的
        String regex = "[ (（)）E.0-9\\u4E00-\\u9FA5]+$";
        Matcher matcher = Pattern.compile(regex).matcher(textValue);
        if (matcher.matches()) {
            //取出其中的数字和小数点包括科学计数法
            Matcher matcherNum = Pattern.compile("[0-9.E]*").matcher(textValue);
            while (matcherNum.find()) {
                String group = matcherNum.group(0);
                if (!"".equals(group.trim())) {
                    //取出的数字必须包含数字
                    Matcher numMat = Pattern.compile(".*\\d+.*").matcher(group);
                    if (numMat.matches()) {
                        numberValue.append(group.trim());
                    }
                }
            }
            //取出非数字内容
            String valueWithoutNum = matcherNum.replaceAll("");
            //去掉中英文括号
            labTextValue = valueWithoutNum.replaceAll("[ (（)）]*", "");
        }
        if ("".equals(numberValue.toString())) {
            String negativRregex = "^[-]?[0-9.]+$";
            Matcher nMatcher = Pattern.compile(negativRregex).matcher(textValue);
            if (nMatcher.matches()) {
                numberValue.append(nMatcher.group(0));
                labTextValue = nMatcher.replaceAll("");
            }
        }
        lab.put("数值", numberValue.toString());
        lab.put("化验定性结果", labTextValue);
    }

    private JSONObject generateLab() {
        JSONObject lab = new JSONObject();
        lab.put("数值", "");
        lab.put("化验组", "");
        lab.put("化验定性结果高峰值", "");
        lab.put("原文时间", "");
        lab.put("化验数值高峰值", "");
        lab.put("父类化验项", "");
        lab.put("化验描绘词", "");
        lab.put("化验变化态", "");
        lab.put("化验定性结果", "");
        lab.put("时间错误类型", "");
        lab.put("化验组样本", "");
        lab.put("否定词", "");
        lab.put("化验样本", "");
        lab.put("化验名称样本", "");
        lab.put("模糊时间类型", "");
        lab.put("段落标题", "");
        lab.put("时间", "");
        lab.put("时间类型", "");
        lab.put("数值单位", "");
        lab.put("化验", "");
        lab.put("异常", "");
        lab.put("化验条件", "");
        lab.put("化验代码", "");
        lab.put("仪器编号", "");
        lab.put("参考范围", "");
        lab.put("申请科室", "");
        return lab;
    }

    private void initMsdataBasic(JSONObject msdata, JSONObject record) {
        String rid = record.getString("_id");
        String batchNo = record.getString("batchNo");
        String patientId = record.getString("patientId");
        String recordType = record.getString("recordType");
        String hospitalId = record.getString("hospitalId");
        String format = record.getString("format");
        JSONArray odCategories = record.getJSONArray("odCategories");

        msdata.put("date", date);
        msdata.put("recordid", rid);
        msdata.put("projectProcessId", projectProcessId);
        msdata.put("batchNo", batchNo);
        msdata.put("odCategories", odCategories);
        msdata.put("patientid", patientId);
        msdata.put("hospitalId", hospitalId);
        msdata.put("recordType", recordType);
        msdata.put("format", format);
        msdata.put("SDS_Version", "V1.0.0");
    }

}
