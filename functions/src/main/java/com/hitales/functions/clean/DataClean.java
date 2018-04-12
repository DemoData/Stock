package com.hitales.functions.clean;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.Mapping;
import com.hitales.common.support.MappingMatch;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DataClean {

    //    @Autowired
//    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    private static MongoTemplate hrsMongoTemplate;

    static {
        MongoProperties mongoProperties = new MongoProperties();
        mongoProperties.setHost("localhost");
        mongoProperties.setPort(27017);
        mongoProperties.setDatabase("HRS");
        mongoProperties.setUsername("aron");
        mongoProperties.setPassword("aron".toCharArray());
        hrsMongoTemplate = DBConnection.generateTemplate(mongoProperties);
    }

    public static void main(String[] args) {
        DataClean dataClean = new DataClean();
        dataClean.cleanData();
    }

    public void cleanData() {
        int pageNum = 0;
        int count = 0;
        int inCount = 0;
        int outCount = 0;
        boolean isFinished = false;
        List<BathUpdateOptions> options = new ArrayList<>();
        while (!isFinished) {
            Query query = new Query();
            query.addCriteria(Criteria.where("batchNo").is("shch2018040901"));
            query.addCriteria(Criteria.where("odCategories").is("肝癌相关"));
            query.addCriteria(Criteria.where("recordType").is("其他记录"));
//            query.addCriteria(Criteria.where("sourceId").is("296494"));
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

                if (!dbSubRecordType.equals(types[1]) && ("入院记录".equals(types[0]) || "出院记录".equals(types[0]))) {
                    if ("入院记录".equals(types[0])) {
                        inCount++;
                    }
                    if ("出院记录".equals(types[0])) {
                        outCount++;
                    }
                    item.put("recordType", types[0]);
                    item.put("subRecordType", types[1]);
                    log.info(">>>>>>>>>>>原类型：" + dbRecordType + "-" + dbSubRecordType + ", 新类型：" + types[0] + "-" + types[1] + "->" + item.getString("_id"));
                    BathUpdateOptions bathUpdateOption = new BathUpdateOptions();
                    bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(item.get("_id"))));
                    bathUpdateOption.setUpdate(Update.update("recordType", types[0]).set("subRecordType", types[1]));
                    bathUpdateOption.setMulti(true);
                    bathUpdateOption.setUpsert(false);
                    options.add(bathUpdateOption);
                    count++;
                }
            }
            //超过1000个执行一次更新
            if (options.size() >= 1000) {
                int result = bathUpdate(hrsMongoTemplate, "Record", options);
                log.info(">>>>>>>>>>>受影响的行数," + result);
                options.clear();
            }
            pageNum++;
        }
        if (!options.isEmpty()) {
            int result = bathUpdate(hrsMongoTemplate, "Record", options);
            log.info(">>>>>>>>>>>受影响的行数," + result);
            options.clear();
        }
        log.info(">>>>>>>>>>>Done," + count);
        log.info(">>>>>>>>>>>Done,incCunt:" + inCount + ",outCount" + outCount);
    }

    public static int bathUpdate(MongoTemplate mongoTemplate, String collectionName,
                                 List<BathUpdateOptions> options) {
        return doBathUpdate(mongoTemplate.getCollection(collectionName),
                collectionName, options, true);
    }

    private static int doBathUpdate(DBCollection dbCollection, String collName,
                                    List<BathUpdateOptions> options, boolean ordered) {
        DBObject command = new BasicDBObject();
        command.put("update", collName);
        List<BasicDBObject> updateList = new ArrayList<BasicDBObject>();
        for (BathUpdateOptions option : options) {
            BasicDBObject update = new BasicDBObject();
            update.put("q", option.getQuery().getQueryObject());
            update.put("u", option.getUpdate().getUpdateObject());
            update.put("upsert", option.isUpsert());
            update.put("multi", option.isMulti());
            updateList.add(update);
        }
        command.put("updates", updateList);
        command.put("ordered", ordered);
        CommandResult commandResult = dbCollection.getDB().command(command);
        if (commandResult == null || commandResult.get("n") == null) {
            return 0;
        }
        return Integer.parseInt(commandResult.get("n").toString());
    }

    public void updateRecordTypeByAnchor() {
        Query query = new Query();
        query.addCriteria(Criteria.where("batchNo").is("bdsz20180320"));
        query.addCriteria(Criteria.where("recordType").in("入院记录"));
        List<JSONObject> jsonObjects = hrsMongoTemplate.find(query, JSONObject.class, "Record");
        log.info("jsonObjects:" + jsonObjects.size());

        Long count = 0L;
        for (JSONObject item : jsonObjects) {
            JSONObject info = item.getJSONObject("info");
            if (info == null) {
                log.error("info is null ,_id: " + item.get("_id"));
                continue;
            }
            String text = info.getString("text");
            if (text == null || "".equals(text)) {
                log.error("textARS is null ,_id: " + item.get("_id"));
                continue;
            }

//            count = anchorMatch(text, item, count);
        }
        log.info("Done," + count);

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


    class BathUpdateOptions {
        private Query query;
        private Update update;
        private boolean upsert = false;
        private boolean multi = false;

        public Query getQuery() {
            return query;
        }

        public void setQuery(Query query) {
            this.query = query;
        }

        public Update getUpdate() {
            return update;
        }

        public void setUpdate(Update update) {
            this.update = update;
        }

        public boolean isUpsert() {
            return upsert;
        }

        public void setUpsert(boolean upsert) {
            this.upsert = upsert;
        }

        public boolean isMulti() {
            return multi;
        }

        public void setMulti(boolean multi) {
            this.multi = multi;
        }
    }


}
