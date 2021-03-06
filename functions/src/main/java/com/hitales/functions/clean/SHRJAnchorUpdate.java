package com.hitales.functions.clean;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.SplitAnchor;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

public class SHRJAnchorUpdate {

    static MongoCredential mongoCredential = MongoCredential.createCredential("xh", "HRS-live", "rt0hizu{j9lzJNqi".toCharArray());
    static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
//    static ServerAddress serverAddress = new ServerAddress("localhost", 27017);

    static List<MongoCredential> mongoCredentials = new ArrayList<>();

    static {
        mongoCredentials.add(mongoCredential);
    }

    //static ServerAddress serverAddress = new ServerAddress("localhost", 27017);
    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    //static MongoClient mongo = new MongoClient("localhost", 27017);
    static MongoDatabase db = mongo.getDatabase("HRS-live");
    static MongoCollection dc = db.getCollection("Record");

    private static MongoTemplate hrsMongoTemplate;

    static {
        MongoProperties mongoProperties = new MongoProperties();
        mongoProperties.setHost("localhost");
        mongoProperties.setPort(3718);
        mongoProperties.setDatabase("HRS-live");
        mongoProperties.setUsername("xh");
        mongoProperties.setPassword("rt0hizu{j9lzJNqi".toCharArray());
        hrsMongoTemplate = DBConnection.generateTemplate(mongoProperties);
    }

    static List<String> anchorsList = new ArrayList<>();

    static {
    }

    //禁用的锚点
    static List<String> notAnchorList = new ArrayList<>();

    static {
//        notAnchorList.add("出院诊断");
//        notAnchorList.add("初步诊断");
    }

    //前面不是中文的需要打上锚点
    static List<String> prefAnchorList = new ArrayList<>();

    static {
        /*prefAnchorList.add("补充病史和体征");
        prefAnchorList.add("初步诊断");
        prefAnchorList.add("诊断依据");
        prefAnchorList.add("鉴别诊断");
        prefAnchorList.add("诊疗计划");
        prefAnchorList.add("注意事项");
        prefAnchorList.add("分析讨论");
        prefAnchorList.add("入院日期");
        prefAnchorList.add("出院日期");
        prefAnchorList.add("门诊诊断");
        prefAnchorList.add("入院诊断");
        prefAnchorList.add("出院诊断");
        prefAnchorList.add("入院时主要症状及体征");
        prefAnchorList.add("病程与治疗结果（注明手术日期、手术名称、输血量及抢救情况）");
        prefAnchorList.add("病程与治疗结果(注明手术日期、手术名称、输血量及抢救情况)");
        prefAnchorList.add("出院时情况（症状及体征）");
        prefAnchorList.add("出院时情况(症状及体征)");
        prefAnchorList.add("出院后用药及建议");
        prefAnchorList.add("医师签名");
        prefAnchorList.add("主　诉");
        prefAnchorList.add("现病史");
        prefAnchorList.add("既往史");
        prefAnchorList.add("个人史");
        prefAnchorList.add("婚育史");
        prefAnchorList.add("月经史");
        prefAnchorList.add("家族史");
        prefAnchorList.add("体　格　检　查");
        prefAnchorList.add("专　科　检　查");
        prefAnchorList.add("实验室及其他辅助检查");
        prefAnchorList.add("初步诊断");
        prefAnchorList.add("医师签名");
        prefAnchorList.add("日　　期");
        prefAnchorList.add("主要化验结果");
        prefAnchorList.add("特殊检验及重要会诊");
        prefAnchorList.add("合并症");
        prefAnchorList.add("治疗结果");
        prefAnchorList.add("病史可靠程度");
        prefAnchorList.add("病史陈述者");
        prefAnchorList.add("病史采集时间");
        prefAnchorList.add("入院时主要病状及体征");
        prefAnchorList.add("预约提示");
        prefAnchorList.add("主任医师");
        prefAnchorList.add("主治医师");
        prefAnchorList.add("住院医师");
        prefAnchorList.add("记录日期");
        prefAnchorList.add("系统回顾(阳性表现应在空间内填写发病时间及扼要诊疗经过)");
        prefAnchorList.add("姓名");
        prefAnchorList.add("门（急）诊诊断");
        prefAnchorList.add("与患者关系");*/
        prefAnchorList.add("签名");
    }

