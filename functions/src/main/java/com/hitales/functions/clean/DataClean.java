package com.hitales.functions.clean;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.BatchUpdateOption;
import com.hitales.common.support.Mapping;
import com.hitales.common.support.MappingMatch;
import com.hitales.common.support.MongoOperations;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DataClean {

    private static MongoOperations mongoOperations;

    private static JdbcTemplate jdbcTemplate;

    static {
        MongoProperties hrsProperties = new MongoProperties();
        hrsProperties.setHost("localhost");
        hrsProperties.setPort(27017);
        hrsProperties.setDatabase("HRS");
        hrsProperties.setUsername("aron");
        hrsProperties.setPassword("aron".toCharArray());
        mongoOperations = new MongoOperations(DBConnection.generateTemplate(hrsProperties));

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:mysql://localhost:3306/local?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&useSSL=false");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("woshixuhu1217");
        dataSourceBuilder.driverClassName("com.mysql.jdbc.Driver");
        DataSource dataSource = dataSourceBuilder.build();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public static void main(String[] args) {
        DataClean dataClean = new DataClean();
        dataClean.cleanType();
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
                result += updateStart(options);
            }
        }
        if (!options.isEmpty()) {
            result += updateStart(options);
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
            Query query = new Query();
            query.addCriteria(Criteria.where("batchNo").is("shly20180423"));
            query.addCriteria(Criteria.where("patientId").is(pid));
            List<JSONObject> records = mongoOperations.find(query, JSONObject.class, "Record");
            if (records == null || records.isEmpty()) {
                continue;
            }
//            log.info("pid->" + pid);
            for (JSONObject record : records) {
                String rid = record.getString("_id");

                BatchUpdateOption bathUpdateOption = new BatchUpdateOption();
                bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(rid)));
                bathUpdateOption.setUpdate(Update.update("batchNo", "shly20180424"));
                bathUpdateOption.setMulti(true);
                bathUpdateOption.setUpsert(false);
                options.add(bathUpdateOption);
                count++;
                //超过1000个执行一次更新
                if (options.size() >= 1000) {
                    result += updateStart(options);
                    options.clear();
                }
            }
        }
        if (!options.isEmpty()) {
            result += updateStart(options);
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
//            log.info("pid->" + pid);
            JSONObject newPatient = new JSONObject();
            newPatient.put("_id", pid);
            newPatient.put("姓名", "");
            newPatient.put("batchNo", "shly20180423");
            newPatient.put("婚姻状况", "");
            newPatient.put("hospitalId", "5ad86cb8acc162a73ee74f16");
            newPatient.put("createTime", createTime);
            newPatient.put("出生日期", "");
            newPatient.put("updateTime", System.currentTimeMillis());
            newPatient.put("年龄", "");
            newPatient.put("现住址", "");
            newPatient.put("籍贯", "");
            newPatient.put("性别", "");
            newPatient.put("血型", "");
            newPatient.put("名族", "");
            newPatient.put("职业", "");
            newPatient.put("isForged", true);
            options.add(newPatient);
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
            query.addCriteria(Criteria.where("batchNo").is("shly20180424"));
            query.addCriteria(Criteria.where("source").is("病历文书"));
//            query.addCriteria(Criteria.where("odCategories").is("肝癌相关"));
            query.addCriteria(Criteria.where("recordType").is("手术操作记录"));
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
//                String sourceRecordType = item.getString("sourceRecordType");
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
                String mappedValue = MappingMatch.getMappedValue(mapping, textARS);

                String[] types = mappedValue.split("-");

                anchorMatch(anchorContent, dbRecordType, dbSubRecordType, item.getString("_id"), types);

                if (!dbSubRecordType.equals(types[1])){
//                        && ("入院记录".equals(types[0]) || "出院记录".equals(types[0]))) {
                    /*if ("入院记录".equals(types[0])) {
                        inCount++;
                    }
                    if ("出院记录".equals(types[0])) {
                        outCount++;
                    }*/
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
                result += updateStart(options);
            }
            pageNum++;
        }
        if (!options.isEmpty()) {
            result += updateStart(options);
        }
        log.info(">>>>>>>>>>>Done," + count + ",all effected:" + result);
        log.info(">>>>>>>>>>>Done,incCunt:" + inCount + ",outCount:" + outCount);
    }

    private int updateStart(List<BatchUpdateOption> options) {
        int effected = mongoOperations.batchUpdate("Record", options);
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
        String[] outHospital = {"治疗经过", "诊疗经过", "出院指导", "出院医嘱", "出院诊断"};

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
