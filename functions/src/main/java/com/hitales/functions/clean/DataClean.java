package com.hitales.functions.clean;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.BatchUpdateOption;
import com.hitales.common.support.Mapping;
import com.hitales.common.support.MappingMatch;
import com.hitales.common.support.MongoOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    private static MongoTemplate hrsMongoTemplate;

    private static JdbcTemplate jdbcTemplate;

    static {
        MongoProperties hrsProperties = new MongoProperties();
        hrsProperties.setHost("localhost");
        hrsProperties.setPort(27017);
        hrsProperties.setDatabase("HRS");
        hrsProperties.setUsername("aron");
        hrsProperties.setPassword("aron".toCharArray());
        hrsMongoTemplate = DBConnection.generateTemplate(hrsProperties);

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:mysql://rm-bp1a049g618bz7l2q.mysql.rds.aliyuncs.com:3306/zhongliuxiangguan?autoReconnect=true");
        dataSourceBuilder.username("health");
        dataSourceBuilder.password("Yiy1health_2017");
        dataSourceBuilder.driverClassName("com.mysql.jdbc.Driver");
        DataSource dataSource = dataSourceBuilder.build();
        jdbcTemplate = new JdbcTemplate(dataSource);

    }

    public static void main(String[] args) {
        DataClean dataClean = new DataClean();
        dataClean.cleanOdCategories();
    }

    public void print() {
        Query query = new Query();
//        query.addCriteria(Criteria.where("_id").is("5ad6dfe4f4c7a2f31e9b93df"));
        JSONObject jsonObject = hrsMongoTemplate.findOne(query, JSONObject.class, "temp");
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
        query.addCriteria(Criteria.where("odCategories").size(2));
        List<JSONObject> records = hrsMongoTemplate.find(query, JSONObject.class, "Record");

        for (JSONObject record : records) {
            String rid = record.getString("_id");
            List<String> odCategories = record.getObject("odCategories", List.class);


            Map<String, String> ods = new HashMap<>();

            for (String odCategory : odCategories) {
                ods.put(odCategory, null);
            }
            if (ods.isEmpty()) {
                log.error("!!!!!!!!!!找不到病种，rid -> " + rid);
                continue;
            }
            if (ods.size() == 2) {
                continue;
            }
            Set<String> keys = ods.keySet();
            log.info("old odCategories->" + odCategories.toString() + " ,new odCategories is ->" + keys.toArray()[0]);
            BatchUpdateOption bathUpdateOption = new BatchUpdateOption();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(rid))
                    .addCriteria(Criteria.where("batchNo").is("shch20180416")));
            bathUpdateOption.setUpdate(Update.update("odCategories", keys.toArray(new String[]{})));
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
            query.addCriteria(Criteria.where("batchNo").is("shch20180416"));
            query.addCriteria(Criteria.where("sourceType").is("text"));
//            query.addCriteria(Criteria.where("odCategories").is("肝癌相关"));
            query.addCriteria(Criteria.where("recordType").is("治疗方案"));
//            query.addCriteria(Criteria.where("sourceId").is("32127"));
            log.info(">>>>>>>>> pageNum:" + pageNum);
            query.with(new PageRequest(pageNum, 1000));
            //分页
            List<JSONObject> jsonObjects = hrsMongoTemplate.find(query, JSONObject.class, "Record");
            log.info(">>>>>>>>> found jsonObjects:" + jsonObjects.size());

            if (jsonObjects.size() < 1000) {
                isFinished = true;
            }

            List<Mapping> mapping = hrsMongoTemplate.findAll(Mapping.class, "Mapping");

            if (mapping == null || mapping.isEmpty()) {
                MappingMatch.addMappingRule(hrsMongoTemplate);
                mapping = hrsMongoTemplate.findAll(Mapping.class, "Mapping");
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
//                result += updateStart(options);
            }
            pageNum++;
        }
        if (!options.isEmpty()) {
//            result += updateStart(options);
        }
        log.info(">>>>>>>>>>>Done," + count + ",all effected:" + result);
        log.info(">>>>>>>>>>>Done,incCunt:" + inCount + ",outCount:" + outCount);
    }

    private int updateStart(List<BatchUpdateOption> options) {
        MongoOperations mongoOperations = new MongoOperations(hrsMongoTemplate);
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