    //前面是中文的锚点要去掉
    static List<String> prefNotAnchorList = new ArrayList<>();

    static {

    }

    //前面不能为中文，后面跟冒号作为锚点
    static List<String> colonAnchorList = new ArrayList<>();

    static {
//        colonAnchorList.add("查体");
//        colonAnchorList.add("入院查体");
//        colonAnchorList.add("诊断日期");
    }

    //中括号包围的锚点，中括号是特殊字符
    static List<String> bracketAnchoList = new ArrayList<>();

    static {
    }

    static List<String> colonEndAnchorList = new ArrayList<>();

    static {
//        colonEndAnchorList.add("出院诊断");
//        colonEndAnchorList.add("初步诊断");
//        colonEndAnchorList.add("病例特点");
        colonEndAnchorList.add("诊断日期");
        colonEndAnchorList.add("主要诊断");
        colonEndAnchorList.add("其他诊断");
    }

    public static void main(String[] args) {
        BasicDBObject docQuery = new BasicDBObject();
//        shrj20180521，shrj20180522
        docQuery.append("batchNo", "shrj20180522");
        docQuery.append("source", "病历文书");
        BasicDBList recordTypeList = new BasicDBList();
//        recordTypeList.add(new BasicDBObject("recordType", "手术操作记录"));
        recordTypeList.add(new BasicDBObject("recordType", "入院记录"));
        recordTypeList.add(new BasicDBObject("recordType", "出院记录"));
        docQuery.put("$or", recordTypeList);
        long sum = dc.count(docQuery);
        System.out.println(sum);
        FindIterable<Document> iterable = dc.find(docQuery);
        MongoCursor<Document> itor = iterable.iterator();
        int m = 0;
        List<BathUpdateOptions> options = new ArrayList<>();
        while (itor.hasNext()) {
            Document document = itor.next();
            JSONObject jsonObject = JSONObject.parseObject(document.toJson());
            System.out.println(jsonObject.getString("_id"));
            String textARS = jsonObject.getJSONObject("info").getString("text");

           /* String textARS = jsonObject.getJSONObject("info").getString("textARS");
            textARS = TextFormatter.formatTextByAnchaor(textARS);*/
//            String text_back = jsonObject.getJSONObject("info").getString("text_back");
//            textARS = text_back;
            /*if (!jsonObject.getJSONObject("info").containsKey("text_back")) {
                jsonObject.getJSONObject("info").put("text_back", text_back);
            } else {
                textARS = jsonObject.getJSONObject("info").getString("text_back");
            }*/
//            String textARS = jsonObject.getJSONObject("info").getString("textARS");
//            textARS = TextFormatter.formatTextByAnchaor(textARS);
            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");
            /*for(String anchor : anchorsList){
                textARS = textARS.replaceAll(anchor, "【【" + anchor + "】】");
            }*/

            //textARS = textARS.replaceAll("【【原文记录标题】】", "");
            for (String anchor : anchorsList) {
                textARS = textARS.replaceAll(anchor, "【【" + anchor + "】】");
            }
            for (String anchor : notAnchorList) {
                textARS = textARS.replaceAll("【【" + anchor + "】】", anchor);
            }
            for (String anchor : bracketAnchoList) {
                textARS = textARS.replaceAll("\\[" + anchor + "\\]", "【【" + anchor + "】】");
            }
            for (String anchor : prefAnchorList) {
                int lastIndex = 0;
                while (textARS.indexOf(anchor, lastIndex) != -1) {
                    int index = textARS.indexOf(anchor, lastIndex);
                    if (index == 0 || !SplitAnchor.isChinese(textARS.charAt(index - 1))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(textARS.substring(0, index));
                        stringBuilder.append("【【" + anchor + "】】");
                        stringBuilder.append(textARS.substring(index + anchor.length()));
                        textARS = stringBuilder.toString();
                    }
                    lastIndex = index + anchor.length();
                }
            }
            for (String anchor : prefNotAnchorList) {
                int lastIndex = 0;
                String originAnchor = anchor;
                anchor = "【【" + anchor + "】】";
                while (textARS.indexOf(anchor, lastIndex) != -1) {
                    int index = textARS.indexOf(anchor, lastIndex);
                    if (index != 0 && SplitAnchor.isChinese(textARS.charAt(index - 1))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(textARS.substring(0, index));
                        stringBuilder.append(originAnchor);
                        stringBuilder.append(textARS.substring(index + anchor.length()));
                        textARS = stringBuilder.toString();
                    }
                    lastIndex = index + anchor.length();
                }
            }
            for (String anchor : colonAnchorList) {
                int lastIndex = 0;
                while (textARS.indexOf(anchor, lastIndex) != -1) {
                    int index = textARS.indexOf(anchor, lastIndex);
                    if ((index == 0 || !SplitAnchor.isChinese(textARS.charAt(index - 1))) &&
                            ('：' == textARS.charAt(index + anchor.length()) || ':' == textARS.charAt(index + anchor.length()))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(textARS.substring(0, index));
                        stringBuilder.append("【【" + anchor + "】】");
                        stringBuilder.append(textARS.substring(index + anchor.length()));
                        textARS = stringBuilder.toString();
                    }
                    lastIndex = index + anchor.length();
                }
            }
            for (String anchor : colonEndAnchorList) {
                int lastIndex = 0;
                while (textARS.indexOf(anchor, lastIndex) != -1) {
                    int index = textARS.indexOf(anchor, lastIndex);
                    if ('：' == textARS.charAt(index + anchor.length()) || ':' == textARS.charAt(index + anchor.length())
                            || ' ' == textARS.charAt(index + anchor.length()) || '　'== textARS.charAt(index + anchor.length()) ) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(textARS.substring(0, index));
                        stringBuilder.append("【【" + anchor + "】】");
                        stringBuilder.append(textARS.substring(index + anchor.length()));
                        textARS = stringBuilder.toString();
                    }
                    lastIndex = index + anchor.length();
                }
            }
            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");

            textARS = textARS.replaceAll("【【既往史】】、手术史、药敏史、【【家族史】】", "既往史、手术史、药敏史、家族史");
            textARS = textARS.replaceAll("【化验检查】", "【【化验检查】】");
            textARS = textARS.replaceAll("【入院前检查】", "【【入院前检查】】");

            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");
            BathUpdateOptions bathUpdateOption = BathUpdateOptions.getInstance();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(jsonObject.get("_id"))));
            bathUpdateOption.setUpdate(Update.update("info.text", textARS)
//                    .set("info.text_back", text_back)
                    .set("updateTime", System.currentTimeMillis()));
            bathUpdateOption.setMulti(true);
            bathUpdateOption.setUpsert(false);
            options.add(bathUpdateOption);
            System.out.println(sum + " " + ++m);
        }
        int count = options.size() / 1000 + (options.size() % 1000 == 0 ? 0 : 1);
        for (int i = 0; i < count; i++) {
            int result = doBathUpdate(hrsMongoTemplate.getCollection("Record"), "Record", options.subList(i * 1000, (i + 1) * 1000 > options.size() ? options.size() : (i + 1) * 1000), true);
            System.out.println(result);
        }
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

    static class BathUpdateOptions {
        private Query query;
        private Update update;
        private boolean upsert = false;
        private boolean multi = false;

        public static BathUpdateOptions getInstance() {
            return new BathUpdateOptions();
        }

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
