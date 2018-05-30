package com.hitales.functions.clean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.BatchUpdateOption;
import com.hitales.common.support.Mapping;
import com.hitales.common.support.MappingMatch;
import com.hitales.common.support.MongoOperations;
import com.hitales.entity.LabDetail;
import com.hitales.entity.Patient;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DataClean {

    private static MongoOperations mongoOperations;

    private static JdbcTemplate jdbcTemplate;

    private static String EMPTY_FLAG = "";

    static {
        MongoProperties hrsProperties = new MongoProperties();
        hrsProperties.setHost("dds-bp1baff8ad4002a41.mongodb.rds.aliyuncs.com");
        hrsProperties.setPort(3717);
        hrsProperties.setDatabase("HRS-live");
        hrsProperties.setUsername("xh");
        hrsProperties.setPassword("rt0hizu{j9lzJNqi".toCharArray());
        mongoOperations = new MongoOperations(DBConnection.generateTemplate(hrsProperties));

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:mysql://rm-bp191mn7925119b5awo.mysql.rds.aliyuncs.com:3306/rk?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&useSSL=false");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("Yiy1health_2017");
        dataSourceBuilder.driverClassName("com.mysql.jdbc.Driver");
        DataSource dataSource = dataSourceBuilder.build();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public static void main(String[] args) {
        DataClean dataClean = new DataClean();
//        dataClean.shchRestore4Lab();
    }

    private Integer index = 1;

    /*public void shchRestore4Lab() {
        int count = 0;
        int result = 0;
        int pageNum = 0;
        boolean isFinished = false;
        List<BatchUpdateOption> options = new ArrayList<>();

        Query query = new Query();
        query.addCriteria(Criteria.where("batchNo").is("shch20180309"));
        query.addCriteria(Criteria.where("subRecordType").is("化验"));

        while (!isFinished) {
            query.with(new PageRequest(pageNum, 5000));
            List<JSONObject> records = mongoOperations.find(query, JSONObject.class, "Record");
            log.info(">>>>>>>>> found jsonObjects:" + records.size());
            if (records.size() < 5000) {
                isFinished = true;
            }
            for (JSONObject record : records) {
                String rid = record.getString("_id");
                String sourceId = record.getString("sourceId");
                StringBuilder sql = new StringBuilder("select t.`检验时间` AS 'assayTime',t.`项目名称` AS 'assayName',t.`结果正常标志` AS 'resultFlag',t.`检验结果` AS 'assayResult',t.`检验值` AS 'assayValue',t.`单位` AS 'assayUnit',t.`标本` AS 'assaySpecimen',t.`参考范围` AS 'referenceRange',t.`检验状态` AS 'assayState',t.`检验方法名称` AS 'assayMethodName',t.`仪器编号` AS 'machineNo' from ");
                JSONArray odCategories = record.getJSONArray("odCategories");
                List<LabDetail> details = null;
                if (odCategories.contains("胰腺相关")) {
                    sql.append("`shch_yxxg_检验报告明细` t ");
                }
                if (odCategories.contains("胰腺占位")) {
                    sql.append("`shch_yxzw_检验报告明细` t ");
                }
                if (odCategories.contains("糖尿病相关")) {
                    sql.append("`shch_tnb_检验报告明细` t ");
                }
                if (odCategories.contains("健康查体")) {
                    sql.append("`shch_jkct_检验报告明细` t ");
                }
                sql.append("where t.`检验申请号`=?");
                details = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper(LabDetail.class), sourceId);
                if (details == null || details.isEmpty()) {
                    continue;
                }
                List<Map<String, String>> updatedArray = detailaArray2Map(details);
                BatchUpdateOption bathUpdateOption = new BatchUpdateOption();
                bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(rid)));
                bathUpdateOption.setUpdate(Update.update("info.detailArray", updatedArray));
                bathUpdateOption.setMulti(true);
                bathUpdateOption.setUpsert(false);
                options.add(bathUpdateOption);
                count++;
                //超过1000个执行一次更新
                if (options.size() >= 1000) {
                    result += updateStart(options, "Record");
                }
            }
            if (!options.isEmpty()) {
                result += updateStart(options, "Record");
            }
            pageNum++;
            log.info(">>>>>>>>> 页数:" + pageNum);
        }
        if (!options.isEmpty()) {
            result += updateStart(options, "Record");
        }
        log.info(">>>>>>>>>>>Done," + count + ",effected:" + result);
    }*/

    private void startUpdate(Map<String, List<String>> filter) {
        List<Object[]> updateList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filter.entrySet()) {
            String str = entry.getKey();
            StringBuilder groupRecordName = new StringBuilder();
            //处理前缀
            for (int i = index.toString().length(); i < 4; i++) {
                groupRecordName.append("0");
            }
            groupRecordName.append(index++);
            if (str.length() > 4 && str.contains(".")) {
                String[] dateArray = str.split("\\.");
                if (dateArray == null || dateArray.length == 0) {
                    groupRecordName.append(dateArray[0]);
                }
                groupRecordName.append(dateArray[0]);
                Integer month = Integer.valueOf(dateArray[1]);
                Integer day = Integer.valueOf(dateArray[2]);

                String monthValue = month < 10 ? "0" + month : month.toString();
                String dayValue = day < 10 ? "0" + day : day.toString();
                groupRecordName.append(monthValue).append(dayValue);
            } else {
                String suffix = UUID.randomUUID().toString().substring(0, 8);
                groupRecordName.append(suffix);
            }
//            String[] params = {groupRecordName.toString(), entry.getValue()};
            for (String id : entry.getValue()) {
                String[] params = {groupRecordName.toString(), id};
                updateList.add(params);
            }
            if (updateList.size() > 500) {
                log.info(">>>>>>>>>>> updating:" + updateList.size() + " <<<<<<<<<<<<");
                jdbcTemplate.batchUpdate("update 仁济南院_medical_content set groupRecordName=? where id=?", updateList);
                updateList.clear();
            }
        }
        if (!updateList.isEmpty()) {
            log.info(">>>>>>>>>>> updating:" + updateList.size() + " <<<<<<<<<<<<");
            jdbcTemplate.batchUpdate("update 仁济南院_medical_content set groupRecordName=? where id=?", updateList);
            updateList.clear();
        }
    }

    /**
     * 仁济病人基本信息获取性别出生日等
     */
    public void shrjPatientIgnore() {
        List<Map<String, Object>> results = jdbcTemplate.queryForList("select id,groupRecordName from 仁济_patient where groupRecordName like '%E%'");
        for (Map<String, Object> map : results) {
            String groupRecordName = map.get("groupRecordName").toString();
            String id = map.get("id").toString();
//            String[] split = groupRecordName.split("E");
//            String temp = groupRecordName.replace("E","E+");
            BigDecimal bd = new BigDecimal(groupRecordName);
            String value = bd.toPlainString();
            jdbcTemplate.update("update 仁济_patient set groupRecordName=? where id=?", value, id);
        }
    }

    /**
     * 仁济病人基本信息获取性别出生日等
     */
    public void shrjPatientProcess() {
        int count = 0;
        int result = 0;
        List<BatchUpdateOption> options = new ArrayList<>();

        Query query = new Query();
        query.addCriteria(Criteria.where("batchNo").is("shrj20180508"));
        query.addCriteria(Criteria.where("出生日期").is(""));
        List<JSONObject> patients = mongoOperations.find(query, JSONObject.class, "Patient");
        for (JSONObject record : patients) {
            String pid = record.getString("_id");
            List<Map<String, Object>> results = jdbcTemplate.queryForList("select sex,age,inHospitalDate from 仁济_patient where patientId = ?", pid.substring(pid.indexOf("_") + 1));
            Integer ageInt = null;
            Integer yearInt = null;
            String sex = null;
            for (Map<String, Object> map : results) {
                Object age = map.get("age");
                Object inHospitalDate = map.get("inHospitalDate");
                if (age == null || inHospitalDate == null) {
                    continue;
                }
                sex = map.get("sex") == null ? null : map.get("sex").toString();
                ageInt = Integer.valueOf(age.toString());
                yearInt = Integer.valueOf(inHospitalDate.toString().substring(0, 4));
                break;
            }

            if (ageInt == null || yearInt == null) {
                continue;
            }
            Integer birthday = yearInt - ageInt;

            BatchUpdateOption bathUpdateOption = new BatchUpdateOption();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(pid)));
            bathUpdateOption.setUpdate(Update.update("年龄", ageInt.toString())
                    .set("性别", sex)
                    .set("出生日期", birthday.toString()));
            bathUpdateOption.setMulti(true);
            bathUpdateOption.setUpsert(false);
            options.add(bathUpdateOption);
            count++;
            //超过1000个执行一次更新
            if (options.size() >= 1000) {
                result += updateStart(options, "Patient");
            }
        }
        if (!options.isEmpty()) {
            result += updateStart(options, "Patient");
        }
        log.info(">>>>>>>>>>>Done," + count + ",effected:" + result);
    }

    public void setOrgCategories() {
        int count = 0;
        int result = 0;
        List<BatchUpdateOption> options = new ArrayList<>();
//        DBObject dbQuery = new BasicDBObject();
//        dbQuery.put("batchNo", "shch20180416");
//        List<String> allGroupRecordName = hrsMongoTemplate.getCollection("Record").distinct("groupRecordName", dbQuery);
        Query query = new Query();
        query.addCriteria(Criteria.where("batchNo").is("shly20180424"));
        query.addCriteria(Criteria.where("source").is("病历文书"));
        List<JSONObject> records = mongoOperations.find(query, JSONObject.class, "Record");

        for (JSONObject record : records) {
            String rid = record.getString("_id");
            String groupRecordName = record.getString("groupRecordName");
            //找到就诊id
            List<Map<String, Object>> icdList = jdbcTemplate.queryForList("select ID from shly_in_patient_visit_record_20180423 where AdmissionNumber=? group by AdmissionNumber,ID", groupRecordName);
            if (icdList == null || icdList.isEmpty()) {
                continue;
            }
            if (icdList.size() > 1) {
                log.info("!!!!!!!!!!!!!!! > 1");
            }
            String encounterID = icdList.get(0).get("ID").toString();
            List<String> ods = jdbcTemplate.queryForList("select DiagnoseName from shly_patient_diagnosis_20180423 where EncounterID=? group by DiagnoseName", String.class, encounterID);
            if (ods == null || ods.isEmpty()) {
                continue;
            }

            log.info("rid->" + rid + ",new odCategories is ->" + ods.toArray(new String[]{}));
            BatchUpdateOption bathUpdateOption = new BatchUpdateOption();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(rid)));
            bathUpdateOption.setUpdate(Update.update("orgOdCategories", ods.toArray(new String[]{})));
            bathUpdateOption.setMulti(true);
            bathUpdateOption.setUpsert(false);
            options.add(bathUpdateOption);

            count++;
            //超过1000个执行一次更新
            if (options.size() >= 1000) {
                result += updateStart(options, "Record");
            }
        }
        if (!options.isEmpty()) {
            result += updateStart(options, "Record");
        }
        log.info(">>>>>>>>>>>Done," + count + ",effected:" + result);
    }

    public void test() {
        int pageNum = 1;
        boolean isFinish = false;
        Long count = 0L;

        List<Mapping> mapping = mongoOperations.findAll(Mapping.class, "Mapping");

        if (mapping == null || mapping.isEmpty()) {
            MappingMatch.addMappingRule(mongoOperations.getMongoTemplate());
            mapping = mongoOperations.findAll(Mapping.class, "Mapping");
        }
        Map<String, Long> calculator = new HashMap<>();
        while (!isFinish) {
            List<JSONObject> orderList = queryForList(jdbcTemplate, pageNum, 5000,
                    "select r.* from `shly_medical_content_20180423` r where r.`status`=0 and r.`住院号` not in (select p.AdmissionNumber from `shly_in_patient_visit_record_20180423` p) ");
            if ((orderList != null && orderList.size() < 5000)) {
                isFinish = true;
            }
            for (JSONObject jsonObject : orderList) {
                String name = jsonObject.getString("name");
                if (name == null || "".equals(name)) {
                    System.out.println("!!!!!!!!!!!!!!!!!");
                    continue;
                }
                String mappedValue = MappingMatch.getMappedValue(mapping, name);

                String[] types = mappedValue.split("-");
                if (calculator.isEmpty() || calculator.get(types[0]) == null) {
                    calculator.put(types[0], 1L);
                    continue;
                }
                Long typeCount = calculator.get(types[0]);
                calculator.put(types[0], ++typeCount);
            }
            count += orderList.size();
            pageNum++;
        }
        System.out.println("Count : " + count);
        System.out.println(calculator.toString());

    }

    protected List<JSONObject> queryForList(JdbcTemplate pJdbcTemplate, int currPageNum, int pageSize, String querySql) throws DataAccessException {
        StringBuilder newSql = new StringBuilder(querySql);
        if (pageSize > 0) {
            int startIndex = (currPageNum - 1) * pageSize;            //开始行索引
            if (StringUtils.isBlank(querySql)) {
                log.error("queryForList(): sql is empty");
                return null;
            }
            if (startIndex == 0) {
                newSql.append(" limit " + pageSize);
            }
            if (startIndex > 0) {
                newSql.append(" limit ").append(startIndex).append(",").append(pageSize);
            }
            log.info(">>>>>>>>>>sql : " + newSql.toString());
        }
        return pJdbcTemplate.query(newSql.toString(), new RowMapper<JSONObject>() {
            @Override
            public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                String name = rs.getString("文档名称");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);
                return jsonObject;
            }
        });
    }

    public void print() {
        Query query = new Query();
//        query.addCriteria(Criteria.where("_id").is("5ad6dfe4f4c7a2f31e9b93df"));
        JSONObject jsonObject = mongoOperations.findOne(query, JSONObject.class, "temp");
        List<String> allGroupRecordName = jsonObject.getObject("groupRecordName", List.class);
        StringBuilder sb = new StringBuilder();
        for (String groupRecordName : allGroupRecordName) {
            List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT ICD编码,诊断名称,诊断类型 from `病案首页诊断` where 一次就诊号=?", groupRecordName);
            StringBuilder element = new StringBuilder("[");
            for (Map<String, Object> map : maps) {
                element.append(iterateMap(map)).append(" | ");
            }
            element.append("]");
            sb.append("groupRecordName-> " + groupRecordName + ",诊断信息-> " + element.toString() + "\n");
        }

        String RESULT_FILE_PATH = "/Users/aron/out.txt";
        BufferedWriter resultWriter = null;

        try {
            resultWriter = new BufferedWriter(new FileWriter(RESULT_FILE_PATH));
            resultWriter.write(sb.toString());
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

    }

    private String iterateMap(Map<String, Object> map) {
        String icd = map.get("ICD编码").toString();
        String name = map.get("诊断名称").toString();
        String typeName = map.get("诊断类型").toString();
        return "ICD编码:" + icd + ",诊断名称:" + name + ",诊断类型:" + typeName;
    }

    public void cleanOdCategories() {
        int count = 0;
        int result = 0;
        List<BatchUpdateOption> options = new ArrayList<>();
//        DBObject dbQuery = new BasicDBObject();
//        dbQuery.put("batchNo", "shch20180416");
//        List<String> allGroupRecordName = hrsMongoTemplate.getCollection("Record").distinct("groupRecordName", dbQuery);
        Query query = new Query();
        query.addCriteria(Criteria.where("batchNo").is("shch20180416"));
        query.addCriteria(Criteria.where("odCategories").is("胃肠肿瘤"));
        List<JSONObject> records = mongoOperations.find(query, JSONObject.class, "Record");

        for (JSONObject record : records) {
            String rid = record.getString("_id");
            String groupRecordName = record.getString("groupRecordName");

            List<Map<String, Object>> icdList = jdbcTemplate.queryForList("select ICD编码 from 病案首页诊断 where 一次就诊号=? group by ICD编码", groupRecordName);

            Map<String, String> validOds = new HashMap<>();
            for (Map<String, Object> icdMap : icdList) {
                String icd = icdMap.get("ICD编码").toString();
                List<String> ods = jdbcTemplate.queryForList("select 病种类型 from 病种类型 where ICD编码=?", String.class, icd);
                if (ods == null || ods.isEmpty()) {
                    continue;
                }
                validOds.put(ods.get(0), null);
            }
            Set<String> odCategpries = validOds.keySet();

            log.info("rid->" + rid + ",new odCategories is ->" + odCategpries.toArray(new String[]{}));
            BatchUpdateOption bathUpdateOption = new BatchUpdateOption();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(rid))
                    .addCriteria(Criteria.where("batchNo").is("shch20180416")));
            bathUpdateOption.setUpdate(Update.update("odCategories", odCategpries.toArray(new String[]{})));
            bathUpdateOption.setMulti(true);
            bathUpdateOption.setUpsert(false);
            options.add(bathUpdateOption);

            count++;
            //超过1000个执行一次更新
            if (options.size() >= 1000) {
                result += updateStart(options, "Record");
            }
        }
        if (!options.isEmpty()) {
            result += updateStart(options, "Record");
        }
        log.info(">>>>>>>>>>>Done," + count);
    }

    public void spiltShly() {
        int count = 0;
        int result = 0;
        List<BatchUpdateOption> options = new ArrayList<>();

        DBObject dbQuery = new BasicDBObject();
        dbQuery.put("batchNo", "shly20180424");
        List<Object> patientIds = mongoOperations.distinctForDbCollection("Record", "patientId", dbQuery);

        for (Object patient : patientIds) {
            String pid = patient.toString();
//            log.info("pid->" + pid);
            BatchUpdateOption bathUpdateOption = new BatchUpdateOption();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(pid)));
            bathUpdateOption.setUpdate(Update.update("batchNo", "shly20180424"));
            bathUpdateOption.setMulti(true);
            bathUpdateOption.setUpsert(false);
            options.add(bathUpdateOption);
            count++;
            //超过1000个执行一次更新
            if (options.size() >= 1000) {
                result += updateStart(options, "Patient");
            }
        }
        if (!options.isEmpty()) {
            result += updateStart(options, "Patient");
        }
        log.info(">>>>>>>>>>>Done," + count + ",effected " + result);
    }

    /**
     * 检查不存在的patientID并创建
     */
    public void checkPatient() {
        int count = 0;
        List<JSONObject> options = new ArrayList<>();
        DBObject dbQuery = new BasicDBObject();
        dbQuery.put("batchNo", "shly20180423");
        List<Object> patientIds = mongoOperations.distinctForDbCollection("Record", "patientId", dbQuery);
        long createTime = System.currentTimeMillis();
        List<Object[]> jdbcPids = new ArrayList<>();
        for (Object patient : patientIds) {
            String pid = patient.toString();

            JSONObject patientInDB = mongoOperations.findById(pid, JSONObject.class, "Patient");
            if (patientInDB != null) {
                continue;
            }
            Patient forgedPatient = new Patient();
            forgedPatient.setPatientId(pid);
            forgedPatient.setCreateTime(createTime);
            forgedPatient.setForged(true);
            JSONObject json = (JSONObject) JSONObject.toJSON(forgedPatient);
            options.add(json);
            jdbcPids.add(new Object[]{Integer.valueOf(pid.substring(pid.indexOf("_") + 1))});
            count++;
            //超过1000个执行一次更新
            if (options.size() >= 1000) {
                log.info(">>>>>>>>>>>inserting " + options.size() + " count in mongo");
                mongoOperations.insert(options, "Patient");
                options.clear();
            }
        }
        if (!options.isEmpty()) {
            log.info(">>>>>>>>>>>inserting " + options.size() + " count in mongo");
            mongoOperations.insert(options, "Patient");
            options.clear();
        }
        log.info(">>>>>>>>>>>Done," + count);
    }

    public void cleanType() {
        int pageNum = 0;
        int count = 0;
        int inCount = 0;
        int outCount = 0;
        int result = 0;
        boolean isFinished = false;
        List<BatchUpdateOption> options = new ArrayList<>();
        while (!isFinished) {
            Query query = new Query();
//            shrj20180521，shrj20180522
            query.addCriteria(Criteria.where("batchNo").is("shrj20180522"));
            query.addCriteria(Criteria.where("source").is("病历文书"));
//            query.addCriteria(Criteria.where("odCategories").is("肝癌相关"));
            query.addCriteria(Criteria.where("recordType").in("入院记录", "出院记录"));
//            query.addCriteria(Criteria.where("sourceId").is("32127"));
            log.info(">>>>>>>>> pageNum:" + pageNum);
            query.with(new PageRequest(pageNum, 1000));
            //分页
            List<JSONObject> jsonObjects = mongoOperations.find(query, JSONObject.class, "Record");
            log.info(">>>>>>>>> found jsonObjects:" + jsonObjects.size());

            if (jsonObjects.size() < 1000) {
                isFinished = true;
            }

            List<Mapping> mapping = mongoOperations.findAll(Mapping.class, "Mapping");

            if (mapping == null || mapping.isEmpty()) {
                MappingMatch.addMappingRule(mongoOperations.getMongoTemplate());
                mapping = mongoOperations.findAll(Mapping.class, "Mapping");
            }

            for (JSONObject item : jsonObjects) {
                JSONObject info = item.getJSONObject("info");
                String sourceRecordType = item.getString("sourceRecordType");
                String dbRecordType = item.getString("recordType");
                String dbSubRecordType = item.getString("subRecordType");
                if (info == null) {
                    log.error("!!!!!!!!!!info is null ,_id: " + item.get("_id"));
                    continue;
                }
                String anchorContent = info.getString("text");
                String textARS = info.getString("textARS");

                if (textARS == null || "".equals(textARS)) {
                    log.error("!!!!!!!!!!textARS is null ,_id: " + item.get("_id"));
                    continue;
                }
                textARS = textARS.replaceAll("[　*| *| *|\\s*]*", "");

                textARS = textARS.length() > 50 ? textARS.substring(0, 50) : textARS;
                String mappedValue = MappingMatch.getMappedValue(mapping, sourceRecordType);

                String[] types = mappedValue.split("-");

                anchorMatch(anchorContent, dbRecordType, dbSubRecordType, item.getString("_id"), types);

                if (!dbSubRecordType.equals(types[1])
                        && ("入院记录".equals(types[0]) || "出院记录".equals(types[0]))) {
                    if ("入院记录".equals(types[0])) {
                        inCount++;
                    }
                    if ("出院记录".equals(types[0])) {
                        outCount++;
                    }
                    item.put("recordType", types[0]);
                    item.put("subRecordType", types[1]);
                    log.info(">>>>>>>>>>>原类型：" + dbRecordType + "-" + dbSubRecordType + ", 新类型：" + types[0] + "-" + types[1] + "->" + item.getString("_id"));
                    BatchUpdateOption bathUpdateOption = new BatchUpdateOption();
                    bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(item.get("_id"))));
                    bathUpdateOption.setUpdate(Update.update("recordType", types[0])
                            .set("subRecordType", types[1]));
                    bathUpdateOption.setMulti(true);
                    bathUpdateOption.setUpsert(false);
                    options.add(bathUpdateOption);
                    count++;
                }
            }
            //超过1000个执行一次更新
            if (options.size() >= 1000) {
                result += updateStart(options, "Record");
            }
            pageNum++;
        }
        if (!options.isEmpty()) {
            result += updateStart(options, "Record");
        }
        log.info(">>>>>>>>>>>Done," + count + ",all effected:" + result);
        log.info(">>>>>>>>>>>Done,incCunt:" + inCount + ",outCount:" + outCount);
    }

    private int updateStart(List<BatchUpdateOption> options, String collectionName) {
        int effected = mongoOperations.batchUpdate(collectionName, options);
        log.info(">>>>>>>>>>>受影响的行数," + effected);
        options.clear();
        return effected;
    }

    /**
     * 锚点匹配操作，目前只对入院做处理
     *
     * @param anchorContent
     * @param dbRecordType
     * @param dbSubRecordType
     * @param id
     * @param types
     */
    private void anchorMatch(String anchorContent, String dbRecordType, String dbSubRecordType, String id, String[] types) {
        if (!("入院记录".equals(types[0]) || "出院记录".equals(types[0]))) {
            return;
        }
        String[] inHospital = {"现病史", "个人史", "婚育史", "月经史", "家族史", "既往史"};
        String[] outHospital = {"治疗经过", "诊疗经过", "出院指导", "出院医嘱", "出院诊断", "出院情况"};

        Pattern pattern = Pattern.compile("【【(.*?)】】");
        Matcher matcher = pattern.matcher(anchorContent);

        Map<String, String> anchors = new HashMap<>();
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group == null) {
                continue;
            }
            group.trim();
            if ("".equals(group)) {
                continue;
            }
            anchors.put(matcher.group(1), null);
        }

        int inCount = 0;
        int outCount = 0;
        for (String in : inHospital) {
            if (anchors.containsKey(in)) {
                inCount++;
            }
        }
        for (String out : outHospital) {
            if (anchors.containsKey(out)) {
                outCount++;
            }
        }
        boolean matched = false;
        if ("入院记录".equals(types[0])) {
            if (inCount >= 2) {
                matched = true;
            } else if (inCount < 2 && outCount >= 2) {
                matched = true;
                log.info("匹配锚点个数为：" + inCount + "，修改为出院记录,原始类型:" + dbRecordType + ",id:" + id);
                types[0] = "出院记录";
                if (anchorContent.contains("死亡时间")) {
                    types[1] = "死亡记录";
                } else if (anchorContent.contains("出院小结")) {
                    types[1] = "出院小结";
                } else {
                    types[1] = "出院记录";
                }
            }
        }
        if ("出院记录".equals(types[0])) {
            if (outCount >= 2) {
                matched = true;
            } else if (outCount < 2 && inCount >= 2) {
                matched = true;
                log.info("匹配锚点个数为：" + outCount + "，修改为入院记录,sourceRecordType:" + dbRecordType + ",id:" + id);
                types[0] = "入院记录";

                if (anchorContent.contains("小时内入出院")) {
                    types[1] = "24小时内入出院";
                } else if (anchorContent.contains("病案首页")) {
                    types[1] = "病案首页";
                } else {
                    types[1] = "入院记录";
                }
            }
        }
        if (!matched) {
            types[0] = dbRecordType;
            types[1] = dbSubRecordType;
        }
    }

}
